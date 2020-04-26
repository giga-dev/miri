package com.gigaspaces.miri.dialogs;

import com.gigaspaces.miri.GitHubAssistant;
import com.gigaspaces.miri.MiriUtils;
import com.gigaspaces.miri.actions.FixedStepsProgressTask;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.components.*;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class DeleteBranchesDialog extends MiriCustomDialog {
    private final JPanel mainPanel = new JPanel(new BorderLayout());
    private final JBTextField filterTextField = new JBTextField();
    private final JButton findButton = new JButton("Find");
    private final JPanel repositoriesPanel;
    private final JBList<String> branchesList = new JBList<>();
    private final JBLabel branchesSummary = new JBLabel();

    private final GitHubAssistant assistant;
    private final Project project;
    private final Map<String, Collection<String>> repoBranches = new HashMap<>();

    public DeleteBranchesDialog(GitHubAssistant assistant, Project project) {
        super();
        this.assistant = assistant;
        this.project = project;

        setTitle("Delete Branches");

        Map<String, Boolean> repos = assistant.getRepos();
        repositoriesPanel = new JPanel(new GridLayout(repos.size(), 1));
        repositoriesPanel.setBorder(BorderFactory.createTitledBorder("Repositories"));
        for (Map.Entry<String, Boolean> entry : repos.entrySet()) {
            repositoriesPanel.add(new JCheckBox(entry.getKey(), entry.getValue()));
        }

        JPanel filterPanel = new JPanel(new BorderLayout());
        filterPanel.add(new JBLabel("Filter:"), BorderLayout.WEST);
        filterPanel.add(filterTextField, BorderLayout.CENTER);
        filterPanel.add(findButton, BorderLayout.EAST);
        findButton.addActionListener(e -> findBranches());
        //filterTextField.getEmptyText().setText("prefix");

        JPanel searchPanel = new JPanel(new BorderLayout());
        searchPanel.add(filterPanel, BorderLayout.NORTH);
        searchPanel.add(repositoriesPanel, BorderLayout.CENTER);

        this.branchesList.setMinimumSize(new Dimension(80, 300));
        this.branchesList.setMaximumSize(new Dimension(80, 600));
        JPanel resultsPanel = new JPanel(new BorderLayout());
        resultsPanel.setBorder(BorderFactory.createTitledBorder("Branches"));
        resultsPanel.add(new JBScrollPane(branchesList), BorderLayout.CENTER);
        resultsPanel.add(branchesSummary, BorderLayout.SOUTH);

        mainPanel.add(searchPanel, BorderLayout.NORTH);
        mainPanel.add(resultsPanel, BorderLayout.CENTER);

        init();

        getButton(getOKAction()).setText("Delete");
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return mainPanel;
    }

    private Collection<String> listBranches(String repository) throws IOException {
        if (!repoBranches.containsKey(repository)) {
            Collection<String> branches = ProgressManager.getInstance().runProcessWithProgressSynchronously(
                    () -> assistant.listBranches(repository), "Listing branches of " + repository, false, project);
            repoBranches.put(repository, branches);
        }
        return repoBranches.get(repository);
    }

    private void findBranches() {
        final List<String> selectedRepos = getSelectedRepositories();
        if (selectedRepos.size() == 0) {
            Messages.showWarningDialog("No repositories were selected", MiriUtils.TITLE);
            return;
        }

        Collection<String> branches;
        try {
            branches = listBranches(selectedRepos.get(0));
        } catch (IOException e) {
            Messages.showErrorDialog(e.toString(), MiriUtils.TITLE);
            return;
        }

        refreshFilteredBranches(branches);
    }

    private void refreshFilteredBranches(Collection<String> branches) {
        String pattern = filterTextField.getText();
        String[] filteredBranches = branches.stream().filter(s -> s.contains(pattern)).toArray(String[]::new);
        branchesList.setListData(filteredBranches);
        branchesSummary.setText("Results: " + filteredBranches.length);
    }

    private List<String> getSelectedRepositories() {
        List<String> result = new ArrayList<>();
        for (Component component : repositoriesPanel.getComponents()) {
            if (component instanceof JCheckBox) {
                JCheckBox checkBox = (JCheckBox) component;
                if (checkBox.isSelected())
                    result.add(checkBox.getText());
            }
        }
        return result;
    }

    @Override
    protected boolean executeOKAction() {
        final List<String> selectedRepos = getSelectedRepositories();
        if (selectedRepos.size() == 0) {
            Messages.showWarningDialog("No repositories were selected", MiriUtils.TITLE);
            return false;
        }
        final List<String> selectedBranches = branchesList.getSelectedValuesList();
        if (selectedBranches.size() == 0) {
            Messages.showWarningDialog("No branches were selected", MiriUtils.TITLE);
            return false;
        }
        int max = 10;
        if (selectedBranches.size() > max) {
            Messages.showWarningDialog("Cannot delete more than " + max + " branches at once", MiriUtils.TITLE);
            return false;
        }

        if (selectedBranches.contains("master")) {
            Messages.showWarningDialog("Cannot delete branch 'master'", MiriUtils.TITLE);
            return false;
        }

        String question = "Are you sure you want to delete the following " + selectedBranches.size() + " branches?" + System.lineSeparator() +
                String.join(System.lineSeparator(), selectedBranches) + System.lineSeparator() +
                "To confirm, re-type the number of branches which will be deleted (" + selectedBranches.size() + ")"
                ;
        String result = Messages.showInputDialog(question, MiriUtils.TITLE, Messages.getQuestionIcon());
        if (!Objects.equals(result, String.valueOf(selectedBranches.size()))) {
            return false;
        }

        Collection<String> branches = repoBranches.get(selectedRepos.get(0));
        List<String> failures = new ArrayList<>();
        ProgressManager.getInstance().runProcessWithProgressSynchronously(new FixedStepsProgressTask(selectedRepos.size() * selectedBranches.size() + 1) {
            @Override
            protected void execute() {
                for (String branchName : selectedBranches) {
                    for (String repoName : selectedRepos) {
                        try {
                            nextStep("Deleting branch " + branchName + " in " + repoName);
                            assistant.deleteBranch(repoName, branchName);
                        } catch (IOException ex) {
                            failures.add(branchName + " from " + repoName + ": " + ex.toString());
                        }
                    }
                    branches.remove(branchName);
                }
            }
        }, "Deleting " + selectedBranches.size() + " branches", false, project);
        if (failures.isEmpty()) {
            Messages.showInfoMessage("Deleted " + selectedBranches.size() + " branches", MiriUtils.TITLE);
        } else {
            Messages.showErrorDialog("Failed to delete the following: " + System.lineSeparator() +
                    String.join(System.lineSeparator(), failures), MiriUtils.TITLE);
        }
        refreshFilteredBranches(branches);
        return false;
    }
}

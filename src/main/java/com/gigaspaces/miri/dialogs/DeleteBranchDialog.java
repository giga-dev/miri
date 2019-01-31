package com.gigaspaces.miri.dialogs;

import com.gigaspaces.miri.GitHubAssistant;
import com.gigaspaces.miri.MiriUtils;
import com.gigaspaces.miri.actions.FixedStepsProgressTask;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DeleteBranchDialog extends MiriCustomDialog {
    private final String DEL_BRANCH_CAPTION = "Branch to delete";
    private final JPanel repositoriesPanel = new JPanel();
    private final GitHubAssistant assistant;
    private final Project project;

    public DeleteBranchDialog(GitHubAssistant assistant, Project project) {
        super();
        this.assistant = assistant;
        this.project = project;

        setTitle("Delete Branch");

        Map<String, Boolean> repos = assistant.getRepos();
        this.repositoriesPanel.setLayout(new GridLayout(repos.size(), 1));
        for (Map.Entry<String, Boolean> entry : repos.entrySet()) {
            repositoriesPanel.add(new JCheckBox(entry.getKey(), entry.getValue()));
        }

        addTextField(DEL_BRANCH_CAPTION);
        add("Repositories", repositoriesPanel);

        init();
    }

    private java.util.List<String> getSelectedRepositories() {
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
        final String branchName = getRequiredTextField(DEL_BRANCH_CAPTION);
        if (branchName == null) {
            return false;
        }
        final List<String> selectedRepos = getSelectedRepositories();
        if (selectedRepos.size() == 0) {
            Messages.showWarningDialog("No repositories were selected", MiriUtils.TITLE);
            return false;
        }

        List<String> failures = new ArrayList<>();
        ProgressManager.getInstance().runProcessWithProgressSynchronously(new FixedStepsProgressTask(selectedRepos.size() + 1) {
            @Override
            protected void execute() {
                for (String repoName : selectedRepos) {
                    try {
                        nextStep("Deleting branch " + branchName + " in " + repoName);
                        assistant.deleteBranch(repoName, branchName);
                    } catch (IOException ex) {
                        failures.add(repoName + ": " + ex.toString());
                    }
                }
            }

        }, "Deleting branch " + branchName, false, project);
        if (failures.isEmpty()) {
            Messages.showInfoMessage("Deleted branch " + branchName, MiriUtils.TITLE);
        } else if (failures.size() == selectedRepos.size()) {
            Messages.showErrorDialog("Failed to delete branch " + branchName + " from all repositories: " + System.lineSeparator() +
                    String.join(System.lineSeparator(), failures), MiriUtils.TITLE);
        } else {
            Messages.showErrorDialog("Failed to delete branch " + branchName + " from the following repositories: " + System.lineSeparator() +
                    String.join(System.lineSeparator(), failures), MiriUtils.TITLE);
        }

        return super.executeOKAction();
    }
}

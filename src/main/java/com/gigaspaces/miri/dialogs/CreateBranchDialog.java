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

public class CreateBranchDialog extends MiriCustomDialog {

    private final String NEW_BRANCH_CAPTION = "New branch name";
    private final String BASE_BRANCH_CAPTION = "Base branch";

    private final GitHubAssistant assistant;
    private final Project project;
    private final JPanel repositoriesPanel = new JPanel();


    public CreateBranchDialog(GitHubAssistant assistant, Project project) {
        super();
        this.assistant = assistant;
        this.project = project;
        setTitle("Create Branch");

        Map<String, Boolean> repos = assistant.getRepos();
        this.repositoriesPanel.setLayout(new GridLayout(repos.size(), 1));
        for (Map.Entry<String, Boolean> entry : repos.entrySet()) {
            repositoriesPanel.add(new JCheckBox(entry.getKey(), entry.getValue()));
        }

        addTextField(NEW_BRANCH_CAPTION);
        addTextField(BASE_BRANCH_CAPTION, "master");
        add("Repositories", repositoriesPanel);

        init();
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
        final String branchName = getRequiredTextField(NEW_BRANCH_CAPTION);
        if (branchName == null) {
            return false;
        }
        final String baseBranch = getRequiredTextField(BASE_BRANCH_CAPTION);
        if (baseBranch == null) {
            return false;
        }
        final List<String> selectedRepos = getSelectedRepositories();
        if (selectedRepos.size() == 0) {
            Messages.showWarningDialog("No repositories were selected", MiriUtils.TITLE);
            return false;
        }

        ProgressManager.getInstance().runProcessWithProgressSynchronously(new FixedStepsProgressTask(selectedRepos.size() + 1) {
            @Override
            protected void execute() {
                for (String repoName : selectedRepos) {
                    try {
                        nextStep("Creating branch " + branchName + " in " + repoName);
                        assistant.createBranch(repoName, branchName, baseBranch);
                    } catch (IOException ex) {
                        String errorMessage = "Failed to create branch " + branchName + " on repo " + repoName + System.lineSeparator() + ex.toString();
                        Messages.showErrorDialog(errorMessage, MiriUtils.TITLE);
                    }
                }
            }

        }, "Creating branch " + branchName, false, project);

        Messages.showInfoMessage("Created branch " + branchName, MiriUtils.TITLE);

        return super.executeOKAction();
    }
}

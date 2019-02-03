package com.gigaspaces.miri.dialogs;

import com.gigaspaces.miri.GitHubAssistant;
import com.gigaspaces.miri.MiriUtils;
import com.gigaspaces.miri.actions.FixedStepsProgressTask;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.Messages;
import org.kohsuke.github.GHBranch;
import org.kohsuke.github.GHCommit;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHTagObject;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CreateBranchDialog extends MiriCustomDialog {

    private final String NEW_BRANCH_CAPTION = "New branch name";
    private final String BRANCH_FROM_CAPTION = "Branch from";

    private final GitHubAssistant assistant;
    private final Project project;
    private final JPanel repositoriesPanel = new JPanel();
    private final ComboBox<ShaType> shaTypeComboBox = new ComboBox<>(ShaType.values());

    private enum ShaType {branch, tag, commit};

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
        add(BRANCH_FROM_CAPTION, shaTypeComboBox, createTextField(BRANCH_FROM_CAPTION, "master"));
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
        final String branchFrom = getRequiredTextField(BRANCH_FROM_CAPTION);
        if (branchFrom == null) {
            return false;
        }
        final List<String> selectedRepos = getSelectedRepositories();
        if (selectedRepos.size() == 0) {
            Messages.showWarningDialog("No repositories were selected", MiriUtils.TITLE);
            return false;
        }

        ShaType shaType = (ShaType) shaTypeComboBox.getSelectedItem();

        List<CreateAction> actions = new ArrayList<>();
        List<String> validationFailures = new ArrayList<>();
        List<String> creationFailures = new ArrayList<>();
        ProgressManager.getInstance().runProcessWithProgressSynchronously(new FixedStepsProgressTask(selectedRepos.size() * 2 + 1) {
            @Override
            protected void execute() {
                for (String repoName : selectedRepos) {
                    nextStep("Locating " + shaType + " " + branchFrom + " in " + repoName);
                    try {
                        GHRepository repository = assistant.getGitHub().getRepository(repoName);
                        String sha = getSha(shaType, branchFrom, repository);
                        if (sha == null) {
                            validationFailures.add(repoName + ": no such " + shaType + " " + branchFrom);
                        } else {
                            actions.add(new CreateAction(repository, sha));
                        }
                    } catch (IOException ex) {
                        validationFailures.add(repoName + ": " + ex.toString());
                    }
                }
                if (validationFailures.isEmpty()) {
                    /*
                    for (CreateAction action : actions) {
                        nextStep("Creating branch " + branchName + " in " + action.repository.getFullName());
                        try {
                            assistant.createBranchFromSha(action.repository, branchName, action.sha);
                        } catch (IOException ex) {
                            creationFailures.add(action.repository.getFullName() + ": " + ex.toString());
                        }
                    }
                    */
                }
            }
        }, "Creating branch " + branchName, false, project);

        boolean executed;
        if (!creationFailures.isEmpty()) {
            Messages.showErrorDialog("Failed to create branch " + branchName + " on the following repositories: " + System.lineSeparator() +
                    String.join(System.lineSeparator(), creationFailures), MiriUtils.TITLE);
            executed = true;
        } else if (!validationFailures.isEmpty()) {
            Messages.showWarningDialog("Validation failed: " + System.lineSeparator() +
                    String.join(System.lineSeparator(), validationFailures), MiriUtils.TITLE);
            executed = false;
        } else {
            Messages.showInfoMessage("Created branch " + branchName, MiriUtils.TITLE);
            executed = true;
        }
        return executed;
    }

    private String getSha(ShaType shaType, String text, GHRepository repository) throws IOException {
        switch (shaType) {
            case branch:
                GHBranch branch = assistant.getBranchIfExists(repository, text);
                return branch != null ? branch.getSHA1() : null;
            case tag:
                GHTagObject tag = assistant.getTagIfExists(repository, text);
                return tag != null ? tag.getObject().getSha() : null;
            case commit:
                GHCommit commit = assistant.getCommitIfExists(repository, text);
                return commit != null ? commit.getSHA1() : null;
            default:
                throw new IllegalStateException("Unsupported sha type: " + shaType);
        }
    }

    private static class CreateAction {
        private final GHRepository repository;
        private final String sha;

        private CreateAction(GHRepository repository, String sha) {
            this.repository = repository;
            this.sha = sha;
        }
    }
}

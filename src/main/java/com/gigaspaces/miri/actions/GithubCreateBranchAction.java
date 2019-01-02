package com.gigaspaces.miri.actions;

import com.gigaspaces.miri.GitHubAssistant;
import com.gigaspaces.miri.MiriUtils;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.ui.Messages;

import java.io.IOException;
import java.util.List;

public class GithubCreateBranchAction extends AnAction {
    public GithubCreateBranchAction() {
        super("Create Branch...");
    }

    @Override
    public void actionPerformed(AnActionEvent e) {

        GitHubAssistant assistant = GitHubAssistant.instance();
        if (assistant == null) {
            Messages.showWarningDialog("Cannot connect to GitHub - please setup an OAuth token", MiriUtils.TITLE);
            return;
        }

        List<String> repos = assistant.getRepos();
        String baseBranch = "master";
        String message = "Base branch: " + baseBranch + System.lineSeparator() +
                "Target repositories:" + System.lineSeparator();
        for (String repo : repos) {
            message += "* " + repo + System.lineSeparator();
        }
        message += System.lineSeparator() + "Which branch would you like to create? ";

        String branchName = Messages.showInputDialog(message, MiriUtils.TITLE, Messages.getQuestionIcon());
        if (branchName != null) {
            ProgressManager.getInstance().runProcessWithProgressSynchronously(new FixedStepsProgressTask(repos.size() + 1) {
                @Override
                protected void execute() {
                    for (String repoName : repos) {
                        try {
                            nextStep("Creating branch " + branchName + " in " + repoName);
                            assistant.createBranch(repoName, branchName, baseBranch);
                        } catch (IOException ex) {
                            String errorMessage = "Failed to create branch " + branchName + " on repo " + repoName + System.lineSeparator() + ex.toString();
                            Messages.showErrorDialog(errorMessage, MiriUtils.TITLE);
                        }
                    }
                }

            }, "Creating branch " + branchName, false, e.getProject());

            Messages.showInfoMessage("Created branch " + branchName, MiriUtils.TITLE);
        }
    }
}

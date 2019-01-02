package com.gigaspaces.miri.actions;

import com.gigaspaces.miri.GitHubAssistant;
import com.gigaspaces.miri.MiriUtils;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.ui.Messages;

import java.io.IOException;
import java.util.List;

public class GithubDeleteBranchAction extends AnAction {
    public GithubDeleteBranchAction() {
        super("Delete Branch...");
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        GitHubAssistant assistant = GitHubAssistant.instance();
        if (assistant == null) {
            Messages.showWarningDialog("Cannot connect to GitHub - please setup an OAuth token", MiriUtils.TITLE);
            return;
        }

        String message = "Target repositories:" + System.lineSeparator();
        final List<String> repos = assistant.getRepos();
        for (String repo : repos) {
            message += "* " + repo + System.lineSeparator();
        }
        message += System.lineSeparator() + "Which branch should I delete? ";

        String branchName = Messages.showInputDialog(message, MiriUtils.TITLE, Messages.getQuestionIcon());
        if (branchName != null) {
            ProgressManager.getInstance().runProcessWithProgressSynchronously(new FixedStepsProgressTask(repos.size() + 1) {
                @Override
                protected void execute() {
                    for (String repoName : repos) {
                        try {
                            nextStep("Deleting branch " + branchName + " in " + repoName);
                            assistant.deleteBranch(repoName, branchName);
                        } catch (IOException ex) {
                            String errorMessage = "Failed to delete branch " + branchName + " on repo " + repoName + System.lineSeparator() + ex.toString();
                            Messages.showErrorDialog(errorMessage, MiriUtils.TITLE);
                        }
                    }
                }

            }, "Deleting branch " + branchName, false, e.getProject());

            Messages.showInfoMessage("Deleted branch " + branchName, MiriUtils.TITLE);
        }
    }

}

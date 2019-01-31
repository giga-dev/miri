package com.gigaspaces.miri.actions;

import com.gigaspaces.miri.GitHubAssistant;
import com.gigaspaces.miri.MiriUtils;
import com.gigaspaces.miri.dialogs.DeleteBranchDialog;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;

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

        DeleteBranchDialog dialog = new DeleteBranchDialog(assistant, e.getProject());
        dialog.show();
    }
}

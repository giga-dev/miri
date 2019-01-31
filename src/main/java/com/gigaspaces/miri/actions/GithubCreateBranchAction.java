package com.gigaspaces.miri.actions;

import com.gigaspaces.miri.GitHubAssistant;
import com.gigaspaces.miri.MiriUtils;
import com.gigaspaces.miri.dialogs.CreateBranchDialog;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;

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

        CreateBranchDialog dialog = new CreateBranchDialog(assistant, e.getProject());
        dialog.show();
    }
}

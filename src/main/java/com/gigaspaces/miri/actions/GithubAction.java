package com.gigaspaces.miri.actions;

import com.gigaspaces.miri.GitHubAssistant;
import com.gigaspaces.miri.MiriUtils;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;

public abstract class GithubAction extends AnAction {
    public GithubAction(String text) {
        super(text);
    }

    public void actionPerformed(AnActionEvent e) {
        GitHubAssistant assistant = GitHubAssistant.instance();
        if (assistant == null) {
            Messages.showWarningDialog("Cannot connect to GitHub - please setup an OAuth token", MiriUtils.TITLE);
            return;
        }

        createDialog(assistant, e.getProject()).show();
    }

    protected abstract DialogWrapper createDialog(GitHubAssistant assistant, Project project);
}

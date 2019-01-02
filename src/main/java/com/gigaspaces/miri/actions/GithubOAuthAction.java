package com.gigaspaces.miri.actions;

import com.gigaspaces.miri.GitHubAssistant;
import com.gigaspaces.miri.MiriUtils;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;

public class GithubOAuthAction extends AnAction {
    public GithubOAuthAction() {
        super("OAuth Token...");
    }
    @Override
    public void actionPerformed(AnActionEvent e) {
        String currValue = GitHubAssistant.getOAuthToken();
        String newValue = Messages.showInputDialog("Enter Github oAuth token", MiriUtils.TITLE, Messages.getQuestionIcon(), currValue, null);
        if (newValue != null) {
            GitHubAssistant.setOAuthToken(newValue);
            Messages.showInfoMessage(newValue.isEmpty() ? "Cleared" : "Updated", MiriUtils.TITLE);
        }
    }
}

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
        String message = "Miri needs an OAuth token to interact with GitHub on your behalf" + System.lineSeparator() +
                "If you don't have one, you can create it at: https://github.com/settings/tokens" + System.lineSeparator() +
                "To learn more visit https://help.github.com/articles/creating-a-personal-access-token-for-the-command-line/" + System.lineSeparator() +
                "Enter Github OAuth token:";
        String newValue = Messages.showInputDialog(message, MiriUtils.TITLE, Messages.getQuestionIcon(), currValue, null);
        if (newValue != null) {
            GitHubAssistant.setOAuthToken(newValue);
            Messages.showInfoMessage(newValue.isEmpty() ? "Cleared" : "Updated", MiriUtils.TITLE);
        }
    }
}

package com.gigaspaces.miri.actions;

import com.gigaspaces.miri.JenkinsAssistant;
import com.gigaspaces.miri.MiriUtils;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;

public class JenkinsAuthAction extends AnAction {
    public JenkinsAuthAction() {
        super("Jenkins credentials...");
    }
    @Override
    public void actionPerformed(AnActionEvent e) {
        String currValue = JenkinsAssistant.getCredentials();
        String message = "Miri needs a credentials to interact with Jenkins on your behalf" + System.lineSeparator() +
                "Enter your credentials in 'username:password' format:";
        String newValue = Messages.showInputDialog(message, MiriUtils.TITLE, Messages.getQuestionIcon(), currValue, null);
        if (newValue != null) {
            JenkinsAssistant.setCredentials(newValue);
            Messages.showInfoMessage(newValue.isEmpty() ? "Cleared" : "Updated", MiriUtils.TITLE);
        }
    }
}

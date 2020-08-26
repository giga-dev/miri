package com.gigaspaces.miri.actions;

import com.gigaspaces.miri.JenkinsAssistant;
import com.gigaspaces.miri.MiriUtils;
import com.gigaspaces.miri.dialogs.JenkinsCreateBuildDialog;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;

import java.util.List;

public class JenkinsCreateBuildAction extends AnAction {
    public JenkinsCreateBuildAction() {
        super("Create continuous build...");
    }

    public void actionPerformed(AnActionEvent e) {
        String credentials = JenkinsAssistant.getCredentials();
        if (credentials.isEmpty()) {
            Messages.showWarningDialog("Cannot connect to Jenkins - please setup the credentials", MiriUtils.TITLE);
            return;
        }

        try {
            List<JenkinsAssistant.JenkinsParameter> jobParameters = JenkinsAssistant.getJobParameters();
            createDialog(jobParameters, e.getProject()).show();
        } catch (Exception ex) {
            Messages.showErrorDialog(ex.getMessage(), MiriUtils.TITLE);
        }
    }

    private DialogWrapper createDialog(List<JenkinsAssistant.JenkinsParameter> jobParameters, Project project) {
        return new JenkinsCreateBuildDialog(jobParameters, project);
    }
}

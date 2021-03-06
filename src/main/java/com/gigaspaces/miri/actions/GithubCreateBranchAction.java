package com.gigaspaces.miri.actions;

import com.gigaspaces.miri.GitHubAssistant;
import com.gigaspaces.miri.dialogs.CreateBranchDialog;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;

public class GithubCreateBranchAction extends GithubAction {
    public GithubCreateBranchAction() {
        super("Create Branch...");
    }

    @Override
    protected DialogWrapper createDialog(GitHubAssistant assistant, Project project) {
        return new CreateBranchDialog(assistant, project);
    }
}

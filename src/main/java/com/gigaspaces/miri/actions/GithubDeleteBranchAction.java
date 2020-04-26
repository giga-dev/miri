package com.gigaspaces.miri.actions;

import com.gigaspaces.miri.GitHubAssistant;
import com.gigaspaces.miri.dialogs.DeleteBranchDialog;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;

public class GithubDeleteBranchAction extends GithubAction {
    public GithubDeleteBranchAction() {
        super("Delete Branch...");
    }

    @Override
    protected DialogWrapper createDialog(GitHubAssistant assistant, Project project) {
        return new DeleteBranchDialog(assistant, project);
    }
}

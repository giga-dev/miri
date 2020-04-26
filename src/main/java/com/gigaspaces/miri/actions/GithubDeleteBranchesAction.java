package com.gigaspaces.miri.actions;

import com.gigaspaces.miri.GitHubAssistant;
import com.gigaspaces.miri.dialogs.DeleteBranchesDialog;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;

public class GithubDeleteBranchesAction extends GithubAction {
    public GithubDeleteBranchesAction() {
        super("Delete Branches...");
    }

    @Override
    protected DialogWrapper createDialog(GitHubAssistant assistant, Project project) {
        return new DeleteBranchesDialog(assistant, project);
    }
}

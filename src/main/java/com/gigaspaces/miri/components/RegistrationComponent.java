package com.gigaspaces.miri.components;

import com.gigaspaces.miri.actions.BrowseAction;
import com.gigaspaces.miri.actions.GithubCreateBranchAction;
import com.gigaspaces.miri.actions.GithubDeleteBranchAction;
import com.gigaspaces.miri.actions.GithubOAuthAction;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.components.ApplicationComponent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class RegistrationComponent implements ApplicationComponent {
    @NotNull
    @Override
    public String getComponentName() {
        return "miri.registration-plugin";
    }

    @Override
    public void initComponent() {
        ((DefaultActionGroup) ActionManager.getInstance().getAction("MainMenu")).add(initMainMenu());
    }

    private AnAction initMainMenu() {
        DefaultActionGroup menu = new DefaultActionGroup("Miri", new ArrayList<>());
        menu.add(new BrowseAction("Dashboard", "http://xap-lab1003:3030/mydashboard"));
        menu.add(new BrowseAction("Newman", "https://xap-newman.gspaces.com:8443/elm/"));
        menu.add(new BrowseAction("Jenkins", "http://xap-lab1003.gspaces.com:8080"));
        menu.add(new BrowseAction("Trello", "https://trello.com/b/lUzTlHkx/xap-scrum"));
        menu.add(initJiraMenu());
        menu.add(initGithubMenu());
        menu.add(initShortcutsMenu());
        return menu;
    }

    private AnAction initShortcutsMenu() {
        DefaultActionGroup menu = new DefaultActionGroup("Shortcuts", new ArrayList<>());
        menu.setPopup(true);
        menu.addSeparator();
        menu.add(new BrowseAction("GigaSpaces Docs (public)", "https://docs.gigaspaces.com"));
        menu.add(new BrowseAction("GigaSpaces Docs (staging)", "https://docs-staging.gigaspaces.com"));
        return menu;
    }

    private AnAction initGithubMenu() {
        DefaultActionGroup menu = new DefaultActionGroup("Github", new ArrayList<>());
        menu.setPopup(true);
        menu.add(new GithubCreateBranchAction());
        menu.add(new GithubDeleteBranchAction());
        menu.addSeparator();
        menu.add(new GithubOAuthAction());
        return menu;
    }

    private AnAction initJiraMenu() {
        DefaultActionGroup menu = new DefaultActionGroup("Jira", new ArrayList<>());
        menu.setPopup(true);
        menu.add(new BrowseAction("InsightEdge", "https://insightedge.atlassian.net/projects/GS"));
        menu.add(new BrowseAction("Docs", "https://insightedge.atlassian.net/projects/DOC"));
        return menu;
    }

}

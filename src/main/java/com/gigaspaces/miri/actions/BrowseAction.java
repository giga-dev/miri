package com.gigaspaces.miri.actions;

import com.gigaspaces.miri.MiriUtils;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

public class BrowseAction extends AnAction {
    private final String url;

    public BrowseAction(String name, String url) {
        super(name);
        this.url = url;
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        MiriUtils.browseTo(url);
    }
}

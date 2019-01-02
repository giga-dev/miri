package com.gigaspaces.miri.components;

import com.gigaspaces.miri.MiriUtils;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.components.ApplicationComponent;
import org.jetbrains.annotations.NotNull;

public class RegistrationComponent implements ApplicationComponent {
    @NotNull
    @Override
    public String getComponentName() {
        return "miri.registration-plugin";
    }

    @Override
    public void initComponent() {
        AnAction mainMenu = MiriUtils.getMainMenu();
        ((DefaultActionGroup) ActionManager.getInstance().getAction("MainMenu")).add(mainMenu);
    }
}

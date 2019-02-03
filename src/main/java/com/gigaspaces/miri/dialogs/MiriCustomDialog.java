package com.gigaspaces.miri.dialogs;

import com.gigaspaces.miri.MiriUtils;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class MiriCustomDialog extends DialogWrapper {
    private final JPanel mainPanel = new JPanel(new GridBagLayout());
    private int currRow;
    private final Map<String, JTextField> textFields = new HashMap<>();

    protected MiriCustomDialog() {
        super(true);
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return mainPanel;
    }

    @Override
    protected void doOKAction() {
        if (getOKAction().isEnabled() && executeOKAction()) {
            super.doOKAction();
        }
    }

    protected boolean executeOKAction() {
        return true;
    }

    protected void addTextField(String caption) {
        add(caption, createTextField(caption, ""));
    }

    protected void addTextField(String caption, String defaultValue) {
        add(caption, createTextField(caption, defaultValue));
    }

    protected JTextField createTextField(String caption, String defaultValue) {
        JTextField textField = new JTextField(defaultValue);
        textFields.put(caption, textField);
        return textField;
    }

    protected String getTextFieldValue(String caption) {
        return textFields.get(caption).getText();
    }

    protected void add(String caption, Component component) {
        GridBagConstraints c = initRowGridConstraints();
        mainPanel.add(new JLabel(caption + ":"), c);
        c.gridx++;
        //c.weightx = 0.7;
        c.gridwidth = 2;
        mainPanel.add(component, c);
    }
    protected void add(String caption, Component component1, Component component2) {
        GridBagConstraints c = initRowGridConstraints();
        mainPanel.add(new JLabel(caption + ":"), c);
        c.gridx++;
        //c.weightx = 0.1;
        mainPanel.add(component1, c);
        c.gridx++;
        c.weightx = 0.6;
        mainPanel.add(component2, c);
    }

    private GridBagConstraints initRowGridConstraints() {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = currRow++;
        c.ipadx = 8;
        c.fill = GridBagConstraints.HORIZONTAL; //natural height, maximum width
        c.anchor = GridBagConstraints.NORTH;
        c.weightx = 0.2;
        return c;
    }

    protected String getRequiredTextField(String caption) {
        String value = getTextFieldValue(caption);
        if (value == null || value.isEmpty()) {
            Messages.showWarningDialog("Required field is empty - " + caption, MiriUtils.TITLE);
            return null;
        }
        return value;
    }
}

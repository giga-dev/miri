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
        addTextField(caption, "");
    }

    protected void addTextField(String caption, String defaultValue) {
        JTextField textField = new JTextField(defaultValue);
        textFields.put(caption, textField);
        add(caption, textField);
    }

    protected String getTextFieldValue(String caption) {
        return textFields.get(caption).getText();
    }


    protected void add(String caption, Component component) {
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL; //natural height, maximum width
        c.gridx = 0;
        c.gridy = currRow++;
        c.weightx = 0.3;
        c.ipadx = 8;
        c.anchor = GridBagConstraints.NORTH;
        mainPanel.add(new JLabel(caption + ":"), c);
        c.gridx++;
        c.weightx = 0.7;
        mainPanel.add(component, c);
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

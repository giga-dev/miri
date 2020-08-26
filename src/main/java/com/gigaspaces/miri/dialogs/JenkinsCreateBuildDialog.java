package com.gigaspaces.miri.dialogs;

import com.gigaspaces.miri.JenkinsAssistant;
import com.gigaspaces.miri.MiriUtils;
import com.gigaspaces.miri.actions.FixedStepsProgressTask;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import git4idea.GitLocalBranch;
import git4idea.repo.GitRepositoryManager;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class JenkinsCreateBuildDialog extends MiriCustomDialog {

    private final String BRANCH_NAME = "BRANCH_NAME";

    private final Project project;

    private final Map<String, Component> components;

    public JenkinsCreateBuildDialog(List<JenkinsAssistant.JenkinsParameter> jobParameters, Project project) {
        super();
        this.project = project;
        this.components = new LinkedHashMap<>();

        setTitle("Create Continuous Build");
        GitLocalBranch currentBranch = GitRepositoryManager.getInstance(project).getRepositories().get(0).getCurrentBranch();
        String currentBranchName = currentBranch == null ? "" : currentBranch.getName();

        for (JenkinsAssistant.JenkinsParameter parameter : jobParameters) {
            if (parameter.getType() == JenkinsAssistant.ParameterType.BOOLEAN) {
                JCheckBox component = new JCheckBox(parameter.getName(), (boolean) parameter.getDefaultValue());
                components.put(parameter.getName(), component);
            } else {
                JTextField component = createTextField(parameter.getName(), parameter.getName().equalsIgnoreCase(BRANCH_NAME) ? currentBranchName : parameter.getDefaultValue().toString());
                components.put(parameter.getName(), component);

            }
        }

        components.forEach(this::add);
        setOKButtonText("Build");
        init();
    }

    private List<BuildParameter> getBuildParameters() {
        List<BuildParameter> result = new ArrayList<>();


        components.forEach((name, component) -> {
            String value;
            if (component instanceof JCheckBox) {
                JCheckBox checkBox = (JCheckBox) component;
                value = String.valueOf(checkBox.isSelected());
            } else {
                JTextField comp = (JTextField) component;
                value = comp.getText();
            }

            BuildParameter buildParameter = new BuildParameter(name, value);
            result.add(buildParameter);
        });
        return result;
    }

    @Override
    protected boolean executeOKAction() {
        final String branchName = getRequiredTextField(BRANCH_NAME);
        if (branchName == null) {
            return false;
        }

        List<BuildParameter> buildParameters = getBuildParameters();

        final Exception[] exception = {null};
        ProgressManager.getInstance().runProcessWithProgressSynchronously(new FixedStepsProgressTask(1) {
            @Override
            protected void execute() {
                try {
                    nextStep("Triggering build in Jenkins");
                    JenkinsAssistant.triggerBuild(buildParameters);
                } catch (Exception e) {
                    exception[0] = e;
                }

            }
        }, "Trigger build on " + branchName + " branch", false, project);

        if (exception[0] != null) {
            Messages.showErrorDialog("Failed to trigger build: " + exception[0].getMessage(), MiriUtils.TITLE);
            return false;
        } else {
            Messages.showInfoMessage("Continuous build was triggered for branch: " + branchName, MiriUtils.TITLE);
            return true;
        }
    }

    public static class BuildParameter {
        private final String name;
        private final String value;

        BuildParameter(String name, String value) {
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public String getValue() {
            return value;
        }
    }

}

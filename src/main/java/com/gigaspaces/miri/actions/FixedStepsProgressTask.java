package com.gigaspaces.miri.actions;

import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;

public abstract class FixedStepsProgressTask implements Runnable {

    private final double steps;
    private double currStep;
    private ProgressIndicator indicator;

    protected FixedStepsProgressTask(int steps) {
        this.steps = steps;
    }

    @Override
    public void run() {
        this.indicator =  ProgressManager.getInstance().getProgressIndicator();
        execute();
        indicator.setFraction(1);
    }

    protected void nextStep(String text) {
        indicator.setText(text);
        currStep++;
        indicator.setFraction(currStep / steps);
    }

    protected abstract void execute();
}

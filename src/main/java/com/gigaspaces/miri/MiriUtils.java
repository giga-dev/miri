package com.gigaspaces.miri;

import com.intellij.openapi.ui.Messages;

import java.awt.*;
import java.net.URI;

public class MiriUtils {
    public static final String TITLE = "Miri";

    public static void browseTo(String url) {
        try {
            Desktop.getDesktop().browse(URI.create(url));
        } catch (Exception e) {
            e.printStackTrace();
            Messages.showMessageDialog("Failed to browse to url [" + url + "]", "Miri", Messages.getErrorIcon());
        }
    }
}

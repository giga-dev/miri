package com.gigaspaces.miri;

import com.gigaspaces.miri.actions.BrowseAction;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.ui.Messages;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.awt.Desktop;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class MiriUtils {
    public static final String TITLE = "Miri";
    public static final String CONFIG_URL = "https://github.com/giga-dev/miri/raw/master/miri-config.xml";

    public static void browseTo(String url) {
        try {
            Desktop.getDesktop().browse(URI.create(url));
        } catch (Exception e) {
            e.printStackTrace();
            Messages.showMessageDialog("Failed to browse to url [" + url + "]", "Miri", Messages.getErrorIcon());
        }
    }

    public static Document loadXml(String url)  {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        try {
            return factory.newDocumentBuilder().parse(new URL(url).openStream());
        } catch (Exception e) {
            throw new RuntimeException("Failed to load xml from " + url, e);
        }
    }

    public static Map<String, Boolean> getRepositories() {
        return loadRepositories(loadXml(CONFIG_URL));
    }

    public static AnAction getMainMenu() {
        return getMainMenu(loadXml(CONFIG_URL));
    }

    public static AnAction getMainMenu(Document xmlDoc) {
        return parseMenu(findChildByName(xmlDoc.getDocumentElement(), "menu"));
    }

    public static DefaultActionGroup parseMenu(Node menuNode) {
        String name = getAttribute(menuNode, "name");
        DefaultActionGroup menu = new DefaultActionGroup(name, new ArrayList<>());
        if (name.equals("GitHub")) {
            menu.add(new com.gigaspaces.miri.actions.GithubCreateBranchAction());
            menu.add(new com.gigaspaces.miri.actions.GithubDeleteBranchAction());
            menu.add(new com.gigaspaces.miri.actions.GithubDeleteBranchesAction());
            menu.addSeparator();
            menu.add(new com.gigaspaces.miri.actions.GithubOAuthAction());
        } else {
            NodeList childNodes = menuNode.getChildNodes();
            for (int i = 0; i < childNodes.getLength(); i++) {
                Node node = childNodes.item(i);
                if (node.getNodeName().equals("menu")) {
                    DefaultActionGroup submenu = parseMenu(node);
                    submenu.setPopup(true);
                    menu.add(submenu);
                } else if (node.getNodeName().equals("action"))
                    menu.add(parseAction(node));
                else if (node.getNodeName().equals("separator"))
                    menu.addSeparator();
            }
        }
        return menu;
    }

    private static AnAction parseAction(Node actionNode) {
        String name = getAttribute(actionNode, "name");
        String url = getAttribute(actionNode, "url");
        if (url != null)
            return new BrowseAction(name, url);
        String className = getAttribute(actionNode, "class");
        if (className != null) {
            try {
                AnAction a;
                return  (AnAction)Class.forName(className).newInstance();
            } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
                throw new RuntimeException("Failed to create action", e);
            }
        }
        return null;
    }

    public static Map<String, Boolean> loadRepositories(Document xmlDoc) {
        Map<String, Boolean> result = new LinkedHashMap<>();
        Node repositoriesNode = findChildByName(xmlDoc.getDocumentElement(), "repositories");
        if (repositoriesNode != null) {
            NodeList childNodes = repositoriesNode.getChildNodes();
            for (int i=0 ; i < childNodes.getLength() ; i++) {
                Node node = childNodes.item(i);
                if (node.getNodeName().equals("repository")) {
                    result.put(getAttribute(node, "name"), getAttributeBoolean(node, "default", true));
                }
            }
        }
        return result;
    }

    public static String getAttribute(Node node, String attName) {
        Node attribute = node.getAttributes().getNamedItem(attName);
        return attribute != null ? attribute.getNodeValue() : null;
    }

    public static boolean getAttributeBoolean(Node node, String attName, boolean defaultValue) {
        String s = getAttribute(node, attName);
        return s != null && !s.isEmpty() ? Boolean.parseBoolean(s) : defaultValue;
    }


    private static Node findChildByName(Element element, String name) {
        NodeList childNodes = element.getChildNodes();
        for (int i=0 ; i < childNodes.getLength() ; i++) {
            Node node = childNodes.item(i);
            if (node.getNodeName().equals(name))
                return node;
        }
        return null;
    }
}

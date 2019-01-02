package com.gigaspaces.miri;

import com.intellij.openapi.ui.Messages;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

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

    public static List<String> getRepositories() {
        return loadRepositories(loadXml(CONFIG_URL));
    }

    public static List<String> loadRepositories(Document xmlDoc) {
        List<String> result = new ArrayList<>();
        Node repositoriesNode = findChildByName(xmlDoc.getDocumentElement(), "repositories");
        if (repositoriesNode != null) {
            NodeList childNodes = repositoriesNode.getChildNodes();
            for (int i=0 ; i < childNodes.getLength() ; i++) {
                Node node = childNodes.item(i);
                if (node.getNodeName().equals("repository")) {
                    result.add(getAttribute(node, "name"));
                }
            }
        }
        return result;
    }

    public static String getAttribute(Node node, String attName) {
        Node attribute = node.getAttributes().getNamedItem(attName);
        return attribute != null ? attribute.getNodeValue() : null;
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

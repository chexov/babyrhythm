package com.babyrhythm;


import static org.apache.log4j.Logger.getLogger;

import java.io.InputStream;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Node;


public class UIVersion {
    private final static Logger log = getLogger(UIVersion.class);

    public static String getVersionString() {
        return "torchoo-" + getVersion();
    }

    public static String getVersion() {
        return versionFromPomXml != null ? versionFromPomXml : getVersionFromPomXml();
    }

    private static String versionFromPomXml = null;

    synchronized static String getVersionFromPomXml() {
        try {
            ClassLoader loader = UIVersion.class.getClassLoader();
            URL url = loader.getResource("META-INF/maven/com.babyrhythm/babyrhythm/pom.xml");
            log.debug(loader + " " + url);
            if (url != null) {
                InputStream stream = url.openStream();
                log.debug(loader + " " + url + " " + stream);
                DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                Node versionNode = builder.parse(stream).getElementsByTagName("version").item(0);
                versionFromPomXml = versionNode.getTextContent();
                IOUtils.closeQuietly(stream);
                log.debug("version " + versionFromPomXml);
                return versionFromPomXml;
            }
        } catch (Exception e) {
            log.error("", e);
        }
        return versionFromPomXml = "devel";
    }

    public static boolean isDevel() {
        return "devel".equals(getVersion());
    }

    public static void main(String[] args) throws Exception {
        System.out.println(getVersionString());
    }

}

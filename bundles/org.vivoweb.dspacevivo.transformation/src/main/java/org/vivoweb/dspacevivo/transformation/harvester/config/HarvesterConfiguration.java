package org.vivoweb.dspacevivo.transformation.harvester.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class HarvesterConfiguration {

    private static String routeConfig = "harvester.conf";

    public static Properties getConf() throws IOException {
        Properties props = new Properties();
        String resourceName = routeConfig;
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        try (InputStream resourceStream = loader.getResourceAsStream(resourceName)) {
            props.load(resourceStream);
        }
        return props;
    }

    public static Properties getConf(String pathConfigFile) throws IOException {
        Properties props = new Properties();
        String resourceName = pathConfigFile;
        try (InputStream resourceStream = new FileInputStream(resourceName);) {
            props.load(resourceStream);
        }
        return props;

    }

}

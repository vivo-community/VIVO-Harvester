package org.vivoweb.harvester.extractdspace.transformation.harvester.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

public class HarvesterConfiguration {

    private static final String routeConfig = "harvester.conf";

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
        try (InputStream resourceStream = Files.newInputStream(Paths.get(pathConfigFile))) {
            props.load(resourceStream);
        }
        return props;

    }

}

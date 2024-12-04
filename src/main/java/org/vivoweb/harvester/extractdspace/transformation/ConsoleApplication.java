package org.vivoweb.harvester.extractdspace.transformation;

import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vivoweb.harvester.extractdspace.transformation.harvester.HarvesterRunner;

public class ConsoleApplication {

    private static final Logger logger = LoggerFactory.getLogger(ConsoleApplication.class);

    public static void main(String[] args) throws IOException {

        logger.info("Testing Dspace Harvester...");
        logger.info("PARAMS...");

        HarvesterRunner runner = new HarvesterRunner();
        if (args.length > 1) {
            logger.info("Configuration file {}", args[0]);
            runner.setPathConfigFile(args[0]);
            logger.info("Output directory {}", args[1]);
            runner.setOutputDir(args[1]);
        } else {
            logger.info("Loading sample configuration (Dspace 7 Demo)");
            logger.info("Showing results in std output");
        }
        logger.info("Init Dspace Harvester...");
        runner.init();
        logger.info("Harvesting Items...");
        runner.harvestItems();
        logger.info("Harvesting Communities...");
        //runner.harvestCommunities();
        logger.info("Harvesting Collections...");
        //runner.harvestCollections();
        logger.info("Harvesting Repositories...");
        //runner.harvestRepositories();
    }

}

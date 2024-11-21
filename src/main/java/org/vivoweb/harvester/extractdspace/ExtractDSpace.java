package org.vivoweb.harvester.extractdspace;

import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vivoweb.dspacevivo.transformation.harvester.HarvesterRunner;

public class ExtractDSpace {

    private static Logger logger = LoggerFactory.getLogger(ExtractDSpace.class);

    public static void main(String[] args) throws IOException {

        logger.info("Testing Dspace Harvester...");
        HarvesterRunner runner = new HarvesterRunner();
        logger.info("Init Dspace Harvester...");
        runner.init();
        logger.info("Harvesting Items...");
        runner.setOutputDir("./data/transform/");
        runner.harvestItems();
        logger.info("Harvesting Communities...");
        //runner.harvestCommunities();
        logger.info("Harvesting Collections...");
        //runner.harvestCollections();
        logger.info("Harvesting Repositories...");
        //runner.harvestRepositories();
        logger.info("... DONE");
    }

}

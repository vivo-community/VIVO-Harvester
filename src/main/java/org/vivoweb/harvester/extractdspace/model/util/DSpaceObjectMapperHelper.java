package org.vivoweb.harvester.extractdspace.model.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.vivoweb.harvester.extractdspace.model.Item;
import org.vivoweb.harvester.extractdspace.model.Repository;

/**
 * @author heon
 */
public class DSpaceObjectMapperHelper {

    public static void main(String[] args) {

    }

    public static String map(Repository repo) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(repo);
    }

    public static String map(Item item) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(item);
    }
}

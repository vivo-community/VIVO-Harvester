package org.vivoweb.dspacevivo.model;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.v3.core.util.ResourceUtils;

public class TestReadWriteItem {

	public static void main(String[] args) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
        String json = ResourceUtils.loadClassResource(TestReadWriteItem.class, "anItem.json");
        ObjectMapper objectMapper = new ObjectMapper();
        
        Item item = objectMapper.readValue(json, Item.class);
        // compact print
        System.out.println(item);

        // pretty print
        String prettyStaff1 = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(item);

        System.out.println(prettyStaff1);//
	}

}

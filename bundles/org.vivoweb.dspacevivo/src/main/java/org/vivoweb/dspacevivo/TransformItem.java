package org.vivoweb.dspacevivo;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.apache.jena.rdf.model.Model;
import org.vivoweb.dspacevivo.model.Item;
import org.vivoweb.dspacevivo.transformation.DspaceItemParser;
import org.vivoweb.dspacevivo.vocab.util.ParserHelper;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.v3.core.util.ResourceUtils;

public class TransformItem {

	public static void main(String[] args) throws IOException {
		try {
			DspaceItemParser parser = new DspaceItemParser();
			String itemJson = IOUtils.toString(System.in, StandardCharsets.UTF_8);
			
			// Convert JSON string from file to Object
	        ObjectMapper objectMapper = new ObjectMapper();
	        
	        Item anItem = objectMapper.readValue(itemJson, Item.class);
			Model repoModel = parser.parse(anItem);
			ParserHelper.dumpStdoutModelNtriples(repoModel);
		} catch (Exception e) {
			System.err.println(e.getMessage());
			System.err.println("Usage: TransformItem < anItem.json ");
		}
	}
}

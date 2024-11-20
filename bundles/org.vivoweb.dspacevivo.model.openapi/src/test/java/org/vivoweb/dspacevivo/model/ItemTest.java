/**
 * 
 */
package org.vivoweb.dspacevivo.model;

import java.io.IOException;

import org.testng.annotations.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.v3.core.util.ResourceUtils;
/**
 * @author heon
 *
 */
public class ItemTest {

	@Test
	public void test() throws IOException {
		
		// Convert JSON string from file to Object
		ObjectMapper mapper = new ObjectMapper();
        String json = ResourceUtils.loadClassResource(getClass(), "anItem.json");
        ObjectMapper objectMapper = new ObjectMapper();
        
        Item item = objectMapper.readValue(json, Item.class);
        // compact print
        System.out.println(item);

        // pretty print
        String prettyStaff1 = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(item);

        System.out.println(prettyStaff1);//
//		// Convert JSON string to Object
//		String jsonInString = "{\"age\":33,\"messages\":[\"msg 1\",\"msg 2\"],\"name\":\"mkyong\"}";
//		User user1 = mapper.readValue(jsonInString, User.class);
//		System.out.println(user1);
//        JsonNode result = Json.mapper().readTree(json);
//      Item anItem = Json.mapper().readValue(result, Item.class);
//        Statement statment = Json.mapper().readValue(json, Statement.class);
//        System.out.println(anItem.toString());
//        final String outputStream = OutputReplacer.OUT.run(new OutputReplacer.Function() {
//            @Override
//            public void run() {
//                Json.prettyPrint(anItem);
//            }
//        });
//        
System.out.println("Done");
	}

}

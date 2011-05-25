package org.vivoweb.harvester.cli.util.args;

import java.util.HashMap;
import java.util.Map;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * A Config Node Parser for simple nodes in this format:
 * <pre>
 *   <paramName>paramValue</paramName>
 * </pre>
 * @author Christopher Haines
 */
public class SimpleNodeParser implements ConfigParser {
	
	@Override
	public Map<String, String> parse(Node node, Map<String, String> args) {
		Map<String, String> map = new HashMap<String, String>();
		NodeList children = node.getChildNodes();
		for(int x = 0; x < children.getLength(); x++) {
			Node child = children.item(x);
			String name = child.getNodeName();
			String value = child.getTextContent();
			map.put(name, value);
		}
		return map;
	}
	
}

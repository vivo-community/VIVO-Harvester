package org.vivoweb.harvester.cli.util.args;

import java.util.HashMap;
import java.util.Map;
import org.vivoweb.harvester.util.XPathAide;
import org.w3c.dom.Node;

/**
 * A Config Node parser that grabs values for fields based on xpath expressions
 * @author Christopher Haines
 */
public class XPathNodeParser implements ConfigParser {
	
	@Override
	public Map<String, String> parse(Node node, Map<String, String> args) {
		Map<String, String> map = new HashMap<String, String>();
		for(String param : args.keySet()) {
			String expression = args.get(param);
			String value = XPathAide.getXPathString(node, expression);
			map.put(param, value);
		}
		return map;
	}
	
}

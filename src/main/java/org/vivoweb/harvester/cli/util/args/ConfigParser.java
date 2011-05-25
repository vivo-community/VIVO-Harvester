package org.vivoweb.harvester.cli.util.args;

import java.util.Map;
import org.w3c.dom.Node;

/**
 * Interface for Config Parsers
 * @author Christopher Haines
 */
public interface ConfigParser {
	/**
	 * Parse the node into a config map
	 * @param node the node to parse
	 * @param args additional parameters to the parser
	 * @return the config map
	 */
	public Map<String, String> parse(Node node, Map<String,String> args);
}

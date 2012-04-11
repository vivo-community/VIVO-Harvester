package org.vivoweb.harvester.util;

import java.io.File;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author kuppuraj
 *
 */
public class XMLGrep {
	
	/**
	 * 
	 */
	protected static Logger log = LoggerFactory.getLogger(XMLGrep.class);
	
	/**
	 * @param src
	 * @param dest
	 * @param xpathExpression
	 */
	@SuppressWarnings("javadoc")
	public static void moveMatchingXML(String src, String dest, String xpathExpression){
		try {
			if(FileAide.isFolder(src)){
				File dir = new File(src);
				File[] files = dir.listFiles();
				
				for(File file : files){
					//String result = XPathTool.getXPathResult(file.getPath(),"//action[. = 'rename']");
					String result = XPathTool.getXPathResult(file.getPath(),xpathExpression);
					if(result!=null && !result.isEmpty()){
						
						if(!FileAide.exists(dest)){
							FileAide.createFolder(dest);
						}
						if(!dest.endsWith("/")) {
							dest+="/"+file.getName();
						}
						else {
							dest+=file.getName();
						}
						FileAide.createFile(dest);
						FileAide.setTextContent(dest, FileAide.getTextContent(file.getPath()));
					}
				}
				
			} else if(FileAide.isFile(src)){
				String result = XPathTool.getXPathResult(src,xpathExpression);
				if(result!=null && !result.isEmpty()){
					if(!FileAide.exists(dest)){
						FileAide.createFile(dest);
					}
					FileAide.setTextContent(dest, FileAide.getTextContent(src));
				}
			}
		} catch(IOException e) {
			log.error(e.getMessage());
		}
	}
	
	public static void main(String args[]){
		try {
			moveMatchingXML("/home/kuppuraj/src/","/home/kuppuraj/dest/","//author[. = 'Kurt Cagle']");
			//System.out.println(XPathTool.getXPathResult("/home/kuppuraj/test.xml","//author[. = 'Kurt Cagle']"));
		} catch(Exception e) {
			log.error(e.getMessage());
		}
	}
	
}

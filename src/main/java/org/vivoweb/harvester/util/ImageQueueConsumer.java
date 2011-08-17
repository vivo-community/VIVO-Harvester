package org.vivoweb.harvester.util;

/**
@author Mayank Saini*/

/// Prerequisite Mesaage xml to Undertand the following Class
/**﻿<?xml version="1.0" encoding="utf-8"?>
<xs:schema xmlns:tns="http://www.bsd.ufl.edu/webservices/IdCard/20100701" elementFormDefault="qualified" targetNamespace="http://www.bsd.ufl.edu/webservices/IdCard/20100701" xmlns:xs="http://www.w3.org/2001/XMLSchema">
  <xs:complexType name="ImageChange">
    <xs:sequence>
      <xs:element name="DateUpdated" type="xs:dateTime" />
      <xs:element name="Image" nillable="true" type="xs:base64Binary" />
      <xs:element name="Ufid" nillable="true" type="xs:string" />
    </xs:sequence>
  </xs:complexType>
  <xs:element name="ImageChange" nillable="true" type="tns:ImageChange" />
</xs:schema>
 */
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.Properties;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.mail.util.ByteArrayDataSource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vivoweb.harvester.util.args.ArgDef;
import org.vivoweb.harvester.util.args.ArgList;
import org.vivoweb.harvester.util.args.ArgParser;
import org.vivoweb.harvester.util.args.UsageException;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import sun.misc.BASE64Decoder;

public class ImageQueueConsumer {
	/**
	 * This Class acts a a consumer to image message on the GatorOne publised queue.It 
	 * consumes the message and write it as a Image of specific type. Gator messages are
	 * of JPEF format 
	 */
	private static Logger log = LoggerFactory.getLogger(ImageQueueConsumer.class);
	private static ByteArrayDataSource bytearr;
	
	/**
	 * This is the Image file  written in the local dir after consuming the message
	 */
	private static File file;
	/**
	 * Input steam to the system.property file
	 */
	private static FileInputStream propFile = null;
	/**
	 * Consumer
	 *  object to consuem the message
	 */
	private static MessageConsumer consumer;
	/**
	 * UFAD username and password
	 */
	private static String userName;
	private static String password;
	private static int maxnum = 1;
	private static Properties P = null;
	/**
	 * ActiveMQ Server URL
	 */
	private static String url;
	private static String subject;
	private static ConnectionFactory connectionFactory;
	
	private static Connection connection;
	private static Session session;
	private static Destination destination;
	/**
	 * Dir name to write the Image files  
	 */
	private static String imagedir;
	/**
	 * Dir name to store the property file   
	 */
	private static String propdir;
	
	/**
	 * Consume all Message from the ActiveMQ queue
	 * @throws JMSException 
	 */
	
	public static void getUpdatesFromQueue() throws JMSException {
		
		//this is for testing purpose only.. so that I can test for 5 images at a time
		//this need to be changed to while (not all messages ) after the final testing
		
		Message message = null;
		for(int i = 0; ((message = consumer.receiveNoWait()) != null) && (i <= maxnum);) {
			
			if(maxnum != 0)
				i++;
			processMessage(message);
			
		}
	}
	
	/**
	 * This function  process the message if message type is ImageChange
	 * @param JMS Message object 
	 * @throws throws JMSException
	 */
	public static void processMessage(Message message) throws JMSException {
		
		String jmsType = message.getStringProperty("type");
		TextMessage text = (TextMessage)message;
		if(text == null)
			log.info("No Text Message Found");
		
		else if(jmsType.equals("ImageChange")) {
			getUfidsAndImages(text.getText()); // this pacsses the Content of the ImageChange TAG to process
		}
		
	}
	
	/**
	 * This fucntion parsed the XML image 
	 * @param Content of the ImageChange TAG to process
	 */
	public static void getUfidsAndImages(String xmlText) {
		
		try {
			DocumentBuilderFactory dbf =
				DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			InputSource is = new InputSource();
			is.setCharacterStream(new StringReader(xmlText));
			
			Document doc = db.parse(is);
			NodeList DateUpdated = doc.getElementsByTagName("DateUpdated");
			Element line1 = (Element)DateUpdated.item(0);
			String date = getCharacterDataFromElement(line1);
			
			NodeList Image = doc.getElementsByTagName("Image");
			Element line2 = (Element)Image.item(0);
			String Image_base_64 = getCharacterDataFromElement(line2);
			
			NodeList Ufid = doc.getElementsByTagName("Ufid");
			Element line3 = (Element)Ufid.item(0);
			String id = getCharacterDataFromElement(line3);
			log.info("Uploading Image Uf ID: " + id + "to Dir :" + imagedir);
			
			
			WriteImageFromBase64(getCharacterDataFromElement(line2), imagedir + id);
			log.info("Image Fetched for UFID:		" + id + "	Uploded Date:		" + date);
			
			
		}
		
		catch(Exception e) {
			log.info(e.toString());
		}
	}
	
	/**
	 * This function get the XML tag values  for example : for Tag "DateUpdated" it will give you the updatedate, for Image Tag it will give 
	 * you the encoded Image String
	 * @param base64String text encoded Image ,path Path to store the Image,This process repeats for every received message
	 * @throwsI OException
	 */
	
	public static String getCharacterDataFromElement(Element e) {
		Node child = e.getFirstChild();
		if(child instanceof CharacterData) {
			CharacterData cd = (CharacterData)child;
			return cd.getData();
		}
		return "?";
	}
	
	/**
	 * This function convert the base64String encoded image to actual Image and store it in specified directory path
	 * @param base64String text encoded Image ,path Path to store the Image,This process repeats for every received message
	 * @throwsI OException
	 */
	
	public static void WriteImageFromBase64(String base64String, String path) throws IOException {
		
		BASE64Decoder decoder = new BASE64Decoder();
		byte[] buf = decoder.decodeBuffer(base64String);
		bytearr = new ByteArrayDataSource(buf, "image/jpeg");
		InputStream in = bytearr.getInputStream();
		File file = new File(path);
		byte buf1[] = new byte[1024];
		int len;
		FileOutputStream fos = new FileOutputStream(file);
		while((len = in.read(buf)) > 0)
			fos.write(buf, 0, len);
		fos.close();
		in.close();
		
	}
	
	/**
	 * This function  create a connection to the activeMQ queue 
	 * @throwsI OException
	 */
	public static MessageConsumer createConnection() {
		connectionFactory = new ActiveMQConnectionFactory(url);
		try {
			connection = connectionFactory.createConnection(userName, password);
			System.out.println("trying to start connection with username:->" + userName + "password:->" + password);
			connection.start();
			
			session = connection.createSession(false,
				Session.AUTO_ACKNOWLEDGE);// Auto Ack is on
			destination = session.createQueue(subject);
			consumer = session.createConsumer(destination);
			return consumer;
			
		} catch(JMSException e) {
			// TODO Auto-generated catch block
			log.info("Connection Failed"+e.toString());
			
			return null;
		}
		
	}
	
	/**
	 * This funtion intialize the class static members { ActiveMQServer,ActiveMQUser,ActiveMQPassword} from the system.properties file
	 * @throwsI OException
	 */
	
	private static void intializeServer() {
		
		file = new File(propdir + "/system.properties");
		try {
			propFile = new FileInputStream(file);
			P = new Properties();
			P.load(propFile);
		} catch(FileNotFoundException e1) { // TODO Auto-generated catch block
			e1.printStackTrace();
		} catch(IOException qe) { // TODO Auto-generated catch block
			qe.printStackTrace();
		}
		
		url = P.getProperty("ActiveMQServer");
		userName = P.getProperty("ActiveMQUser");
		password = P.getProperty("ActiveMQPassword");
		subject = P.getProperty("SUBJECT");
		
	}
	
	public static void main(String... args) throws JMSException {
		
		Exception error = null;
		try {
			InitLog.initLogger(args, getParser());
			log.info(getParser().getAppName() + ": Start");
			new ImageQueueConsumer(args).execute();
		} catch(IllegalArgumentException e) {
			log.error(e.getMessage());
			log.debug("Stacktrace:", e);
			log.info(getParser().getUsage());
			error = e;
		} catch(UsageException e) {
			log.info("Printing Usage:");
			log.info(getParser().getUsage());
			error = e;
		} catch(Exception e) {
			log.error(e.getMessage());
			log.debug("Stacktrace:", e);
			error = e;
		} finally {
			log.info(getParser().getAppName() + ": End");
			if(error != null) {
				System.exit(1);
			}
		}
	}
	
	private static ArgParser getParser() {
		ArgParser parser = new ArgParser("ImageQueueConsumer");
		parser.addArgument(new ArgDef().setShortOption('p').setLongOpt("pathToImageScriptDirectory").withParameter(true, "PATH").setDescription("path to the Image Script Directory").setRequired(true));
		parser.addArgument(new ArgDef().setShortOption('m').setLongOpt("maxFetch").withParameter(true, "MAXFETCH").setDescription("Maximum number if Images that should be fetched from thr queue").setRequired(true));
		return parser;
	}
	
	/**
	 * @throws IOException 
	 * 
	 */
	
	/**
	 * Command line Constructor
	 * @param args command line arguments
	 * @throws UsageException 
	 * @throws IOException 
	 * @throws IllegalArgumentException 
	 */
	private ImageQueueConsumer(String[] args) throws IllegalArgumentException, IOException, UsageException {
		this(getParser().parse(args));
	}
	
	public ImageQueueConsumer(String pathToImageDir, String maxFetched) {
		maxnum = Integer.parseInt(maxFetched.trim());
		this.propdir = pathToImageDir;
		this.imagedir = pathToImageDir + "/images/";
	}
	
	/**
	 * ArgList Constructor
	 * @param argList option set of parsed args
	 */
	private ImageQueueConsumer(ArgList argList) {
		this(argList.get("p"), argList.get("m"));
	}
	
	public void execute() throws IOException {
		intializeServer();
		createConnection();
		try {
			getUpdatesFromQueue();
			connection.close();
		} catch(JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		log.info("Pulled Images from Gator one Server");
	}
	
}

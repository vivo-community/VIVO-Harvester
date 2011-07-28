package org.vivoweb.harvester.util;


/* To run this program you need to do is to import the server certificate and install it in your JDK's KeyStore for more reference check the 
 * Installation Manual
 * Author Mayank Saini
*/
import java.io.File;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashSet;
import java.util.Properties;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.jms.TextMessage;
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
	 * SLF4J Logger
	 */
	private static Logger log = LoggerFactory.getLogger(ImageQueueConsumer.class);
	
	private static File file;
	private static FileInputStream propFile = null;
	private static String userName;
	private static String password;
	private static Properties P = null;
	private static String url ;
	private static String subject ;
	private static ConnectionFactory connectionFactory;
	private static Connection connection;
	private static Session session;
	private static Destination destination;
	private static MessageConsumer consumer;
	private static String imagedir ;
	private static String propdir;
	
	
	public static void getUpdatesFromQueue() throws JMSException {
	
		//this is for testing purpose only.. so that I pull only partial images
		for(int i = 1; i < 2; i++) {
			Message message;
			//while ((message = consumer.receiveNoWait()) != null) {
			
			message = consumer.receiveNoWait();
			processMessage(message);
			
		}
	}
	
	
	// This funciton processes every received messages 
	public static void processMessage(Message message) throws JMSException {
		
		String jmsType = message.getStringProperty("type");
		TextMessage text = (TextMessage)message;
		if(text == null)
			System.out.println("No Text Message Found");
		
		else if(jmsType.equals("ImageChange")) {
			getUfidsAndImages(text.getText());
		}
		
	}
	
	
	/*This function takes the Xml string as a argument , convert it into Xml document , then pass the xml dom to get the Image and Ufid codes. 
	 * 
	 * */
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
			
			System.out.println("Uploading Image Uf ID: "+ id+ "to Dir :"+imagedir);
			WriteImageFromBase64(getCharacterDataFromElement(line2), imagedir + id);
			
			System.out.println("Image Fetched for UFID:		" + id + "	Uploded Date:		" + date);
			
		}

		catch(Exception e) {
			System.err.println(e);
		}
	}
	/* Message contains the Image in the Base64String format, the below fuction converts base64String to byte [] and save it as a 
	image on the disk. This process repeats for every received message 
	*/
	
	public static void WriteImageFromBase64(String base64String, String path) throws IOException {
		
		BASE64Decoder decoder = new BASE64Decoder();
		byte[] buf = decoder.decodeBuffer(base64String);
		File file = new File(path);
		FileOutputStream fos = new FileOutputStream(file);
		fos.write(buf);
		fos.flush();
		fos.close();
	}
	
	//This function get the value of <ufid> & <Image> tag from the xml string 
	public static String getCharacterDataFromElement(Element e) {
		Node child = e.getFirstChild();
		if(child instanceof CharacterData) {
			CharacterData cd = (CharacterData)child;
			return cd.getData();
		}
		return "?";
	}
	
	//This function creates a connection to the ActiveMQ
	public static MessageConsumer createConnection() {
		connectionFactory = new ActiveMQConnectionFactory(url);
		try {
			connection = connectionFactory.createConnection(userName, password);
			connection.start();
			session = connection.createSession(false,
				Session.AUTO_ACKNOWLEDGE);// Auto Ack is on
			destination = session.createQueue(subject);
			consumer = session.createConsumer(destination);
			return consumer;
			
		} catch(JMSException e) {
			// TODO Auto-generated catch block
			
			e.printStackTrace();
			return null;
		}
		
	}
	
	//This funtion intialize the class static members { ActiveMQServer,ActiveMQUser,ActiveMQPassword} from the system.properties file
	private static void intializeServer() {
	
		file = new File(propdir+"/system.properties");
		try {
			propFile = new FileInputStream(file);
			P = new Properties();
			P.load(propFile);
		} catch (FileNotFoundException e1) { // TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException qe) { // TODO Auto-generated catch block
			qe.printStackTrace();
		}

		
		url = P.getProperty("ActiveMQServer");
		userName = P.getProperty("ActiveMQUser");
		password = P.getProperty("ActiveMQPassword");
		subject=P.getProperty("SUBJECT");
		

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
			System.out.println(getParser().getUsage());
			error = e;
		} catch(UsageException e) {
			log.info("Printing Usage:");
			System.out.println(getParser().getUsage());
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



public ImageQueueConsumer(String pathToImageDir) 
{
	this.propdir=pathToImageDir;
	this.imagedir = pathToImageDir+"/images/";
	}
/**
 * ArgList Constructor
 * @param argList option set of parsed args
 */
private ImageQueueConsumer(ArgList argList) {
	this(argList.get("p"));
}
public void execute() throws IOException
{
	intializeServer();
	createConnection();
	try {
		getUpdatesFromQueue();
		connection.close();
	} catch(JMSException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	
	log.info("Pulle Images from Gator one Server");
}

}

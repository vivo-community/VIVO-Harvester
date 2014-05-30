/*******************************************************************************
 * Copyright (c) 2010-2011 VIVO Harvester Team. For full list of contributors, please see the AUTHORS file provided.
 * All rights reserved.
 * This program and the accompanying materials are made available under the terms of the new BSD license which accompanies this distribution, and is available at http://www.opensource.org/licenses/bsd-license.html
 ******************************************************************************/
package org.vivoweb.harvester.fetch;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import org.apache.xml.serialize.XMLSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vivoweb.harvester.util.FileAide;
import org.vivoweb.harvester.util.InitLog;
import org.vivoweb.harvester.util.UniversalNamespaceCache;
import org.vivoweb.harvester.util.WebAide;
import org.vivoweb.harvester.util.args.ArgDef;
import org.vivoweb.harvester.util.args.ArgList;
import org.vivoweb.harvester.util.args.ArgParser;
import org.vivoweb.harvester.util.args.UsageException;
import org.vivoweb.harvester.util.repo.RecordHandler;
import org.vivoweb.harvester.util.repo.RecordStreamOrigin;
import org.vivoweb.harvester.util.repo.XMLRecordOutputStream;
import org.w3c.dom.DOMConfiguration;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;

 /** Class for harvesting from XML Data Sources
 * @author jaf30
 */
public class SimpleXMLFetch implements RecordStreamOrigin {
    /**
     * SLF4J Logger
     */
    private static Logger log = LoggerFactory.getLogger(SimpleXMLFetch.class);

    /**
     * The website address of the JSON source without the protocol prefix (No http://)
     */
    private String strAddress;

    /**
     * The record handler to write records to
     */
    private RecordHandler rhOutput;

        /**
     * Namespace for RDF made from this database
     */
    private String uriNS;

    /**
     * mapping of tagnames
     */
    private String tagNames[];

    /**
     * Mapping idField name
     */
    private String idStrings[];


    // local global fields
    private String nodeNS;
    private StringBuffer stringBuffer;

    /*
     * Some getters and setters
     */


    /**
     * @return the strAddress
     */
    public String getStrAddress() {
        return this.strAddress;
    }

    /**
     * @param strAddress the strAddress to set
     */
    public void setStrAddress(String strAddress) {
        this.strAddress = strAddress;
    }

    /**
     * @return the rhOutput
     */
    public RecordHandler getRhOutput() {
        return this.rhOutput;
    }

    /**
     * @param rhOutput the rhOutput to set
     */
    public void setRhOutput(RecordHandler rhOutput) {
        this.rhOutput = rhOutput;
    }

    /**
     * @return the tagNames
     */
    public String[] getTagNames() {
        return this.tagNames;
    }

    /**
     * @param tagNames the tagNames to set
     */
    public void setTagNames(String[] tagNames) {
        this.tagNames = tagNames;
    }

    /**
     * @return the idStrings
     */
    public String[] getIdStrings() {
        return this.idStrings;
    }

    /**
     * @param idStrings the idStrings to set
     */
    public void setIdStrings(String[] idStrings) {
        this.idStrings = idStrings;
    }



    /**
     * @return namespace URI
     */
    public String getUriNS() {
        return this.uriNS;
    }

    /**
     * @param uriNS namespace URI
     */
    public void setUriNS(String uriNS) {
        this.uriNS = uriNS;
    }


    /**
     * the base for each instance's xmlRos
     */
    private static XMLRecordOutputStream xmlRosBase = new XMLRecordOutputStream(new String[]{"record"}, "<?xml version=\"1.0\" encoding=\"UTF-8\"?><harvest>", "</harvest>", ".*?<identifier>(.*?)</identifier>.*?", null);

    /**
     * Constructor
     * @param args command line arguments
     * @throws IOException error connecting to record handler
     * @throws UsageException user requested usage message
     */
    private SimpleXMLFetch(String[] args) throws IOException, UsageException {
        this(getParser().parse(args));
    }

    /**
     * Constructor
     * @param argList parsed argument list
     * @throws IOException error connecting to record handler
     */
    /**
     * @param args list of arguments
     * @throws IOException an exception
     */
    private SimpleXMLFetch(ArgList args) throws IOException {

       this(
          (args.has("u")?args.get("u"):(args.get("f"))),  // URL
          RecordHandler.parseConfig(args.get("o")), // output override
          args.get("n"), // namespace
          args.getAll("t").toArray(new String[]{}), // tag name
          args.getAll("i").toArray(new String[]{}) // unique identifier
       );

    }

    /**
     * Constructor
     * @param address The website address of the repository, without http://
     * @param rhOutput The recordhandler to write to
     * @param uriNS the default namespace
     * @param rootPath The json path for the root node
     * @param tagNames list of node descriptiosn
     * @param idStrings unique id string
     */
    public SimpleXMLFetch(String address, RecordHandler rhOutput, String uriNS,  String tagNames[], String  idStrings[] ) {
        setStrAddress(address);
        setRhOutput(rhOutput);
        setUriNS(uriNS);

        if(tagNames.length > 0 && tagNames[0] != null) {
            this.tagNames = tagNames;
        } else {
            this.tagNames = new String[]{"NODE"};
        }

        if(idStrings.length > 0 && idStrings[0] != null) {
            this.idStrings = idStrings;
        } else {
            this.idStrings = new String[]{"URI"};
        }
    }


    /**
     * Get the ArgParser for this task
     * @return the ArgParser
     */
    private static ArgParser getParser() {
        ArgParser parser = new ArgParser("SimpleXMLFetch");

        parser.addArgument(new ArgDef().setShortOption('o').setLongOpt("output").setDescription("RecordHandler config file path").withParameter(true, "CONFIG_FILE"));
        parser.addArgument(new ArgDef().setShortOption('O').setLongOpt("outputOverride").withParameterValueMap("RH_PARAM", "VALUE").setDescription("override the RH_PARAM of output recordhandler using VALUE").setRequired(false));

        // json harvester specific arguments
        parser.addArgument(new ArgDef().setShortOption('u').setLongOpt("url").setDescription("url which produces json ").withParameter(true, "URL").setRequired(false));
        parser.addArgument(new ArgDef().setShortOption('f').setLongOpt("file").setDescription("file containing json ").withParameter(true, "FALSE").setRequired(false));

        parser.addArgument(new ArgDef().setShortOption('n').setLongOpt("namespaceBase").withParameter(true, "NAMESPACE_BASE").setDescription("the base namespace to use for each node created").setRequired(false));
        parser.addArgument(new ArgDef().setShortOption('t').setLongOpt("tagname").withParameters(true, "TAGNAME").setDescription("an tagname [have multiple -e for more names]").setRequired(false));
        parser.addArgument(new ArgDef().setShortOption('i').setLongOpt("id").withParameters(true, "ID").setDescription("a single id for the json object [have multiple -i for more ids]").setRequired(false));

        return parser;
    }



    /**
     * Builds a  node record namespace
     * @param nodeName the node to build the namespace for
     * @return the namespace
     */
    private String buildNodeRecordNS(String nodeName) {
        return this.uriNS + nodeName;
    }

    /**
     * Builds a table's field description namespace
     * @param nodeName the node to build the namespace for
     * @return the namespace
     */
    private String buildNodeFieldNS(String nodeName) {
        return this.uriNS + "fields/" + nodeName + "/";
    }

    /**
     * Builds a node type description namespace
     * @param nodeName the node to build the namespace for
     * @return the namespace
     */
    private String buildNodeTypeNS(String nodeName) {
        return this.uriNS + "types#" + nodeName;
    }


    /**
     * Executes the task
     * @throws IOException error getting recrords
     */

    public void execute() throws IOException {


        try {
            XMLRecordOutputStream xmlRos = xmlRosBase.clone();
            xmlRos.setRso(this);

            // Get xml contents as String, check for url first then a file
            InputStream stream = null;

            if (this.strAddress.startsWith("http:")) {
               stream = WebAide.getInputStream(this.strAddress);
            } else if (this.strAddress.startsWith("https:")) {
               stream = WebAide.getInputStream(this.strAddress);
            } else {
               stream = FileAide.getInputStream(this.strAddress);
            }

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true); // never forget this!
            factory.setValidating(false);
            InputSource inputSource = new InputSource(stream);
            //inputSource.setEncoding("ISO-8859-1");
            Document doc = factory.newDocumentBuilder().parse(inputSource);

            UniversalNamespaceCache namespaceCache = new UniversalNamespaceCache(doc, Boolean.TRUE);
            Map<String, String> nsMap = namespaceCache.getPrefix2UriMap();

            for (int i=0; i < this.tagNames.length ; i++) {
                String tagname = this.tagNames[i];
                String id = this.idStrings[i];

                NodeList nodes = doc.getElementsByTagName(tagname);

                log.info("Matched this many nodes: "+ nodes.getLength());
                int count = 0;

                for (int j = 0; j < nodes.getLength(); j++) {
                    Node node = nodes.item(j);

                    StringBuilder sb = new StringBuilder();

                    StringBuilder recID = new StringBuilder();
                    recID.append("node_-_");
                    recID.append(String.valueOf(count));

                    //log.trace("Creating RDF for "+name+": "+recID);
                    // Build RDF BEGIN
                    // Header info
                    this.nodeNS = "node-" + tagname;
                    sb = new StringBuilder();
                    sb.append("<?xml version=\"1.0\"?>\n");
                    sb.append("<rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\n");
                    sb.append("         xmlns:");
                    sb.append(this.nodeNS);
                    sb.append("=\"");
                    sb.append(buildNodeFieldNS(tagname));
                    sb.append("\"\n");
                    sb.append("         xml:base=\"");
                    sb.append(buildNodeRecordNS(tagname));
                    sb.append("\"\n");
                    // add namespaces from the namespaces in the root element
                    Iterator iter = nsMap.keySet().iterator();
                    while (iter.hasNext()) {
                        String nsprefix = (String) iter.next();
                        String nsuri = nsMap.get(nsprefix);
                        if (! nsprefix.equals("DEFAULT")) {
                           sb.append("\nxmlns:"+ nsprefix +"=\""+ nsuri +"\"" );
                        }
                    }
                                        // and close
                    sb.append(">\n\n");

                    // Record info BEGIN
                    sb.append("  <rdf:Description rdf:ID=\"");
                    sb.append(recID);
                    sb.append("\">\n");

                    // insert type value
                    sb.append("    <rdf:type rdf:resource=\"");
                    sb.append(buildNodeTypeNS(tagname));
                    sb.append("\"/>\n");
                    //
                    // Now parse the element matched
                    //


                    NodeList childNodes = node.getChildNodes();

                    //log.info("Found this many childnodes: "+ childNodes.getLength());
                    for (int k = 0; k < childNodes.getLength(); k++) {
                       Node childNode = childNodes.item(k);
                       this.stringBuffer = null; // clear string buffer before adding each node
                       if (childNode.getNodeType() != Node.TEXT_NODE ) {
                          String nodeXml = serializeNode(childNode);
                          sb.append(nodeXml);
                       }
                    }

                    // Record info END
                    sb.append("\n  </rdf:Description>\n");

                    // Footer info
                    sb.append("</rdf:RDF>");
                    // Build RDF END

                    // Write RDF to RecordHandler
                    //log.trace("Adding record: " + fixedkey + "_" + recID);
                    //log.trace("data: "+ sb.toString());
                    //log.info("rhOutput: "+ this.rhOutput);
                    //log.info("recID: "+recID);
                    this.rhOutput.addRecord(tagname + "_" + recID, sb.toString(), this.getClass());
                    count++;
                }
            }
        }  catch(Exception e) {
            log.error(e.getMessage());
            e.printStackTrace();
            throw new IOException(e);
        }
    }

    /**
     * @param node
     * @return
     * @throws TransformerFactoryConfigurationError
     * @throws TransformerException
     */
    static String node2String(Node node) throws TransformerFactoryConfigurationError, TransformerException {
        // you may prefer to use single instances of Transformer, and
        // StringWriter rather than create each time. That would be up to your
        // judgement and whether your app is single threaded etc
        StreamResult xmlOutput = new StreamResult(new StringWriter());
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.transform(new DOMSource(node), xmlOutput);
        return xmlOutput.getWriter().toString();
    }


    public String serializeNode(Node node) {
        DOMImplementationRegistry registry = null;
        LSSerializer writer = null;
        String str = new String();

        try {
            registry = DOMImplementationRegistry.newInstance();
            DOMImplementationLS impl = (DOMImplementationLS)registry.getDOMImplementation("LS");
            writer = impl.createLSSerializer();
            DOMConfiguration domConfig = writer.getDomConfig();
            domConfig.setParameter("namespaces",Boolean.TRUE);
            domConfig.setParameter("namespace-declarations", Boolean.TRUE);

            if (domConfig.canSetParameter("xml-declaration", Boolean.FALSE)) {
                domConfig.setParameter("xml-declaration", Boolean.FALSE);
            }
            str = writer.writeToString(node);
        } catch(ClassCastException e) {
            log.error("Class cast exception", e);
            e.printStackTrace();
        } catch(ClassNotFoundException e) {
            log.error("Class not found exception", e);
            e.printStackTrace();
        } catch(InstantiationException e) {
            log.error("Instantiateion exception", e);
            e.printStackTrace();
        } catch(IllegalAccessException e) {
            log.error("IllegalAccess exception", e);
            e.printStackTrace();
        } catch (Exception e) {
            log.error("Exception", e);
            log.info("node: "+node.getNodeName());
            e.printStackTrace();
        }

        return str;
    }

    /* (non-Javadoc)
     * @see org.vivoweb.harvester.util.repo.RecordStreamOrigin#writeRecord(java.lang.String, java.lang.String)
     */
    @Override
    public void writeRecord(String id, String data) throws IOException {
        log.trace("Adding record "+id);
        this.rhOutput.addRecord(id, data, getClass());
    }

    /**
     * @param obj an object
     */
    protected  void logMapObject(Object obj) {
        HashMap<? , ?> mapobject = (HashMap<?, ?>) obj;
        Iterator<?> iter = mapobject.keySet().iterator();
        while (iter.hasNext()) {
           Object keyobj = iter.next();
           Object valobj = mapobject.get(keyobj);
           log.info(keyobj +": "+ valobj);
        }
     }

    /**
     * @param doc
     * @return
     * @throws IOException
     */
    protected String serializeDocToString(Document doc) throws IOException {
        XMLSerializer serializer = new XMLSerializer();
        ByteArrayOutputStream bout = new ByteArrayOutputStream();

        serializer.setOutputByteStream(bout);
        serializer.serialize(doc);
        return bout.toString();
     }

    /**
     * Main method
     * @param args command line arguments
     */
    public static void main(String... args) {
        Exception error = null;
        try {
            InitLog.initLogger(args, getParser());
            log.info(getParser().getAppName() + ": Start");
            new SimpleXMLFetch(args).execute();
        } catch(IllegalArgumentException e) {
            log.error(e.getMessage());
            log.debug("Stacktrace:",e);
            System.out.println(getParser().getUsage());
            error = e;
        } catch(UsageException e) {
            log.info("Printing Usage:");
            System.out.println(getParser().getUsage());
            error = e;
        } catch(Exception e) {
            log.error(e.getMessage());
            log.debug("Stacktrace:",e);
            error = e;
        } finally {
            log.info(getParser().getAppName() + ": End");
            if(error != null) {
               log.error(error.getMessage());
               System.exit(1);
            } else {
               System.exit(0);
            }
        }
    }


}

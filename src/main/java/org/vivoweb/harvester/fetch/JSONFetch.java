/*******************************************************************************
 * Copyright (c) 2010-2011 VIVO Harvester Team. For full list of contributors, please see the AUTHORS file provided.
 * All rights reserved.
 * This program and the accompanying materials are made available under the terms of the new BSD license which accompanies this distribution, and is available at http://www.opensource.org/licenses/bsd-license.html
 ******************************************************************************/
package org.vivoweb.harvester.fetch;

import com.jayway.jsonpath.InvalidPathException;
import com.jayway.jsonpath.JsonPath;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vivoweb.harvester.util.*;
import org.vivoweb.harvester.util.args.ArgDef;
import org.vivoweb.harvester.util.args.ArgList;
import org.vivoweb.harvester.util.args.ArgParser;
import org.vivoweb.harvester.util.args.UsageException;
import org.vivoweb.harvester.util.repo.RecordHandler;
import org.vivoweb.harvester.util.repo.RecordStreamOrigin;
import org.vivoweb.harvester.util.repo.XMLRecordOutputStream;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * Class for harvesting from JSON Data Sources
 * @author Dale Scheppler
 * @author Christopher Haines (hainesc@ctrip.ufl.edu)
 */
public class JSONFetch implements RecordStreamOrigin {
    /**
     * SLF4J Logger
     */
    private static Logger log = LoggerFactory.getLogger(JSONFetch.class);

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
     * mapping of node descriptions
     */
    private String nodeNames[];

    /**
     * Mapping idField name
     */
    private String idStrings[];

    /**
     * The user defined json path strings
     */
    private String pathStrings[];

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
     * @return the nodeNames
     */
    public String[] getNodeNames() {
        return this.nodeNames;
    }

    /**
     * @param nodeNames the nodeNames to set
     */
    public void setNodeNames(String[] nodeNames) {
        this.nodeNames = nodeNames;
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
     * @return the pathStrings
     */
    public String[] getPathStrings() {
        return this.pathStrings;
    }

    /**
     * @param pathStrings the pathStrings to set
     */
    public void setPathStrings(String[] pathStrings) {
        this.pathStrings = pathStrings;
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
    private JSONFetch(String[] args) throws IOException, UsageException {
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
    private JSONFetch(ArgList args) throws IOException {

       this(
          (args.has("u")?args.get("u"):(args.get("f"))),  // URL
          RecordHandler.parseConfig(args.get("o")), // output override
          args.get("n"), // namespace
          args.getAll("d").toArray(new String[]{}), // json object name
          args.getAll("i").toArray(new String[]{}), // unique identifier
          args.getAll("p").toArray(new String[]{})  // path string
       );

    }

    /**
     * Constructor
     * @param address The website address of the repository, without http://
     * @param rhOutput The recordhandler to write to
     * @param uriNS the default namespace
     * @param rootPath The json path for the root node
     * @param nodeNames list of node descriptiosn
     * @param idStrings unique id string
     * @param pathStrings json path strings
     */
    public JSONFetch(String address, RecordHandler rhOutput, String uriNS,  String nodeNames[], String  idStrings[], String pathStrings[]) {
        setStrAddress(address);
        setRhOutput(rhOutput);
        setUriNS(uriNS);

        if(nodeNames.length > 0 && nodeNames[0] != null) {
            this.nodeNames = nodeNames;
        } else {
            this.nodeNames = new String[]{"NODE"};
        }

        if(idStrings.length > 0 && idStrings[0] != null) {
            this.idStrings = idStrings;
        } else {
            this.idStrings = new String[]{"URI"};
        }

        if(pathStrings.length > 0 && pathStrings[0] != null) {
            this.pathStrings = pathStrings;
        } else {
            this.pathStrings = new String[]{"$"};
        }

    }


    /**
     * Get the ArgParser for this task
     * @return the ArgParser
     */
    private static ArgParser getParser() {
        ArgParser parser = new ArgParser("JSONFetch");

        parser.addArgument(new ArgDef().setShortOption('o').setLongOpt("output").setDescription("RecordHandler config file path").withParameter(true, "CONFIG_FILE"));
        parser.addArgument(new ArgDef().setShortOption('O').setLongOpt("outputOverride").withParameterValueMap("RH_PARAM", "VALUE").setDescription("override the RH_PARAM of output recordhandler using VALUE").setRequired(false));

        // json harvester specific arguments
        parser.addArgument(new ArgDef().setShortOption('u').setLongOpt("url").setDescription("url which produces json ").withParameter(true, "URL").setRequired(false));
        parser.addArgument(new ArgDef().setShortOption('f').setLongOpt("file").setDescription("file containing json ").withParameter(true, "FALSE").setRequired(false));

        parser.addArgument(new ArgDef().setShortOption('n').setLongOpt("namespaceBase").withParameter(true, "NAMESPACE_BASE").setDescription("the base namespace to use for each node created").setRequired(false));
        parser.addArgument(new ArgDef().setShortOption('d').setLongOpt("description").withParameters(true, "NAME").setDescription("a descriptive name for the json object [have multiple -d for more names]").setRequired(false));
        parser.addArgument(new ArgDef().setShortOption('i').setLongOpt("id").withParameters(true, "ID").setDescription("a single id for the json object [have multiple -i for more ids]").setRequired(false));
        parser.addArgument(new ArgDef().setShortOption('p').setLongOpt("path").withParameters(true, "PATH").setDescription("a single path for the json object [have multiple -p for more json paths]").setRequired(false));


        return parser;
    }



    /**
     * Builds a json node record namespace
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

        String jsonpath = new String();
//        Configuration conf = Configuration.defaultConfiguration();
//        conf.addOptions(Option.ALWAYS_RETURN_LIST);

        try {
            XMLRecordOutputStream xmlRos = xmlRosBase.clone();
            xmlRos.setRso(this);


            // Get json contents as String, check for url first then a file
            String jsonString = new String();
            if (this.strAddress == null) {
               System.out.println(getParser().getUsage());
               System.exit(1);
            }
            if (this.strAddress.startsWith("http")) {
               log.debug("URL: "+this.strAddress);
               jsonString = WebAide.getURLContents(this.strAddress);
            } else {
               jsonString = FileAide.getTextContent(this.strAddress);
            }
            //log.info(jsonString);

            for (int i=0; i < this.nodeNames.length ; i++) {
                String name = this.nodeNames[i];
                String id = this.idStrings[i];
                jsonpath = this.pathStrings[i];
                List<Object> nodes = new ArrayList<>();
                log.info("Using path: "+ jsonpath);
                JsonPath path = JsonPath.compile(jsonpath);
                log.info("got jsonpath: "+ path.getPath());
                log.debug("type: "+path.read(jsonString).getClass().getSimpleName());

                //        if (path.read(jsonString).getClass().getSimpleName().equals("JSONArray")) {
                //            nodes = path.read(jsonString);
//                } else {
//                    JSONObject tmpObject = path.read(jsonString);
//                    Gson gson = new GsonBuilder().create();
//
//                    JsonObject job = gson.fromJson(tmpObject.toJSONString(), JsonObject.class);
//
//                    log.debug("JSONObject: " + job);
//                    //nodes = Arrays.asList(Arrays.stream(tmpObject).toArray());
//                }

                if (path.read(jsonString).getClass().getSimpleName().equals("JSONObject")) {
                    Iterator objectIterator;
                    JSONObject jsonObject = path.read(jsonString);

                    objectIterator = jsonObject.keySet().iterator();

                    while (objectIterator.hasNext()) {

                        String key = (String) objectIterator.next();
                        Object objVal = jsonObject.get(key);

//                        log.debug("field: "+key);
//                        log.debug("value: "+objVal);
                        JSONObject tmpOpj = new JSONObject();
                        tmpOpj.put(key,objVal);
                        log.debug("adding: "+tmpOpj);
                        nodes.add(tmpOpj);

                    }
                } else {
                    nodes = path.read(jsonString);
                }

                //    log.debug("JSONArray: " +nodes);


//                List<Object> nodes = path.read(jsonString);

//                List<Object> nodes = JsonPath.using(conf).parse(jsonString).read(jsonpath, new TypeReference<List<Object>>() {});

//                TypeRef<List<Object>> typeReference = new TypeRef<List<Object>>() {};
//                ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
//                List<Object> nodes = JsonPath.using(
//                                        conf
//                                        .jsonProvider(new JacksonJsonProvider(mapper))
//                                        .mappingProvider(new JacksonMappingProvider(mapper))
//                        )
//                        .parse(jsonString)
//                        .read(jsonpath, typeReference);

                log.info("name: "+ name);
                log.info("num nodes: " + nodes.size());
                int count = 0;

                for (Object o: nodes) {
                    JSONObject jsonObject = (JSONObject) o;
                    Iterator iter = jsonObject.keySet().iterator();
                    StringBuilder sb = new StringBuilder();

                    //log.info("fixedkey: "+ fixedkey);
                    StringBuilder recID = new StringBuilder();
                    recID.append("node_-_");
                    recID.append(count);

                    log.trace("Creating RDF for "+name+": "+recID);
                    // Build RDF BEGIN
                    // Header info
                    String nodeNS = "node-" + name;
                    sb = new StringBuilder();
                    sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
                    sb.append("<rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\n");
                    sb.append("         xmlns:");
                    sb.append(nodeNS);
                    sb.append("=\"");
                    sb.append(buildNodeFieldNS(name));
                    sb.append("\"\n");
                    sb.append("         xml:base=\"");
                    sb.append(buildNodeRecordNS(name));
                    sb.append("\">\n");

                    // Record info BEGIN
                    sb.append("  <rdf:Description rdf:ID=\"");
                    sb.append(recID);
                    sb.append("\">\n");

                    // insert type value
                    sb.append("    <rdf:type rdf:resource=\"");
                    sb.append(buildNodeTypeNS(name));
                    sb.append("\"/>\n");

                    while (iter.hasNext()) {
                        String key = (String) iter.next();
                        Object val = jsonObject.get(key);
                        if (val == null) {
                            val = "";
                        }
                        //log.info("val type for key: "+key+ ": "+val.getClass().getName());
                        String fixedkey = key
                                .replaceAll(" |/","_")
                                .replaceAll("\\(|\\)","")
                                .replaceAll("/","_");
                        if (!Character.isDigit(fixedkey.charAt(0)) && !fixedkey.equals("abstract_inverted_index")) {
                            // Confident JSON node names contain "Event:A6bdb69a-e51d-42d7-bd25-62ec3c40b7e8"
                            if (fixedkey.contains(":"))
                                fixedkey = fixedkey.substring(fixedkey.indexOf(":")+1);
                            String field = nodeNS + ":" + fixedkey;
                            sb.append(getFieldXml(field, val, fixedkey));
                        }
                    }
                    // Record info END
                    sb.append("  </rdf:Description>\n");

                    // Footer info
                    sb.append("</rdf:RDF>");
                    // Build RDF END

                    // Write RDF to RecordHandler
                    //log.trace("Adding record: " + fixedkey + "_" + recID);
                    //log.trace("data: "+ sb.toString());
                    //log.info("rhOutput: "+ this.rhOutput);
                    //log.info("recID: "+recID);
                    this.rhOutput.addRecord(name + "_" + recID, sb.toString(), this.getClass());
                    count++;
                }
            }
        } catch (InvalidPathException e) {
            log.error("Invalid JsonPath: "+ jsonpath);
        } catch(Exception e) {
            log.error(e.getMessage());
            e.printStackTrace();
            throw new IOException(e);
        }
    }

    public String getFieldXml(String field, Object val, String fixedkey) {
        StringBuffer sb = new StringBuffer();

        // to make it possible to handle confident data
        if (field.contains(":")) {
            field = field.replaceAll("/","_").substring(0,field.indexOf(':'));
        }
//        field = field.replaceAll("/","_").replaceAll(":","_");

        log.debug("val type for field "+ field +": "+val.getClass().getName());
        sb.append("    <");
        sb.append(SpecialEntities.xmlEncode(field));
        sb.append(">");

        // insert field value
        if (val instanceof  JSONArray) {
            log.debug(field+" is an array with "+((JSONArray) val).size()+" elements") ;
            XMLTagIndexing xmlTagIndexing = new XMLTagIndexing();
            xmlTagIndexing.setElementNo(0);
            arrayHandlingV2(val, sb, xmlTagIndexing, fixedkey);
        } if (val instanceof  JSONObject) {
            log.debug(field+" is an object with "+((JSONObject) val).size()+" elements") ;
            objectHandling(val, sb);
        } else if (val instanceof String || val instanceof Integer){
            sb.append(SpecialEntities.xmlEncode(val.toString().trim().replaceAll("\u201D", "'").replaceAll("\u201C","'")));
        }
        // Field END
        sb.append("</");
        sb.append(SpecialEntities.xmlEncode(field));
        sb.append(">\n");
        return sb.toString();
    }

//    private void arrayHandling(Object val, StringBuffer sb) {
//        JSONArray array = (JSONArray) val;
//
//        sb.append("\n");
//        Iterator arrayIterator = array.iterator();
//        Iterator objectIterator;
////        log.debug("val: "+ array);
//
//        while (arrayIterator.hasNext()) {
//            if (!arrayIndexOpen) {
//                arrayIndexOpen = true;
//                sb.append("    <"+ elementNo +">\n");
//            }
//
//            Object obj = arrayIterator.next();
////            log.debug("objtype: "+ obj.getClass().getName());
//            log.debug("val: "+ obj);
//
//            if (obj instanceof JSONArray) {
//                log.debug("there is an JSON Array inside: "+ obj);
////                arrayHandling(obj, sb);
//            } else if (obj instanceof JSONObject) {
//                log.debug("there is an JSON Object inside: "+ obj);
//
//                JSONObject jsonObject = (JSONObject) obj;
//                objectIterator = jsonObject.keySet().iterator();
//
//
//                while (objectIterator.hasNext()) {
//                    String key = (String) objectIterator.next();
//                    Object objVal = jsonObject.get(key);
//                    if (objVal == null) {
//                        objVal = "";
//                    }
//                    String fixedkey = key
//                            .replaceAll(" |/","_")
//                            .replaceAll("\\(|\\)","");
//                    if (!Character.isDigit(fixedkey.charAt(0))) {
//                        String field = fixedkey;
//                        sb.append(getFieldXml(field, objVal));
//                    }
//                }
//
//
//            } else {
////                sb.append("        <"+i+">");
////                sb.append(obj);
////                sb.append("</"+i+">\n");
////                i++;
//            }
//            if (arrayIndexOpen) {
//                sb.append("    </"+ elementNo +">\n");
//                arrayIndexOpen = false;
//                elementNo++;
//            }
//        }
//        sb.append("    ");
//    }

    private void objectHandling(Object val, StringBuffer sb) {
        Iterator objectIterator;

        JSONObject jsonObject = (JSONObject) val;
        objectIterator = jsonObject.keySet().iterator();

        while (objectIterator.hasNext()) {

            String key = (String) objectIterator.next();
            Object objVal = jsonObject.get(key);
            if (objVal == null) {
                objVal = "";
            }

            key = key.replaceAll("/","_")
//                    .replaceAll("\\(","_")
//                    .replaceAll("\\)","_")
//                    .replaceAll("'","_")
//                    .replaceAll(",","_")
                    .replaceAll(" ","_");

            if (!Character.isDigit(key.charAt(0))) {

                log.debug("field: "+key);
//                        sb.append(getTagName(field, objVal));
                sb.append(getFieldXml(key, objVal, key));
            }
        }
    }

    private void arrayHandlingV2(Object val, StringBuffer sb, XMLTagIndexing xmlTagIndexing, String fixedkey) {
        JSONArray array = (JSONArray) val;

        sb.append("\n");
        Iterator arrayIterator = array.iterator();

        log.debug("val: "+ val);

        while (arrayIterator.hasNext()) {
            Object obj = arrayIterator.next();

            if (!xmlTagIndexing.isArrayIndexOpen()) {
                xmlTagIndexing.setArrayIndexOpen();
                String lastChar = fixedkey.substring(fixedkey.length() - 1);
                if (lastChar.equals("s"))
                    xmlTagIndexing.setXmlTagName(StringUtils.chop(fixedkey));
                else
                    xmlTagIndexing.setXmlTagName(fixedkey);
                sb.append("    <"+xmlTagIndexing.getXmlTagName()+"_"+ xmlTagIndexing.getElementNo() +">");
            }

//            log.debug("val: "+ obj);

            if (obj instanceof JSONArray) {
                log.debug("there is an JSON Array inside: "+ obj);
                XMLTagIndexing xmlArrayIndexing = new XMLTagIndexing();
                xmlArrayIndexing.setElementNo(0);

                arrayHandlingV2(val, sb, xmlArrayIndexing, fixedkey);
            } else if (obj instanceof JSONObject) {
                log.debug("there is an JSON Object inside: "+ obj);
                objectHandling(obj, sb);
            }
            else {
                sb.append(obj.toString().replaceAll("&","&amp;"));
            }
            if (xmlTagIndexing.isArrayIndexOpen()) {
                sb.append("</"+xmlTagIndexing.getXmlTagName()+"_"+ xmlTagIndexing.getElementNo() +">\n");
                xmlTagIndexing.increaseElementNo();
                xmlTagIndexing.setArrayIndexClosed();
            }
        }
        sb.append("    ");
    }

    public String getTagName(String field, Object val) {
        StringBuffer sb = new StringBuffer();
        log.debug("val type for tag "+ field +": "+val.getClass().getName());
        sb.append("    <");
        sb.append(SpecialEntities.xmlEncode(field));
        sb.append(">");

        // insert field value
        sb.append(SpecialEntities.xmlEncode(val.toString().trim()));

        // Field END
        sb.append("</");
        sb.append(SpecialEntities.xmlEncode(field));
        sb.append(">\n");
        return sb.toString();
    }


    @Override
    public void writeRecord(String id, String data) throws IOException {
        log.trace("Adding record "+id);
        this.rhOutput.addRecord(id, data, getClass());
    }

    protected  void logMapObject(Object obj) {
        HashMap<? , ?> mapobject = (HashMap) obj;
        Iterator<?> iter = mapobject.keySet().iterator();
        while (iter.hasNext()) {
           Object keyobj = iter.next();
           Object valobj = mapobject.get(keyobj);
           log.info(keyobj +": "+ valobj);
        }
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
            new JSONFetch(args).execute();
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

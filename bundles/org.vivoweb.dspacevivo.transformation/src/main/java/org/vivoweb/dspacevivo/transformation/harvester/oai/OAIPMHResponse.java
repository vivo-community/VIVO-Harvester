package org.vivoweb.dspacevivo.transformation.harvester.oai;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vivoweb.dspacevivo.model.Collection;
import org.vivoweb.dspacevivo.model.Community;
import org.vivoweb.dspacevivo.model.Item;
import org.vivoweb.dspacevivo.model.Repository;
import org.vivoweb.dspacevivo.model.StatementLiteral;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class OAIPMHResponse {

    private static final Logger LOG = LoggerFactory.getLogger(OAIPMHResponse.class);
    private String rawResponse;
    private Document xmlResponse;
    private List<String> setSpec;
    private static String XSLT_FILENAME = "./resources/aoi_dc.xslt";
    private Properties prop;

    public OAIPMHResponse(String rawResponse) {
        this.rawResponse = rawResponse;
        this.setSpec = new ArrayList();
        parse();
    }

    public OAIPMHResponse(String rawResponse, Properties p) {
        this.rawResponse = rawResponse;
        parse();
        this.prop = p;
        this.setSpec = new ArrayList();
    }

    private void parse() {
        this.xmlResponse = Jsoup.parse(rawResponse, "", Parser.xmlParser());
    }

    private static Document parse(String text) {
        return Jsoup.parse(text, "", Parser.xmlParser());
    }

    public Optional<String> getResumptionToken() {
        Optional<String> resumptionToken = Optional.empty();
        Elements elementsByTag = xmlResponse.getElementsByTag("resumptionToken");
        if (!elementsByTag.isEmpty()) {
            if (elementsByTag.size() > 1) {
                LOG.warn("Multiple 'resumptionToken' tags detected, taking the first one.");
            }
            Element xmlTag = elementsByTag.get(0);
            resumptionToken = Optional.of(xmlTag.text().trim());
        }
        return resumptionToken;
    }

    public String getRawResponse() {
        return rawResponse;
    }

    public void setRawResponse(String rawResponse) {
        this.rawResponse = rawResponse;
    }

    public Document getXmlResponse() {
        return xmlResponse;
    }

    public void setXmlResponse(Document xmlResponse) {
        this.xmlResponse = xmlResponse;
    }

    public Collection modelCollection() {
        Collection col = new Collection();
        col.setId("");
        col.setIsPartOfCommunityID(new ArrayList());
        col.setListOfStatementLiterals(new ArrayList());
        col.setUri("");
        col.setUrl("");
        col.setHasItem(new ArrayList());

        return col;
    }

    public Item modelItem(Document doc, Document head) {
        Document result = doc;
        Item resp = new Item();

        resp.setDspaceIsPartOfCollectionID(new ArrayList());
        Elements listh = head.getElementsByTag("header");
        for (Element e : listh.get(0).children()) {
            String htex = e.text();
            String htag = e.tagName();

            if ("identifier".equals(htag)) {
                resp.setId(htex.split(":")[2]);
            } else if ("setSpec".equals(htag)) {
                setSpec.add(htex);
                if (htex.contains("col")) {
                    resp.getDspaceIsPartOfCollectionID().add(htex);
                }
            }

        }

        Elements list = result.getElementsByTag("oai_dc");

        String id = resp.getId();
        String uri = this.prop.getProperty("uriPrefix") + id.replace("/","_");
        resp.setListOfStatementLiterals(new ArrayList());
        for (Element e : list.get(0).children()) {
            String text = e.text();
            String tag = e.tagName();
            String literalType = "xsd:string";
            switch (tag) {
                case "dc:identifier":
                    resp.setUrl(text);
                    /*
                     * Replace last '/' by '_' to define URI
                     */
                    resp.setUri(uri);
//                    StringBuffer sb = new StringBuffer(text);
//                    int index=sb.lastIndexOf("/");    
//                    sb.replace(index,1+index,"_");    
//                    resp.setUri(sb.toString());
                    break;
                case "dc:bundle":
                    if (resp.getDspaceBitstreamURLs() == null){
                        resp.setDspaceBitstreamURLs(Lists.newArrayList());
                    }
                    resp.getDspaceBitstreamURLs().add(text);
                    break;
                case "dc:date":
                    literalType = "xsd:dateTime"; 
                default:
                    StatementLiteral statementLiteral = new StatementLiteral();
                    statementLiteral.setSubjectUri(uri);
                    statementLiteral.setPredicateUri(tag.replace("dc:", "http://purl.org/dc/terms/"));
                    statementLiteral.setObjectLiteral(text);
                    statementLiteral.setLiteralType(literalType);
                    resp.getListOfStatementLiterals().add(statementLiteral);

            }

        }

        return resp;
    }
    public String urlToUri(String url, String id) {
        String pragma = id.replace("/", "_");
        String uri =  url.replaceAll(id, pragma);
        return uri;
    }
    public List<String> getSetSpec() {
        return setSpec;
    }

    public List<Item> modelItems() {
        List<Item> response = Lists.newArrayList();
        Document result = this.xmlResponse;
        Elements list = result.getElementsByTag("record");

        for (Element e : list) {

            Item resp = new Item();
            String id = e.getElementsByTag("identifier").text();
            resp.setId(id);
            Elements eset = e.getElementsByTag("setSpec");
            for (Element sp : eset) {
                String col = sp.getElementsByTag("setSpec").text();
                if (col.contains("col")) {
                    //resp.setDspaceIsPartOfCollectionID(col);
                }

            }
            Element meta = e.getElementsByTag("metadata").first().child(0);
            resp.setListOfStatementLiterals(new ArrayList());
            for (Element s : meta.children()) {

                if ("dc:bundle".equals(s.tagName())) {
                    if (resp.getDspaceBitstreamURLs() == null){
                        resp.setDspaceBitstreamURLs(Lists.newArrayList());
                    }
                    resp.getDspaceBitstreamURLs().add(s.text());
                } else {

                    StatementLiteral statementLiteral = new StatementLiteral();
                    statementLiteral.setSubjectUri(id);
                    statementLiteral.setPredicateUri(s.tagName().replace("dc:", "http://purl.org/dc/terms/"));
                    statementLiteral.setObjectLiteral(s.text());
                    statementLiteral.setLiteralType(null);
                    resp.getListOfStatementLiterals().add(statementLiteral);
                }

            }
            response.add(resp);
            //resp.setDspaceBitstreamURL(rawResponse);
        }

        return response;
    }

    public org.w3c.dom.Document parsexml(String st) throws SAXException, ParserConfigurationException, IOException {
        DocumentBuilderFactory domFact = DocumentBuilderFactory.newInstance();
        domFact.setNamespaceAware(true);
        DocumentBuilder builder = domFact.newDocumentBuilder();

        org.w3c.dom.Document doc = builder.parse(new ByteArrayInputStream(st.getBytes()));
        return doc;

    }

    public List<Item> modelItemsxoai() throws TransformerException, XPathExpressionException, SAXException, ParserConfigurationException, IOException {
        InputStream toInputStream1 = IOUtils.toInputStream(this.rawResponse);
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = builderFactory.newDocumentBuilder();
        org.w3c.dom.Document xmlDocument = builder.parse(toInputStream1);
        return extracttransform(xmlDocument);
    }

    public List<Community> modelCommunity() throws TransformerException, XPathExpressionException, SAXException, ParserConfigurationException, IOException {
        List<Community> lcom = new ArrayList();

        Document doc = this.xmlResponse;
        Elements list = doc.getElementsByTag("set");
        for (Element e : list) {
            Community com = new Community();
            String id = e.getElementsByTag("setSpec").text();
            String name = e.getElementsByTag("setName").text();
            if (id.contains("com_")) {
                com.setId(id);
                String uri = this.prop.getProperty("uriPrefix") + "handle" + id.replace("com_", "/").replace("_", "/");
                com.setUri(uri);
                lcom.add(com);

            }

        }

        return lcom;
    }

    public List<Repository> modelRepository() throws TransformerException, XPathExpressionException, SAXException, ParserConfigurationException, IOException {
        List<Repository> lrepo = new ArrayList();

        Document doc = this.xmlResponse;
        Elements list = doc.getElementsByTag("Identify");
        for (Element e : list) {
            Repository repo = new Repository();
            String uri = e.getElementsByTag("baseURL").text();
            repo.setUri(uri);
            String id = e.getElementsByTag("repositoryIdentifier").text();
            repo.setId(id);
            repo.setListOfStatementLiterals(new ArrayList());
            Elements elements = e.children();
            for (Element echild : elements) {
                String tagname = echild.tagName();
                String textValue = echild.text();

                StatementLiteral statementLiteral = new StatementLiteral();
                statementLiteral.setSubjectUri(uri);
                statementLiteral.setPredicateUri("http://purl.org/dc/terms/" + tagname);
                statementLiteral.setObjectLiteral(textValue);
                statementLiteral.setLiteralType(null);
                repo.getListOfStatementLiterals().add(statementLiteral);

            }

            lrepo.add(repo);

        }

        return lrepo;
    }

    public List<Collection> modelCollections() throws TransformerException, XPathExpressionException, SAXException, ParserConfigurationException, IOException {
        List<Collection> lcol = new ArrayList();

        Document doc = this.xmlResponse;
        Elements list = doc.getElementsByTag("set");
        for (Element e : list) {
            Collection col = new Collection();
            String id = e.getElementsByTag("setSpec").text();
            String name = e.getElementsByTag("setName").text();
            if (id.contains("col_")) {
                col.setId(id);
                String uri = this.prop.getProperty("uriPrefix") + "/handle" + id.replace("col_", "/").replace("_", "/");
                col.setUri(uri);
                lcol.add(col);
            }

        }

        return lcol;
    }

    public List<String> modelItemCollections() throws TransformerException, XPathExpressionException, SAXException, ParserConfigurationException, IOException {
        List<String> litems = new ArrayList();
        Document doc = this.xmlResponse;
        Elements list = doc.getElementsByTag("identifier");
        for (Element e : list) {
            String completeId = e.text();

            String[] parts = completeId.split(":");
            String id = parts[parts.length - 1];
            litems.add(id);

        }

        return litems;
    }

    public List<String> modelSetSpec() throws TransformerException, XPathExpressionException, SAXException, ParserConfigurationException, IOException {
        List<String> lspec = new ArrayList();
        Document doc = this.xmlResponse;
        Elements list = doc.getElementsByTag("header");
        for (Element e : list) {
            Elements listspec = e.getElementsByTag("setSpec");
            for (Element spec : listspec) {
                String idspec = spec.text();
                lspec.add(idspec);
            }

        }

        return lspec;
    }

    public List<Item> extracttransform(org.w3c.dom.Document xmlDocument) throws XPathExpressionException, TransformerException {
        List<Item> resp = Lists.newArrayList();
        XPath xPath = XPathFactory.newInstance().newXPath();
        xPath.setNamespaceContext(new NamespaceContext() {
            @Override
            public String getNamespaceURI(String prefix) {
                switch (prefix) {
                    case "oai20":
                        return "http://www.openarchives.org/OAI/2.0/";
                    case "marc":
                        return "http://www.loc.gov/MARC21/slim";
                    case "oai_cerif_openaire":
                        return "https://www.openaire.eu/cerif-profile/1.1/";
                    case "dc":
                        return "http://purl.org/dc/elements/1.1/";
                    case "aoi_dc":
                        return "http://www.openarchives.org/OAI/2.0/oai_dc/";
                }
                return null;
            }

            @Override
            public String getPrefix(String namespaceURI) {
                return null;
            }

            @Override
            public Iterator getPrefixes(String namespaceURI) {
                return null;
            }
        });

        String expression = "/OAI-PMH/ListRecords/record/metadata/metadata";
        NodeList nodeList = (NodeList) xPath.compile(expression).evaluate(xmlDocument, XPathConstants.NODESET);

        String expressionhead = "/OAI-PMH/ListRecords/record/header";
        NodeList headers = (NodeList) xPath.compile(expressionhead).evaluate(xmlDocument, XPathConstants.NODESET);

        for (int i = 0; i < nodeList.getLength(); i++) {
            Node item = nodeList.item(i);
            String nodeToXML = nodeToXML(item);
            String ApplyXSLT = ApplyXSLT(nodeToXML, "a");
            Item it = modelItem(parse(ApplyXSLT), parse(nodeToXML(headers.item(i))));
            resp.add(it);
        }
        return resp;
    }

    public static String nodeToXML(Node node) throws TransformerException {
        StringWriter sw = new StringWriter();
        Transformer t = TransformerFactory.newInstance().newTransformer();
        t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        t.setOutputProperty(OutputKeys.INDENT, "yes");
        t.transform(new DOMSource(node), new StreamResult(sw));
        return sw.toString();
    }

    public static String ApplyXSLT(String xmlIn, String xsl) throws TransformerConfigurationException, TransformerException {
        //StreamSource xslSource = new StreamSource(new StringReader(xsl));
        StreamSource xmlInSource = new StreamSource(new StringReader(xmlIn));
        Transformer tf = TransformerFactory.newInstance().newTransformer(new StreamSource(new File(XSLT_FILENAME)));
        StringWriter xmlOutWriter = new StringWriter();
        tf.transform(xmlInSource, new StreamResult(xmlOutWriter));
        return xmlOutWriter.toString();
    }

    public void renameNamespaceRecursive(Node node, String namespace) {
        org.w3c.dom.Document document = node.getOwnerDocument();
        if (node.getNodeType() == Node.ELEMENT_NODE) {
            document.renameNode(node, namespace, node.getNodeName());
        }
        NodeList list = node.getChildNodes();
        for (int i = 0; i < list.getLength(); ++i) {
            renameNamespaceRecursive(list.item(i), namespace);
        }
    }

}

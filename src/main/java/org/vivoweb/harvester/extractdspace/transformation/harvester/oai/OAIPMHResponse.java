package org.vivoweb.harvester.extractdspace.transformation.harvester.oai;

import com.google.common.collect.Lists;
import java.io.ByteArrayInputStream;
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
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vivoweb.harvester.extractdspace.model.Collection;
import org.vivoweb.harvester.extractdspace.model.Community;
import org.vivoweb.harvester.extractdspace.model.Item;
import org.vivoweb.harvester.extractdspace.model.Repository;
import org.vivoweb.harvester.extractdspace.model.StatementLiteral;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class OAIPMHResponse {

    private static final Logger LOG = LoggerFactory.getLogger(OAIPMHResponse.class);
    private static final String XSLT_DC_FILENAME = "aoi_dc.xslt";
    private static final String XSLT_DIM_FILENAME = "aoi_dim.xslt";
    private final List<String> setSpec;
    private String rawResponse;
    private Document xmlResponse;
    private Properties prop;

    public OAIPMHResponse(String rawResponse) {
        this.rawResponse = rawResponse;
        this.setSpec = new ArrayList<>();
        parse();
    }

    public OAIPMHResponse(String rawResponse, Properties p) {
        this.rawResponse = rawResponse;
        parse();
        this.prop = p;
        this.setSpec = new ArrayList<>();
    }

    private static Document parse(String text) {
        return Jsoup.parse(text, "", Parser.xmlParser());
    }

    public static String nodeToXML(Node node) throws TransformerException {
        StringWriter sw = new StringWriter();
        Transformer t = TransformerFactory.newInstance().newTransformer();
        t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        t.setOutputProperty(OutputKeys.INDENT, "yes");
        t.transform(new DOMSource(node), new StreamResult(sw));
        return sw.toString();
    }

    private void parse() {
        this.xmlResponse = Jsoup.parse(rawResponse, "", Parser.xmlParser());
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
        col.setIsPartOfCommunityID(new ArrayList<>());
        col.setListOfStatementLiterals(new ArrayList<>());
        col.setUri("");
        col.setUrl("");
        col.setHasItem(new ArrayList<>());

        return col;
    }

    public Item modelItem(Document metadataDoc, Document headerDoc, String metadataFormat) {
        Item item = new Item();
        item.setDspaceIsPartOfCollectionID(new ArrayList<>());
        item.setListOfStatementLiterals(new ArrayList<>());

        // Process header
        processHeader(headerDoc, item);

        // Determine metadata tag
        String metadataTag = determineMetadataTag(metadataFormat);

        // Process metadata
        processMetadata(metadataDoc, metadataTag, item);

        return item;
    }

    private void processHeader(Document headerDoc, Item item) {
        Elements headerElements = headerDoc.getElementsByTag("header");
        if (headerElements.isEmpty()) {
            return;
        }

        for (Element element : headerElements.get(0).children()) {
            String tagName = element.tagName();
            String text = element.text();

            switch (tagName) {
                case "identifier":
                    item.setId(parseIdentifier(text));
                    break;
                case "setSpec":
                    handleSetSpec(text, item);
                    break;
            }
        }
    }

    private String determineMetadataTag(String metadataFormat) {
        return (metadataFormat != null && metadataFormat.trim().equalsIgnoreCase("DIM"))
            ? "dim:dim"
            : "oai_dc";
    }

    private void processMetadata(Document metadataDoc, String metadataTag, Item item) {
        Elements metadataElements = metadataDoc.getElementsByTag(metadataTag);
        if (metadataElements.isEmpty()) {
            return;
        }

        String itemId = item.getId();
        String uriPrefix = this.prop.getProperty("uriPrefix");
        String itemUri = uriPrefix + itemId.replace("/", "_");
        item.setUri(itemUri);

        for (Element element : metadataElements.get(0).children()) {
            String text = element.text();
            String tagName = element.tagName();

            if ("oai_dc".equals(metadataTag)) {
                handleOaiDcMetadata(item, text, tagName, itemUri);
            } else {
                handleDimMetadata(item, element, text, itemUri);
            }
        }
    }

    private void handleSetSpec(String text, Item item) {
        item.getDspaceIsPartOfCollectionID().add(text);
    }

    private String parseIdentifier(String identifier) {
        String[] parts = identifier.split(":");
        return parts.length > 2 ? parts[2] : identifier;
    }

    private void handleOaiDcMetadata(Item item, String text, String tagName, String uri) {
        switch (tagName) {
            case "dc:identifier":
                item.setUrl(text);
                break;
            case "dc:bundle":
                addToBitstreamURLs(item, text);
                break;
            case "dc:date":
                addStatementLiteral(item, uri, tagName, text, "xsd:dateTime", true);
                break;
            default:
                addStatementLiteral(item, uri, tagName, text, "xsd:string", true);
        }
    }

    private void handleDimMetadata(Item item, Element element, String text, String uri) {
        String elementName = element.attr("element");
        String literalType = "xsd:string";

        if ("date".equals(elementName)) {
            literalType = "xsd:dateTime";
        }

        if ("identifier".equals(elementName)) {
            item.setUrl(text);
        } else if ("bundle".equals(elementName)) {
            addToBitstreamURLs(item, text);
        } else {
            addStatementLiteral(item, uri, elementName, text, literalType, false);
        }
    }

    private void addToBitstreamURLs(Item item, String url) {
        if (item.getDspaceBitstreamURLs() == null) {
            item.setDspaceBitstreamURLs(new ArrayList<>());
        }
        item.getDspaceBitstreamURLs().add(url);
    }

    private void addStatementLiteral(Item item, String subjectUri, String predicate,
                                     String objectLiteral, String literalType, boolean isDC) {
        StatementLiteral statement = new StatementLiteral();
        statement.setSubjectUri(subjectUri);

        if (isDC) {
            statement.setPredicateUri(predicate.replace("dc:", "http://purl.org/dc/terms/"));
        } else {
            statement.setPredicateUri("http://purl.org/dc/terms/" + predicate);
        }
        statement.setObjectLiteral(objectLiteral);
        statement.setLiteralType(literalType);

        item.getListOfStatementLiterals().add(statement);
    }

    public String urlToUri(String url, String id) {
        String pragma = id.replace("/", "_");
        String uri = url.replaceAll(id, pragma);
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

            Element meta = e.getElementsByTag("metadata").first().child(0);
            resp.setListOfStatementLiterals(new ArrayList<>());
            for (Element s : meta.children()) {

                if ("dc:bundle".equals(s.tagName())) {
                    if (resp.getDspaceBitstreamURLs() == null) {
                        resp.setDspaceBitstreamURLs(Lists.newArrayList());
                    }
                    resp.getDspaceBitstreamURLs().add(s.text());
                } else {

                    StatementLiteral statementLiteral = new StatementLiteral();
                    statementLiteral.setSubjectUri(id);
                    statementLiteral.setPredicateUri(
                        s.tagName().replace("dc:", "http://purl.org/dc/terms/"));
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

    public org.w3c.dom.Document parsexml(String st)
        throws SAXException, ParserConfigurationException, IOException {
        DocumentBuilderFactory domFact = DocumentBuilderFactory.newInstance();
        domFact.setNamespaceAware(true);
        DocumentBuilder builder = domFact.newDocumentBuilder();

        org.w3c.dom.Document doc = builder.parse(new ByteArrayInputStream(st.getBytes()));
        return doc;

    }

    public List<Item> modelItemsxoai()
        throws TransformerException, XPathExpressionException, SAXException,
        ParserConfigurationException, IOException {
        InputStream toInputStream1 = IOUtils.toInputStream(this.rawResponse);
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = builderFactory.newDocumentBuilder();
        org.w3c.dom.Document xmlDocument = builder.parse(toInputStream1);
        return extracttransform(xmlDocument);
    }

    public List<Community> modelCommunity() {
        List<Community> lcom = new ArrayList<>();

        Document doc = this.xmlResponse;
        Elements list = doc.getElementsByTag("set");
        for (Element e : list) {
            Community com = new Community();
            String id = e.getElementsByTag("setSpec").text();
            String name = e.getElementsByTag("setName").text();
            if (id.contains("com_")) {
                com.setId(id);
                String uri = this.prop.getProperty("uriPrefix") + "handle" +
                    id.replace("com_", "/").replace("_", "/");
                com.setUri(uri);
                lcom.add(com);

            }

        }

        return lcom;
    }

    public List<Repository> modelRepository() {
        List<Repository> lrepo = new ArrayList<>();

        Document doc = this.xmlResponse;
        Elements list = doc.getElementsByTag("Identify");
        for (Element e : list) {
            Repository repo = new Repository();
            String uri = e.getElementsByTag("baseURL").text();
            repo.setUri(uri);
            String id = e.getElementsByTag("repositoryIdentifier").text();
            repo.setId(id);
            repo.setListOfStatementLiterals(new ArrayList<>());
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

    public List<Collection> modelCollections() {
        List<Collection> lcol = new ArrayList<>();

        Document doc = this.xmlResponse;
        Elements list = doc.getElementsByTag("set");
        for (Element e : list) {
            Collection col = new Collection();
            String id = e.getElementsByTag("setSpec").text();
            String name = e.getElementsByTag("setName").text();
            if (id.contains("col_")) {
                col.setId(id);
                String uri = this.prop.getProperty("uriPrefix") + "/handle" +
                    id.replace("col_", "/").replace("_", "/");
                col.setUri(uri);
                lcol.add(col);
            }

        }

        return lcol;
    }

    public List<String> modelItemCollections() {
        List<String> litems = new ArrayList<>();
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

    public List<String> modelSetSpec() {
        List<String> lspec = new ArrayList<>();
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

    public List<Item> extracttransform(org.w3c.dom.Document xmlDocument)
        throws XPathExpressionException, TransformerException {
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

        String metadataFormat = prop.getProperty("metadataFormat");
        if (metadataFormat == null || metadataFormat.isEmpty()) {
            metadataFormat = "DC";
        }

        String xslFilename = null;
        if (metadataFormat.trim().equalsIgnoreCase("DC")) {
            xslFilename = XSLT_DC_FILENAME;
        } else if ((metadataFormat.trim().equalsIgnoreCase("DIM"))) {
            xslFilename = XSLT_DIM_FILENAME;
        } else {
            LOG.error("Unsupported metadata format: {}", metadataFormat);
            System.exit(1);
        }

        String expression = "/OAI-PMH/ListRecords/record/metadata" +
            (xslFilename.equals(XSLT_DC_FILENAME) ? "/metadata" : "");
        NodeList nodeList =
            (NodeList) xPath.compile(expression).evaluate(xmlDocument, XPathConstants.NODESET);

        String expressionHead = "/OAI-PMH/ListRecords/record/header";
        NodeList headers =
            (NodeList) xPath.compile(expressionHead).evaluate(xmlDocument, XPathConstants.NODESET);

        for (int i = 0; i < nodeList.getLength(); i++) {
            Node item = nodeList.item(i);
            String nodeToXML = nodeToXML(item);
            String ApplyXSLT = ApplyXSLT(nodeToXML, xslFilename);
            Item it =
                modelItem(parse(ApplyXSLT), parse(nodeToXML(headers.item(i))), metadataFormat);
            resp.add(it);
        }
        return resp;
    }

    public String ApplyXSLT(String xmlIn, String xsl)
        throws TransformerException {
        StreamSource xmlInSource = new StreamSource(new StringReader(xmlIn));

        InputStream in = this.getClass().getClassLoader().getResourceAsStream(xsl);

        Transformer tf = TransformerFactory.newInstance().newTransformer(new StreamSource(in));
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

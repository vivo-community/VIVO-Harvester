package org.vivoweb.harvester.extractdspace.vocab.vivo;

import org.apache.jena.ontology.DatatypeProperty;
import org.apache.jena.ontology.ObjectProperty;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;

public class VCARD {
    /**
     * <p>The namespace of the vocabulary as a string</p>
     */
    public static final String NS = "http://www.w3.org/2006/vcard/ns#";
    /**
     * <p>The ontology model that holds the vocabulary terms</p>
     */
    private static final OntModel M_MODEL =
        ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM, null);
    /**
     * <p>The namespace of the vocabulary as a resource</p>
     */
    public static final Resource NAMESPACE = M_MODEL.createResource(NS);
    public static final ObjectProperty hasAddress =
        M_MODEL.createObjectProperty("http://www.w3.org/2006/vcard/ns#hasAddress");
    public static final ObjectProperty hasCalendarLink =
        M_MODEL.createObjectProperty("http://www.w3.org/2006/vcard/ns#hasCalendarLink");
    public static final ObjectProperty hasCalendarRequest =
        M_MODEL.createObjectProperty("http://www.w3.org/2006/vcard/ns#hasCalendarRequest");
    public static final ObjectProperty hasCalenderBusy =
        M_MODEL.createObjectProperty("http://www.w3.org/2006/vcard/ns#hasCalenderBusy");
    public static final ObjectProperty hasCategory =
        M_MODEL.createObjectProperty("http://www.w3.org/2006/vcard/ns#hasCategory");
    public static final ObjectProperty hasEmail =
        M_MODEL.createObjectProperty("http://www.w3.org/2006/vcard/ns#hasEmail");
    public static final ObjectProperty hasFormattedName =
        M_MODEL.createObjectProperty("http://www.w3.org/2006/vcard/ns#hasFormattedName");
    public static final ObjectProperty hasGeo =
        M_MODEL.createObjectProperty("http://www.w3.org/2006/vcard/ns#hasGeo");
    public static final ObjectProperty hasInstantMessage =
        M_MODEL.createObjectProperty("http://www.w3.org/2006/vcard/ns#hasInstantMessage");
    public static final ObjectProperty hasKey =
        M_MODEL.createObjectProperty("http://www.w3.org/2006/vcard/ns#hasKey");
    public static final ObjectProperty hasLanguage =
        M_MODEL.createObjectProperty("http://www.w3.org/2006/vcard/ns#hasLanguage");
    public static final ObjectProperty hasLogo =
        M_MODEL.createObjectProperty("http://www.w3.org/2006/vcard/ns#hasLogo");
    /**
     * <p>To include a member in the group this vCard represents</p>
     */
    public static final ObjectProperty hasMember =
        M_MODEL.createObjectProperty("http://www.w3.org/2006/vcard/ns#hasMember");
    public static final ObjectProperty hasName =
        M_MODEL.createObjectProperty("http://www.w3.org/2006/vcard/ns#hasName");
    public static final ObjectProperty hasNickname =
        M_MODEL.createObjectProperty("http://www.w3.org/2006/vcard/ns#hasNickname");
    public static final ObjectProperty hasNote =
        M_MODEL.createObjectProperty("http://www.w3.org/2006/vcard/ns#hasNote");
    public static final ObjectProperty hasOrganizationName =
        M_MODEL.createObjectProperty("http://www.w3.org/2006/vcard/ns#hasOrganizationName");
    public static final ObjectProperty hasOrganizationalUnitName =
        M_MODEL.createObjectProperty("http://www.w3.org/2006/vcard/ns#hasOrganizationalUnitName");
    public static final ObjectProperty hasPhoto =
        M_MODEL.createObjectProperty("http://www.w3.org/2006/vcard/ns#hasPhoto");
    public static final ObjectProperty hasRelated =
        M_MODEL.createObjectProperty("http://www.w3.org/2006/vcard/ns#hasRelated");
    public static final ObjectProperty hasSound =
        M_MODEL.createObjectProperty("http://www.w3.org/2006/vcard/ns#hasSound");
    public static final ObjectProperty hasTelephone =
        M_MODEL.createObjectProperty("http://www.w3.org/2006/vcard/ns#hasTelephone");
    public static final ObjectProperty hasTimeZone =
        M_MODEL.createObjectProperty("http://www.w3.org/2006/vcard/ns#hasTimeZone");
    public static final ObjectProperty hasTitle =
        M_MODEL.createObjectProperty("http://www.w3.org/2006/vcard/ns#hasTitle");
    public static final ObjectProperty hasURL =
        M_MODEL.createObjectProperty("http://www.w3.org/2006/vcard/ns#hasURL");
    public static final DatatypeProperty additionalName =
        M_MODEL.createDatatypeProperty("http://www.w3.org/2006/vcard/ns#additionalName");
    /**
     * <p>The date of marriage, or equivalent, of the object the vCard represents</p>
     */
    public static final DatatypeProperty anniversary =
        M_MODEL.createDatatypeProperty("http://www.w3.org/2006/vcard/ns#anniversary");
    /**
     * <p>To specify the birth date of the object the vCard represents</p>
     */
    public static final DatatypeProperty birthdate =
        M_MODEL.createDatatypeProperty("http://www.w3.org/2006/vcard/ns#birthdate");
    public static final DatatypeProperty calendarBusy =
        M_MODEL.createDatatypeProperty("http://www.w3.org/2006/vcard/ns#calendarBusy");
    public static final DatatypeProperty calendarLink =
        M_MODEL.createDatatypeProperty("http://www.w3.org/2006/vcard/ns#calendarLink");
    public static final DatatypeProperty calendarRequest =
        M_MODEL.createDatatypeProperty("http://www.w3.org/2006/vcard/ns#calendarRequest");
    public static final DatatypeProperty category =
        M_MODEL.createDatatypeProperty("http://www.w3.org/2006/vcard/ns#category");
    public static final DatatypeProperty country =
        M_MODEL.createDatatypeProperty("http://www.w3.org/2006/vcard/ns#country");
    public static final DatatypeProperty email =
        M_MODEL.createDatatypeProperty("http://www.w3.org/2006/vcard/ns#email");
    /**
     * <p>Called Family Name in vCard</p>
     */
    public static final DatatypeProperty familyName =
        M_MODEL.createDatatypeProperty("http://www.w3.org/2006/vcard/ns#familyName");
    public static final DatatypeProperty formattedName =
        M_MODEL.createDatatypeProperty("http://www.w3.org/2006/vcard/ns#formattedName");
    /**
     * <p>To specify the components of the sex and gender identity of the object the
     * vCard represents. To enable other Gender/Sex codes to be used, this dataproperty
     * has range URI. The vCard gender code classes are defined under Code/Gender</p>
     */
    public static final DatatypeProperty gender =
        M_MODEL.createDatatypeProperty("http://www.w3.org/2006/vcard/ns#gender");
    /**
     * <p>Must use the geo URI scheme RFC5870</p>
     */
    public static final DatatypeProperty geo =
        M_MODEL.createDatatypeProperty("http://www.w3.org/2006/vcard/ns#geo");
    /**
     * <p>called Given Name invCard</p>
     */
    public static final DatatypeProperty givenName =
        M_MODEL.createDatatypeProperty("http://www.w3.org/2006/vcard/ns#givenName");
    /**
     * <p>Called Honorific Prefix in vCard</p>
     */
    public static final DatatypeProperty honorificPrefix =
        M_MODEL.createDatatypeProperty("http://www.w3.org/2006/vcard/ns#honorificPrefix");
    public static final DatatypeProperty honorificSuffix =
        M_MODEL.createDatatypeProperty("http://www.w3.org/2006/vcard/ns#honorificSuffix");
    public static final DatatypeProperty instantMessage =
        M_MODEL.createDatatypeProperty("http://www.w3.org/2006/vcard/ns#instantMessage");
    public static final DatatypeProperty key =
        M_MODEL.createDatatypeProperty("http://www.w3.org/2006/vcard/ns#key");
    /**
     * <p>Use 2 char language code from RFC5646</p>
     */
    public static final DatatypeProperty language =
        M_MODEL.createDatatypeProperty("http://www.w3.org/2006/vcard/ns#language");
    public static final DatatypeProperty locality =
        M_MODEL.createDatatypeProperty("http://www.w3.org/2006/vcard/ns#locality");
    public static final DatatypeProperty logo =
        M_MODEL.createDatatypeProperty("http://www.w3.org/2006/vcard/ns#logo");
    public static final DatatypeProperty nickName =
        M_MODEL.createDatatypeProperty("http://www.w3.org/2006/vcard/ns#nickName");
    public static final DatatypeProperty note =
        M_MODEL.createDatatypeProperty("http://www.w3.org/2006/vcard/ns#note");
    public static final DatatypeProperty organizationName =
        M_MODEL.createDatatypeProperty("http://www.w3.org/2006/vcard/ns#organizationName");
    public static final DatatypeProperty organizationalUnitName =
        M_MODEL.createDatatypeProperty("http://www.w3.org/2006/vcard/ns#organizationalUnitName");
    public static final DatatypeProperty photo =
        M_MODEL.createDatatypeProperty("http://www.w3.org/2006/vcard/ns#photo");
    public static final DatatypeProperty postalCode =
        M_MODEL.createDatatypeProperty("http://www.w3.org/2006/vcard/ns#postalCode");
    public static final DatatypeProperty productId =
        M_MODEL.createDatatypeProperty("http://www.w3.org/2006/vcard/ns#productId");
    public static final DatatypeProperty region =
        M_MODEL.createDatatypeProperty("http://www.w3.org/2006/vcard/ns#region");
    public static final DatatypeProperty related =
        M_MODEL.createDatatypeProperty("http://www.w3.org/2006/vcard/ns#related");
    public static final DatatypeProperty revision =
        M_MODEL.createDatatypeProperty("http://www.w3.org/2006/vcard/ns#revision");
    public static final DatatypeProperty role =
        M_MODEL.createDatatypeProperty("http://www.w3.org/2006/vcard/ns#role");
    public static final DatatypeProperty sortAs =
        M_MODEL.createDatatypeProperty("http://www.w3.org/2006/vcard/ns#sortAs");
    public static final DatatypeProperty sound =
        M_MODEL.createDatatypeProperty("http://www.w3.org/2006/vcard/ns#sound");
    public static final DatatypeProperty source =
        M_MODEL.createDatatypeProperty("http://www.w3.org/2006/vcard/ns#source");
    public static final DatatypeProperty streetAddress =
        M_MODEL.createDatatypeProperty("http://www.w3.org/2006/vcard/ns#streetAddress");
    public static final DatatypeProperty telephone =
        M_MODEL.createDatatypeProperty("http://www.w3.org/2006/vcard/ns#telephone");
    public static final DatatypeProperty timeZone =
        M_MODEL.createDatatypeProperty("http://www.w3.org/2006/vcard/ns#timeZone");
    public static final DatatypeProperty title =
        M_MODEL.createDatatypeProperty("http://www.w3.org/2006/vcard/ns#title");
    /**
     * <p>To specify a value that represents a globally unique identifier corresponding
     * to the entity associated with the vCard</p>
     */
    public static final DatatypeProperty uid =
        M_MODEL.createDatatypeProperty("http://www.w3.org/2006/vcard/ns#uid");
    public static final DatatypeProperty url =
        M_MODEL.createDatatypeProperty("http://www.w3.org/2006/vcard/ns#url");
    public static final OntClass Acquaintance =
        M_MODEL.createClass("http://www.w3.org/2006/vcard/ns#Acquaintance");
    /**
     * <p>To specify the components of the delivery address for the vCard object</p>
     */
    public static final OntClass Address =
        M_MODEL.createClass("http://www.w3.org/2006/vcard/ns#Address");
    /**
     * <p>These types are concerned with information related to the delivery addressing
     * or label for the vCard object</p>
     */
    public static final OntClass Addressing =
        M_MODEL.createClass("http://www.w3.org/2006/vcard/ns#Addressing");
    public static final OntClass Agent =
        M_MODEL.createClass("http://www.w3.org/2006/vcard/ns#Agent");
    public static final OntClass Calendar =
        M_MODEL.createClass("http://www.w3.org/2006/vcard/ns#Calendar");
    /**
     * <p>To specify the URI for the busy time associated with the object that the vCard
     * represents. Was called FBURI in vCard</p>
     */
    public static final OntClass CalendarBusy =
        M_MODEL.createClass("http://www.w3.org/2006/vcard/ns#CalendarBusy");
    /**
     * <p>To specify the URI for a calendar associated with the object represented by
     * the vCard. Was called CALURI in vCard.</p>
     */
    public static final OntClass CalendarLink =
        M_MODEL.createClass("http://www.w3.org/2006/vcard/ns#CalendarLink");
    /**
     * <p>To specify the calendar user address [RFC5545] to which a scheduling request
     * [RFC5546] should be sent for the object represented by the vCard. Was called
     * CALADRURI in vCard</p>
     */
    public static final OntClass CalendarRequest =
        M_MODEL.createClass("http://www.w3.org/2006/vcard/ns#CalendarRequest");
    /**
     * <p>To specify application category information about the vCard, also known as
     * tags. This was called CATEGORIES in vCard.</p>
     */
    public static final OntClass Category =
        M_MODEL.createClass("http://www.w3.org/2006/vcard/ns#Category");
    /**
     * <p>Also called mobile telephone</p>
     */
    public static final OntClass Cell = M_MODEL.createClass("http://www.w3.org/2006/vcard/ns#Cell");
    public static final OntClass Child =
        M_MODEL.createClass("http://www.w3.org/2006/vcard/ns#Child");
    /**
     * <p>Contains all the Code related Classes that are used to indicate vCard Types</p>
     */
    public static final OntClass Code = M_MODEL.createClass("http://www.w3.org/2006/vcard/ns#Code");
    public static final OntClass Colleague =
        M_MODEL.createClass("http://www.w3.org/2006/vcard/ns#Colleague");
    /**
     * <p>These properties describe information about how to communicate with the object
     * the vCard represents</p>
     */
    public static final OntClass Communication =
        M_MODEL.createClass("http://www.w3.org/2006/vcard/ns#Communication");
    public static final OntClass Contact =
        M_MODEL.createClass("http://www.w3.org/2006/vcard/ns#Contact");
    public static final OntClass Coresident =
        M_MODEL.createClass("http://www.w3.org/2006/vcard/ns#Coresident");
    public static final OntClass Coworker =
        M_MODEL.createClass("http://www.w3.org/2006/vcard/ns#Coworker");
    public static final OntClass Crush =
        M_MODEL.createClass("http://www.w3.org/2006/vcard/ns#Crush");
    public static final OntClass Date = M_MODEL.createClass("http://www.w3.org/2006/vcard/ns#Date");
    /**
     * <p>To specify the electronic mail address for communication with the object the
     * vCard represents</p>
     */
    public static final OntClass Email =
        M_MODEL.createClass("http://www.w3.org/2006/vcard/ns#Email");
    public static final OntClass Emergency =
        M_MODEL.createClass("http://www.w3.org/2006/vcard/ns#Emergency");
    /**
     * <p>These properties are concerned with additional explanations, such as that
     * related to informational notes or revisions specific to the vCard</p>
     */
    public static final OntClass Explanatory =
        M_MODEL.createClass("http://www.w3.org/2006/vcard/ns#Explanatory");
    public static final OntClass Fax = M_MODEL.createClass("http://www.w3.org/2006/vcard/ns#Fax");
    public static final OntClass Female =
        M_MODEL.createClass("http://www.w3.org/2006/vcard/ns#Female");
    /**
     * <p>Specifies the formatted text corresponding to the name of the object the vCard
     * represents</p>
     */
    public static final OntClass FormattedName =
        M_MODEL.createClass("http://www.w3.org/2006/vcard/ns#FormattedName");
    public static final OntClass Friend =
        M_MODEL.createClass("http://www.w3.org/2006/vcard/ns#Friend");
    public static final OntClass Gender =
        M_MODEL.createClass("http://www.w3.org/2006/vcard/ns#Gender");
    /**
     * <p>Used to indicate global positioning information that is specific to an address</p>
     */
    public static final OntClass Geo = M_MODEL.createClass("http://www.w3.org/2006/vcard/ns#Geo");
    /**
     * <p>These properties are concerned with information associated with geographical
     * positions or regions associated with the object the vCard represents</p>
     */
    public static final OntClass Geographical =
        M_MODEL.createClass("http://www.w3.org/2006/vcard/ns#Geographical");
    /**
     * <p>Defines all the properties required to be a Group of Individuals or Organizations</p>
     */
    public static final OntClass Group =
        M_MODEL.createClass("http://www.w3.org/2006/vcard/ns#Group");
    /**
     * <p>This implies that the property is related to an individual's personal life</p>
     */
    public static final OntClass Home = M_MODEL.createClass("http://www.w3.org/2006/vcard/ns#Home");
    /**
     * <p>These types are used to capture information associated with the identification
     * and naming of the entity associated with the vCard</p>
     */
    public static final OntClass Identification =
        M_MODEL.createClass("http://www.w3.org/2006/vcard/ns#Identification");
    /**
     * <p>Defines all the properties required to be an Individual</p>
     */
    public static final OntClass Individual =
        M_MODEL.createClass("http://www.w3.org/2006/vcard/ns#Individual");
    /**
     * <p>To specify the URI for instant messaging and presence protocol communications
     * with the object the vCard represents. Was called IMPP in vCard.</p>
     */
    public static final OntClass InstantMessage =
        M_MODEL.createClass("http://www.w3.org/2006/vcard/ns#InstantMessage");
    public static final OntClass Key = M_MODEL.createClass("http://www.w3.org/2006/vcard/ns#Key");
    public static final OntClass Kin = M_MODEL.createClass("http://www.w3.org/2006/vcard/ns#Kin");
    /**
     * <p>The parent class for all vCard Objects</p>
     */
    public static final OntClass Kind = M_MODEL.createClass("http://www.w3.org/2006/vcard/ns#Kind");
    /**
     * <p>To specify the language(s) that may be used for contacting the entity associated
     * with the vCard.</p>
     */
    public static final OntClass Language =
        M_MODEL.createClass("http://www.w3.org/2006/vcard/ns#Language");
    /**
     * <p>Defines all the properties required to be a Location</p>
     */
    public static final OntClass Location =
        M_MODEL.createClass("http://www.w3.org/2006/vcard/ns#Location");
    /**
     * <p>To specify a graphic image of a logo associated with the object the vCard
     * represents</p>
     */
    public static final OntClass Logo = M_MODEL.createClass("http://www.w3.org/2006/vcard/ns#Logo");
    public static final OntClass Male = M_MODEL.createClass("http://www.w3.org/2006/vcard/ns#Male");
    public static final OntClass Me = M_MODEL.createClass("http://www.w3.org/2006/vcard/ns#Me");
    public static final OntClass Met = M_MODEL.createClass("http://www.w3.org/2006/vcard/ns#Met");
    public static final OntClass Muse = M_MODEL.createClass("http://www.w3.org/2006/vcard/ns#Muse");
    /**
     * <p>Specifies the components of the name of the object the vCard represents</p>
     */
    public static final OntClass Name = M_MODEL.createClass("http://www.w3.org/2006/vcard/ns#Name");
    public static final OntClass Neighbor =
        M_MODEL.createClass("http://www.w3.org/2006/vcard/ns#Neighbor");
    /**
     * <p>Specifies the text corresponding to the nickname of the object the vCard represents</p>
     */
    public static final OntClass Nickname =
        M_MODEL.createClass("http://www.w3.org/2006/vcard/ns#Nickname");
    public static final OntClass None = M_MODEL.createClass("http://www.w3.org/2006/vcard/ns#None");
    /**
     * <p>To specify supplemental information or a comment that is associated with the
     * vCard</p>
     */
    public static final OntClass Note = M_MODEL.createClass("http://www.w3.org/2006/vcard/ns#Note");
    /**
     * <p>To specify the organizational name associated with the vCardDefines all the
     * properties required to be an Organization</p>
     */
    public static final OntClass Organization =
        M_MODEL.createClass("http://www.w3.org/2006/vcard/ns#Organization");
    public static final OntClass OrganizationName =
        M_MODEL.createClass("http://www.w3.org/2006/vcard/ns#OrganizationName");
    public static final OntClass OrganizationUnitName =
        M_MODEL.createClass("http://www.w3.org/2006/vcard/ns#OrganizationUnitName");
    /**
     * <p>These properties are concerned with information associated with characteristics
     * of the organization or organizational units of the object that the vCard represents</p>
     */
    public static final OntClass Organizational =
        M_MODEL.createClass("http://www.w3.org/2006/vcard/ns#Organizational");
    public static final OntClass Other =
        M_MODEL.createClass("http://www.w3.org/2006/vcard/ns#Other");
    public static final OntClass Pager =
        M_MODEL.createClass("http://www.w3.org/2006/vcard/ns#Pager");
    public static final OntClass Parent =
        M_MODEL.createClass("http://www.w3.org/2006/vcard/ns#Parent");
    /**
     * <p>Specifies an image or photograph information that annotates some aspect of
     * the object the vCard represents</p>
     */
    public static final OntClass Photo =
        M_MODEL.createClass("http://www.w3.org/2006/vcard/ns#Photo");
    /**
     * <p>To specify a relationship between another entity and the entity represented
     * by this vCard</p>
     */
    public static final OntClass Related =
        M_MODEL.createClass("http://www.w3.org/2006/vcard/ns#Related");
    public static final OntClass RelatedType =
        M_MODEL.createClass("http://www.w3.org/2006/vcard/ns#RelatedType");
    public static final OntClass Role = M_MODEL.createClass("http://www.w3.org/2006/vcard/ns#Role");
    /**
     * <p>Contains all the Security related Classes</p>
     */
    public static final OntClass Security =
        M_MODEL.createClass("http://www.w3.org/2006/vcard/ns#Security");
    public static final OntClass Sibling =
        M_MODEL.createClass("http://www.w3.org/2006/vcard/ns#Sibling");
    /**
     * <p>To specify a digital sound content information that annotates some aspect
     * of the vCard. This property is often used to specify the proper pronunciation
     * of the name property value of the vCard</p>
     */
    public static final OntClass Sound =
        M_MODEL.createClass("http://www.w3.org/2006/vcard/ns#Sound");
    public static final OntClass Spouse =
        M_MODEL.createClass("http://www.w3.org/2006/vcard/ns#Spouse");
    public static final OntClass Sweetheart =
        M_MODEL.createClass("http://www.w3.org/2006/vcard/ns#Sweetheart");
    public static final OntClass Telephone =
        M_MODEL.createClass("http://www.w3.org/2006/vcard/ns#Telephone");
    public static final OntClass TelephoneType =
        M_MODEL.createClass("http://www.w3.org/2006/vcard/ns#TelephoneType");
    /**
     * <p>Also called sms telephone</p>
     */
    public static final OntClass Text = M_MODEL.createClass("http://www.w3.org/2006/vcard/ns#Text");
    public static final OntClass TextPhone =
        M_MODEL.createClass("http://www.w3.org/2006/vcard/ns#TextPhone");
    /**
     * <p>Used to indicate time zone information that is specific to a location or address</p>
     */
    public static final OntClass TimeZone =
        M_MODEL.createClass("http://www.w3.org/2006/vcard/ns#TimeZone");
    /**
     * <p>To specify the position or job of the object the vCard represents</p>
     */
    public static final OntClass Title =
        M_MODEL.createClass("http://www.w3.org/2006/vcard/ns#Title");
    /**
     * <p>This is called TYPE in vCard but renamed here to Context for less confusion
     * (with types/class)</p>
     */
    public static final OntClass Type = M_MODEL.createClass("http://www.w3.org/2006/vcard/ns#Type");
    /**
     * <p>To specify a uniform resource locator associated with the object to which
     * the vCard refers. Examples for individuals include personal web sites, blogs,
     * and social networking site identifiers.</p>
     */
    public static final OntClass URL = M_MODEL.createClass("http://www.w3.org/2006/vcard/ns#URL");
    public static final OntClass Unknown =
        M_MODEL.createClass("http://www.w3.org/2006/vcard/ns#Unknown");
    public static final OntClass Video =
        M_MODEL.createClass("http://www.w3.org/2006/vcard/ns#Video");
    public static final OntClass Voice =
        M_MODEL.createClass("http://www.w3.org/2006/vcard/ns#Voice");
    /**
     * <p>This implies that the property is related to an individual's work place</p>
     */
    public static final OntClass Work = M_MODEL.createClass("http://www.w3.org/2006/vcard/ns#Work");

    /**
     * <p>The namespace of the vocabulary as a string</p>
     *
     * @return namespace as String
     * @see #NS
     */
    public static String getURI() {
        return NS;
    }

}

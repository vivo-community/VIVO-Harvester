/*******************************************************************************
 * Copyright (c) 2010 Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams. All rights reserved.
 * This program and the accompanying materials are made available under the terms of the new BSD license which
 * accompanies this distribution, and is available at http://www.opensource.org/licenses/bsd-license.html Contributors:
 * Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams - initial API and implementation
 ******************************************************************************/
package org.vivoweb.harvester.translate;

/**
 * VCARD Translator Translates the VCARD 3.0 Format into the VIVO format The current variables this translator supports:
 * N Name A structured representation of the name of the person, place or thing associated with the vCard object. FN
 * Formatted Name The formatted name string associated with the vCard object PHOTO Photograph An image or photograph of
 * the individual associated with the vCard BDAY Birthday Date of birth of the individual associated with the vCard ADR
 * Delivery Address A structured representation of the physical delivery address for the vCard object LABEL Label
 * Address Addressing label for physical delivery to the person/object associated with the vCard TEL Telephone The
 * canonical number string for a telephone number for telephony communication with the vCard object EMAIL Email The
 * address for electronic mail communication with the vCard object MAILER Email Program (Optional) Type of email program
 * used TZ Time Zone Information related to the standard time zone of the vCard object GEO Global Positioning The
 * property specifies a latitude and longitude TITLE Title Specifies the job title, functional position or function of
 * the individual associated with the vCard object within an organization (V. P. Research and Development) ROLE Role or
 * occupation The role, occupation, or business category of the vCard object within an organization (eg. Executive) LOGO
 * Logo An image or graphic of the logo of the organization that is associated with the individual to which the vCard
 * belongs AGENT Agent Information about another person who will act on behalf of the vCard object. Typically this would
 * be an area administrator, assistant, or secretary for the individual ORG Organization Name The name and optionally
 * the unit(s) of the organization associated with the vCard object. This property is based on the X.520 Organization
 * Name attribute and the X.520 Organization Unit attribute or Organizational unit NOTE Note Specifies supplemental
 * information or a comment that is associated with the vCard REV Last Revision Combination of the calendar date and
 * time of day of the last update to the vCard object SOUND Sound By default, if this property is not grouped with other
 * properties it specifies the pronunciation of the Formatted Name property of the vCard object. URL URL An URL is a
 * representation of an Internet location that can be used to obtain real-time information about the vCard object UID
 * Unique Identifier Specifies a value that represents a persistent, globally unique identifier associated with the
 * object VERSION Version Version of the vCard Specification KEY Public Key
 * @TODO Stephen: complete javadoc overview
 * @TODO Stephen: Complete Translation
 * @TODO Stephen: allow for other versions
 * @author Stephen V. Williams swilliams@ctrip.ufl.edu
 */
public class VCardTranslator {
	// Global Variables
	
	/**
	 * Parse VCard File
	 */
	@SuppressWarnings("unused")
	private void parseFile() {
		// loop through file
		// line in split on :
		// if Key.Equals()
	}
	
	/**
	 * 
	 */
	public void execute() {
		// Nothing yet
	}
	
}

/*******************************************************************************
 * Copyright (c) 2010-2011 VIVO Harvester Team. For full list of contributors, please see the AUTHORS file provided.
 * All rights reserved.
 * This program and the accompanying materials are made available under the terms of the new BSD license which accompanies this distribution, and is available at http://www.opensource.org/licenses/bsd-license.html
 ******************************************************************************/
package org.vivoweb.harvester.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Christopher Haines (hainesc@ctrip.ufl.edu)
 */
public class SpecialEntities {
	/**
	 * html encode mapping
	 */
	private static Map<String, String> htmlEncode = null;
	/**
	 * html decode mapping
	 */
	private static Map<String, String> htmlDecode = null;
	/**
	 * xml encode mapping
	 */
	private static Map<String, String> xmlEncode = null;
	/**
	 * xml decode mapping
	 */
	private static Map<String, String> xmlDecode = null;
	/**
	 * list of accepted characters
	 */
	private static List<Character> acceptedChars = null;
	
	/**
	 * Gets the html encode mapping
	 * @return html encode mapping
	 */
	private static Map<String, String> getHtmlEncode() {
		if(htmlEncode == null) {
			htmlEncode = new HashMap<String, String>();
			htmlEncode.put("&#32;", "&nbsp;");
			htmlEncode.put("&#34;", "&quot;");
			htmlEncode.put("&#38;", "&amp;");
			htmlEncode.put("&#39;", "&apos;");
			htmlEncode.put("&#60;", "&lt;");
			htmlEncode.put("&#62;", "&gt;");
			htmlEncode.put("&#160;", "&nbsp;");// be carefull with this one (non-breaking white space)
			htmlEncode.put("&#161;", "&iexcl;");
			htmlEncode.put("&#162;", "&cent;");
			htmlEncode.put("&#163;", "&pound;");
			htmlEncode.put("&#164;", "&curren;");
			htmlEncode.put("&#165;", "&yen;");
			htmlEncode.put("&#166;", "&brvbar;");
			htmlEncode.put("&#167;", "&sect;");
			htmlEncode.put("&#168;", "&uml;");
			htmlEncode.put("&#169;", "&copy;");
			htmlEncode.put("&#170;", "&ordf;");
			htmlEncode.put("&#171;", "&laquo;");
			htmlEncode.put("&#172;", "&not;");
			htmlEncode.put("&#173;", "&shy;");
			htmlEncode.put("&#174;", "&reg;");
			htmlEncode.put("&#175;", "&macr;");
			htmlEncode.put("&#176;", "&deg;");
			htmlEncode.put("&#177;", "&plusmn;");
			htmlEncode.put("&#178;", "&sup2;");
			htmlEncode.put("&#179;", "&sup3;");
			htmlEncode.put("&#180;", "&acute;");
			htmlEncode.put("&#181;", "&micro;");
			htmlEncode.put("&#182;", "&para;");
			htmlEncode.put("&#183;", "&middot;");
			htmlEncode.put("&#184;", "&cedil;");
			htmlEncode.put("&#185;", "&sup1;");
			htmlEncode.put("&#186;", "&ordm;");
			htmlEncode.put("&#187;", "&raquo;");
			htmlEncode.put("&#188;", "&frac14;");
			htmlEncode.put("&#189;", "&frac12;");
			htmlEncode.put("&#190;", "&frac34;");
			htmlEncode.put("&#191;", "&iquest;");
			htmlEncode.put("&#192;", "&Agrave;");
			htmlEncode.put("&#193;", "&Aacute;");
			htmlEncode.put("&#194;", "&Acirc;");
			htmlEncode.put("&#195;", "&Atilde;");
			htmlEncode.put("&#196;", "&Auml;");
			htmlEncode.put("&#197;", "&Aring;");
			htmlEncode.put("&#198;", "&AElig;");
			htmlEncode.put("&#199;", "&Ccedil;");
			htmlEncode.put("&#200;", "&Egrave;");
			htmlEncode.put("&#201;", "&Eacute;");
			htmlEncode.put("&#202;", "&Ecirc;");
			htmlEncode.put("&#203;", "&Euml;");
			htmlEncode.put("&#204;", "&Igrave;");
			htmlEncode.put("&#205;", "&Iacute;");
			htmlEncode.put("&#206;", "&Icirc;");
			htmlEncode.put("&#207;", "&Iuml;");
			htmlEncode.put("&#208;", "&ETH;");
			htmlEncode.put("&#209;", "&Ntilde;");
			htmlEncode.put("&#210;", "&Ograve;");
			htmlEncode.put("&#211;", "&Oacute;");
			htmlEncode.put("&#212;", "&Ocirc;");
			htmlEncode.put("&#213;", "&Otilde;");
			htmlEncode.put("&#214;", "&Ouml;");
			htmlEncode.put("&#215;", "&times;");
			htmlEncode.put("&#216;", "&Oslash;");
			htmlEncode.put("&#217;", "&Ugrave;");
			htmlEncode.put("&#218;", "&Uacute;");
			htmlEncode.put("&#219;", "&Ucirc;");
			htmlEncode.put("&#220;", "&Uuml;");
			htmlEncode.put("&#221;", "&Yacute;");
			htmlEncode.put("&#222;", "&THORN;");
			htmlEncode.put("&#223;", "&szlig;");
			htmlEncode.put("&#224;", "&agrave;");
			htmlEncode.put("&#225;", "&aacute;");
			htmlEncode.put("&#226;", "&acirc;");
			htmlEncode.put("&#227;", "&atilde;");
			htmlEncode.put("&#228;", "&auml;");
			htmlEncode.put("&#229;", "&aring;");
			htmlEncode.put("&#230;", "&aelig;");
			htmlEncode.put("&#231;", "&ccedil;");
			htmlEncode.put("&#232;", "&egrave;");
			htmlEncode.put("&#233;", "&eacute;");
			htmlEncode.put("&#234;", "&ecirc;");
			htmlEncode.put("&#235;", "&euml;");
			htmlEncode.put("&#236;", "&igrave;");
			htmlEncode.put("&#237;", "&iacute;");
			htmlEncode.put("&#238;", "&icirc;");
			htmlEncode.put("&#239;", "&iuml;");
			htmlEncode.put("&#240;", "&eth;");
			htmlEncode.put("&#241;", "&ntilde;");
			htmlEncode.put("&#242;", "&ograve;");
			htmlEncode.put("&#243;", "&oacute;");
			htmlEncode.put("&#244;", "&ocirc;");
			htmlEncode.put("&#245;", "&otilde;");
			htmlEncode.put("&#246;", "&ouml;");
			htmlEncode.put("&#247;", "&divide;");
			htmlEncode.put("&#248;", "&oslash;");
			htmlEncode.put("&#249;", "&ugrave;");
			htmlEncode.put("&#250;", "&uacute;");
			htmlEncode.put("&#251;", "&ucirc;");
			htmlEncode.put("&#252;", "&uuml;");
			htmlEncode.put("&#253;", "&yacute;");
			htmlEncode.put("&#254;", "&thorn;");
			htmlEncode.put("&#255;", "&yuml;");
			htmlEncode.put("&#338;", "&OElig;");
			htmlEncode.put("&#339;", "&oelig;");
			htmlEncode.put("&#352;", "&Scaron;");
			htmlEncode.put("&#353;", "&scaron;");
			htmlEncode.put("&#376;", "&Yuml;");
			htmlEncode.put("&#402;", "&fnof;");
			htmlEncode.put("&#710;", "&circ;");
			htmlEncode.put("&#732;", "&tilde;");
			htmlEncode.put("&#913;", "&Alpha;");
			htmlEncode.put("&#914;", "&Beta;");
			htmlEncode.put("&#915;", "&Gamma;");
			htmlEncode.put("&#916;", "&Delta;");
			htmlEncode.put("&#917;", "&Epsilon;");
			htmlEncode.put("&#918;", "&Zeta;");
			htmlEncode.put("&#919;", "&Eta;");
			htmlEncode.put("&#920;", "&Theta;");
			htmlEncode.put("&#921;", "&Iota;");
			htmlEncode.put("&#922;", "&Kappa;");
			htmlEncode.put("&#923;", "&Lambda;");
			htmlEncode.put("&#924;", "&Mu;");
			htmlEncode.put("&#925;", "&Nu;");
			htmlEncode.put("&#926;", "&Xi;");
			htmlEncode.put("&#927;", "&Omicron;");
			htmlEncode.put("&#928;", "&Pi;");
			htmlEncode.put("&#929;", "&Rho;");
			htmlEncode.put("&#931;", "&Sigma;");
			htmlEncode.put("&#932;", "&Tau;");
			htmlEncode.put("&#933;", "&Upsilon;");
			htmlEncode.put("&#934;", "&Phi;");
			htmlEncode.put("&#935;", "&Chi;");
			htmlEncode.put("&#936;", "&Psi;");
			htmlEncode.put("&#937;", "&Omega;");
			htmlEncode.put("&#945;", "&alpha;");
			htmlEncode.put("&#946;", "&beta;");
			htmlEncode.put("&#947;", "&gamma;");
			htmlEncode.put("&#948;", "&delta;");
			htmlEncode.put("&#949;", "&epsilon;");
			htmlEncode.put("&#950;", "&zeta;");
			htmlEncode.put("&#951;", "&eta;");
			htmlEncode.put("&#952;", "&theta;");
			htmlEncode.put("&#953;", "&iota;");
			htmlEncode.put("&#954;", "&kappa;");
			htmlEncode.put("&#955;", "&lambda;");
			htmlEncode.put("&#956;", "&mu;");
			htmlEncode.put("&#957;", "&nu;");
			htmlEncode.put("&#958;", "&xi;");
			htmlEncode.put("&#959;", "&omicron;");
			htmlEncode.put("&#960;", "&pi;");
			htmlEncode.put("&#961;", "&rho;");
			htmlEncode.put("&#962;", "&sigmaf;");
			htmlEncode.put("&#963;", "&sigma;");
			htmlEncode.put("&#964;", "&tau;");
			htmlEncode.put("&#965;", "&upsilon;");
			htmlEncode.put("&#966;", "&phi;");
			htmlEncode.put("&#967;", "&chi;");
			htmlEncode.put("&#968;", "&psi;");
			htmlEncode.put("&#969;", "&omega;");
			htmlEncode.put("&#977;", "&thetasym;");
			htmlEncode.put("&#978;", "&upsih;");
			htmlEncode.put("&#982;", "&piv;");
			htmlEncode.put("&#8194;", "&ensp;");
			htmlEncode.put("&#8195;", "&emsp;");
			htmlEncode.put("&#8201;", "&thinsp;");
			htmlEncode.put("&#8204;", "&zwnj;");
			htmlEncode.put("&#8205;", "&zwj;");
			htmlEncode.put("&#8206;", "&lrm;");
			htmlEncode.put("&#8207;", "&rlm;");
			htmlEncode.put("&#8211;", "&ndash;");
			htmlEncode.put("&#8212;", "&mdash;");
			htmlEncode.put("&#8216;", "&lsquo;");
			htmlEncode.put("&#8217;", "&rsquo;");
			htmlEncode.put("&#8218;", "&sbquo;");
			htmlEncode.put("&#8220;", "&ldquo;");
			htmlEncode.put("&#8221;", "&rdquo;");
			htmlEncode.put("&#8222;", "&bdquo;");
			htmlEncode.put("&#8224;", "&dagger;");
			htmlEncode.put("&#8225;", "&Dagger;");
			htmlEncode.put("&#8226;", "&bull;");
			htmlEncode.put("&#8230;", "&hellip;");
			htmlEncode.put("&#8240;", "&permil;");
			htmlEncode.put("&#8242;", "&prime;");
			htmlEncode.put("&#8243;", "&Prime;");
			htmlEncode.put("&#8249;", "&lsaquo;");
			htmlEncode.put("&#8250;", "&rsaquo;");
			htmlEncode.put("&#8254;", "&oline;");
			htmlEncode.put("&#8260;", "&frasl;");
			htmlEncode.put("&#8364;", "&euro;");
			htmlEncode.put("&#8472;", "&weierp;");
			htmlEncode.put("&#8465;", "&image;");
			htmlEncode.put("&#8476;", "&real;");
			htmlEncode.put("&#8482;", "&trade;");
			htmlEncode.put("&#8501;", "&alefsym;");
			htmlEncode.put("&#8592;", "&larr;");
			htmlEncode.put("&#8593;", "&uarr;");
			htmlEncode.put("&#8594;", "&rarr;");
			htmlEncode.put("&#8595;", "&darr;");
			htmlEncode.put("&#8596;", "&harr;");
			htmlEncode.put("&#8629;", "&crarr;");
			htmlEncode.put("&#8656;", "&lArr;");
			htmlEncode.put("&#8657;", "&uArr;");
			htmlEncode.put("&#8658;", "&rArr;");
			htmlEncode.put("&#8659;", "&dArr;");
			htmlEncode.put("&#8660;", "&hArr;");
			htmlEncode.put("&#8704;", "&forall;");
			htmlEncode.put("&#8706;", "&part;");
			htmlEncode.put("&#8707;", "&exist;");
			htmlEncode.put("&#8709;", "&empty;");
			htmlEncode.put("&#8711;", "&nabla;");
			htmlEncode.put("&#8712;", "&isin;");
			htmlEncode.put("&#8713;", "&notin;");
			htmlEncode.put("&#8715;", "&ni;");
			htmlEncode.put("&#8719;", "&prod;");
			htmlEncode.put("&#8721;", "&sum;");
			htmlEncode.put("&#8722;", "&minus;");
			htmlEncode.put("&#8727;", "&lowast;");
			htmlEncode.put("&#8730;", "&radic;");
			htmlEncode.put("&#8733;", "&prop;");
			htmlEncode.put("&#8734;", "&infin;");
			htmlEncode.put("&#8736;", "&ang;");
			htmlEncode.put("&#8743;", "&and;");
			htmlEncode.put("&#8744;", "&or;");
			htmlEncode.put("&#8745;", "&cap;");
			htmlEncode.put("&#8746;", "&cup;");
			htmlEncode.put("&#8747;", "&int;");
			htmlEncode.put("&#8756;", "&there4;");
			htmlEncode.put("&#8764;", "&sim;");
			htmlEncode.put("&#8773;", "&cong;");
			htmlEncode.put("&#8776;", "&asymp;");
			htmlEncode.put("&#8800;", "&ne;");
			htmlEncode.put("&#8801;", "&equiv;");
			htmlEncode.put("&#8804;", "&le;");
			htmlEncode.put("&#8805;", "&ge;");
			htmlEncode.put("&#8834;", "&sub;");
			htmlEncode.put("&#8835;", "&sup;");
			htmlEncode.put("&#8836;", "&nsub;");
			htmlEncode.put("&#8838;", "&sube;");
			htmlEncode.put("&#8839;", "&supe;");
			htmlEncode.put("&#8853;", "&oplus;");
			htmlEncode.put("&#8855;", "&otimes;");
			htmlEncode.put("&#8869;", "&perp;");
			htmlEncode.put("&#8901;", "&sdot;");
			htmlEncode.put("&#8968;", "&lceil;");
			htmlEncode.put("&#8969;", "&rceil;");
			htmlEncode.put("&#8970;", "&lfloor;");
			htmlEncode.put("&#8971;", "&rfloor;");
			htmlEncode.put("&#9001;", "&lang;");
			htmlEncode.put("&#9002;", "&rang;");
			htmlEncode.put("&#9674;", "&loz;");
			htmlEncode.put("&#9824;", "&spades;");
			htmlEncode.put("&#9827;", "&clubs;");
			htmlEncode.put("&#9829;", "&hearts;");
			htmlEncode.put("&#9830;", "&diams;");
		}
		return htmlEncode;
	}
	
	/**
	 * Gets the html decode mapping
	 * @return html decode mapping
	 */
	private static Map<String, String> getHtmlDecode() {
		if(htmlDecode == null) {
			htmlDecode = new HashMap<String, String>();
			for(String ch : getHtmlEncode().keySet()) {
				htmlDecode.put(getHtmlEncode().get(ch), ch);
			}
		}
		return htmlDecode;
	}
	
	/**
	 * Gets the xml encode mapping
	 * @return xml encode mapping
	 */
	private static Map<String, String> getXmlEncode() {
		if(xmlEncode == null) {
			xmlEncode = new HashMap<String, String>();
			xmlEncode.put("&#32;", "&nbsp;");
			xmlEncode.put("&#34;", "&quot;");
			xmlEncode.put("&#38;", "&amp;");
			xmlEncode.put("&#39;", "&apos;");
			xmlEncode.put("&#60;", "&lt;");
			xmlEncode.put("&#62;", "&gt;");
		}
		return xmlEncode;
	}
	
	/**
	 * Gets the xml decode mapping
	 * @return xml decode mapping
	 */
	private static Map<String, String> getXmlDecode() {
		if(xmlDecode == null) {
			xmlDecode = new HashMap<String, String>();
			for(String ch : getXmlEncode().keySet()) {
				xmlDecode.put(getXmlEncode().get(ch), ch);
			}
		}
		return xmlDecode;
	}
	
	/**
	 * Gets the list of accepted characters
	 * @return the character list
	 */
	private static List<Character> getAcceptedChars() {
		if(acceptedChars == null) {
			acceptedChars = new LinkedList<Character>();
			acceptedChars.add(Character.valueOf('a'));
			acceptedChars.add(Character.valueOf('b'));
			acceptedChars.add(Character.valueOf('c'));
			acceptedChars.add(Character.valueOf('d'));
			acceptedChars.add(Character.valueOf('e'));
			acceptedChars.add(Character.valueOf('f'));
			acceptedChars.add(Character.valueOf('g'));
			acceptedChars.add(Character.valueOf('h'));
			acceptedChars.add(Character.valueOf('i'));
			acceptedChars.add(Character.valueOf('j'));
			acceptedChars.add(Character.valueOf('k'));
			acceptedChars.add(Character.valueOf('l'));
			acceptedChars.add(Character.valueOf('m'));
			acceptedChars.add(Character.valueOf('n'));
			acceptedChars.add(Character.valueOf('o'));
			acceptedChars.add(Character.valueOf('p'));
			acceptedChars.add(Character.valueOf('q'));
			acceptedChars.add(Character.valueOf('r'));
			acceptedChars.add(Character.valueOf('s'));
			acceptedChars.add(Character.valueOf('t'));
			acceptedChars.add(Character.valueOf('u'));
			acceptedChars.add(Character.valueOf('v'));
			acceptedChars.add(Character.valueOf('w'));
			acceptedChars.add(Character.valueOf('x'));
			acceptedChars.add(Character.valueOf('y'));
			acceptedChars.add(Character.valueOf('z'));
			acceptedChars.add(Character.valueOf('A'));
			acceptedChars.add(Character.valueOf('B'));
			acceptedChars.add(Character.valueOf('C'));
			acceptedChars.add(Character.valueOf('D'));
			acceptedChars.add(Character.valueOf('E'));
			acceptedChars.add(Character.valueOf('F'));
			acceptedChars.add(Character.valueOf('G'));
			acceptedChars.add(Character.valueOf('H'));
			acceptedChars.add(Character.valueOf('I'));
			acceptedChars.add(Character.valueOf('J'));
			acceptedChars.add(Character.valueOf('K'));
			acceptedChars.add(Character.valueOf('L'));
			acceptedChars.add(Character.valueOf('M'));
			acceptedChars.add(Character.valueOf('N'));
			acceptedChars.add(Character.valueOf('O'));
			acceptedChars.add(Character.valueOf('P'));
			acceptedChars.add(Character.valueOf('Q'));
			acceptedChars.add(Character.valueOf('R'));
			acceptedChars.add(Character.valueOf('S'));
			acceptedChars.add(Character.valueOf('T'));
			acceptedChars.add(Character.valueOf('U'));
			acceptedChars.add(Character.valueOf('V'));
			acceptedChars.add(Character.valueOf('W'));
			acceptedChars.add(Character.valueOf('X'));
			acceptedChars.add(Character.valueOf('Y'));
			acceptedChars.add(Character.valueOf('Z'));
			acceptedChars.add(Character.valueOf('0'));
			acceptedChars.add(Character.valueOf('1'));
			acceptedChars.add(Character.valueOf('2'));
			acceptedChars.add(Character.valueOf('3'));
			acceptedChars.add(Character.valueOf('4'));
			acceptedChars.add(Character.valueOf('5'));
			acceptedChars.add(Character.valueOf('6'));
			acceptedChars.add(Character.valueOf('7'));
			acceptedChars.add(Character.valueOf('8'));
			acceptedChars.add(Character.valueOf('9'));
			acceptedChars.add(Character.valueOf('.'));
			acceptedChars.add(Character.valueOf(','));
			acceptedChars.add(Character.valueOf(';'));
			acceptedChars.add(Character.valueOf(':'));
			acceptedChars.add(Character.valueOf('?'));
			acceptedChars.add(Character.valueOf('/'));
			acceptedChars.add(Character.valueOf('\\'));
			acceptedChars.add(Character.valueOf('|'));
			acceptedChars.add(Character.valueOf('('));
			acceptedChars.add(Character.valueOf(')'));
			acceptedChars.add(Character.valueOf('{'));
			acceptedChars.add(Character.valueOf('}'));
			acceptedChars.add(Character.valueOf('['));
			acceptedChars.add(Character.valueOf(']'));
			acceptedChars.add(Character.valueOf('~'));
			acceptedChars.add(Character.valueOf('`'));
			acceptedChars.add(Character.valueOf('!'));
			acceptedChars.add(Character.valueOf('@'));
			acceptedChars.add(Character.valueOf('#'));
			acceptedChars.add(Character.valueOf('$'));
			acceptedChars.add(Character.valueOf('%'));
			acceptedChars.add(Character.valueOf('^'));
			acceptedChars.add(Character.valueOf('*'));
			acceptedChars.add(Character.valueOf('-'));
			acceptedChars.add(Character.valueOf('_'));
			acceptedChars.add(Character.valueOf(' '));
			acceptedChars.add(Character.valueOf('+'));
			acceptedChars.add(Character.valueOf('='));
			acceptedChars.add(Character.valueOf('\t'));
			acceptedChars.add(Character.valueOf('\n'));
			acceptedChars.add(Character.valueOf('\r'));
			acceptedChars.add(Character.valueOf('\f'));
		}
		return acceptedChars;
	}
	
	/**
	 * Converts all special characters to HTML-entities
	 * @param s input string
	 * @param exceptions force these characters to be encoded
	 * @return html encoded string
	 */
	public static String htmlEncode(String s, char... exceptions) {
		return encode(s, "html", exceptions);
	}
	
	/**
	 * Converts all HTML-special-character-entities to special characters
	 * @param s input string
	 * @return html decoded string
	 */
	public static String htmlDecode(String s) {
		return decode(s, "html");
	}
	
	/**
	 * Converts all special characters to XML-entities
	 * @param s input string
	 * @param exceptions force these characters to be encoded
	 * @return xml encoded string
	 */
	public static String xmlEncode(String s, char... exceptions) {
		return encode(s, "xml", exceptions);
	}
	
	/**
	 * Converts all XML-special-character-entities to special characters
	 * @param s input string
	 * @return xml decoded string
	 */
	public static String xmlDecode(String s) {
		return decode(s, "xml");
	}
	
	/**
	 * Converts all special characters to encoded-entities
	 * @param s input string
	 * @param encodeType the type of encoding
	 * @param exceptions force these characters to be encoded
	 * @return encoded string
	 */
	private static String encode(String s, String encodeType, char... exceptions) {
		if(s == null){
			return null;
		}
		Map<String, String> encodeMap;
		List<Character> accept = new LinkedList<Character>(getAcceptedChars());
		for(char c : exceptions) {
			accept.remove(Character.valueOf(c));
		}
		if(encodeType.equalsIgnoreCase("html")) {
			encodeMap = getHtmlEncode();
		} else if(encodeType.equalsIgnoreCase("xml")) {
			encodeMap = getXmlEncode();
		} else {
			throw new IllegalArgumentException("encodeType must be xml or html only");
		}
		StringBuilder b = new StringBuilder(s.length());
		for(int i = 0; i < s.length(); i++) {
			char ch = s.charAt(i);
			if(accept.contains(Character.valueOf(ch))) {
				b.append(ch);
			} else {
				StringBuilder b2 = new StringBuilder();
				if(Character.isISOControl(ch)) {
					// ignore
				} else if(Character.isWhitespace(ch)) {
					b2.append("&#").append((int)ch).append(";");
				} else if(Character.isHighSurrogate(ch)) {
					int codePoint;
					if((i + 1 < s.length()) && Character.isSurrogatePair(ch, s.charAt(i + 1)) && Character.isDefined(codePoint = (Character.toCodePoint(ch, s.charAt(i + 1))))) {
						b2.append("&#").append(codePoint).append(";");
					}
					i++;
				} else if(Character.isLowSurrogate(ch)) {
					i++;
				} else if(Character.isDefined(ch)) {
					b2.append("&#").append((int)ch).append(";");
				}
				if(encodeMap.containsKey(b2.toString())) {
					b.append(encodeMap.get(b2.toString()));
				} else {
					b.append(b2.toString());
				}
			}
		}
		return b.toString();
	}
	
	/**
	 * Converts all special-character-entities to special characters
	 * @param s input string
	 * @param decodeType the type of decoding
	 * @return decoded string
	 */
	private static String decode(String s, String decodeType) {
		Map<String, String> decodeMap;
		if(decodeType.equalsIgnoreCase("html")) {
			decodeMap = getHtmlDecode();
		} else if(decodeType.equalsIgnoreCase("xml")) {
			decodeMap = getXmlDecode();
		} else {
			throw new IllegalArgumentException("decodeType must be xml or html only");
		}
		String str = s;
		for(String htmlEntity : decodeMap.keySet()) {
			str = str.replaceAll(htmlEntity, decodeMap.get(htmlEntity));
		}
		Matcher m = Pattern.compile("&#(.*?);").matcher(str);
		Set<String> charCodes = new HashSet<String>();
		while(m.find()) {
			charCodes.add(m.group(1));
		}
		for(String ch : charCodes) {
			int charCode = Integer.parseInt(ch);
			if(Character.isDefined((char)charCode)) {
				str = str.replaceAll("&#" + ch + ";", "" + (char)charCode);
			} else if(Character.isDefined(charCode)) {
				StringBuilder b2 = new StringBuilder();
				for(char c : Character.toChars(charCode)) {
					b2.append(c);
				}
				str = str.replaceAll("&#" + ch + ";", b2.toString());
			}
		}
		return str;
	}
}

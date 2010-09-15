/*******************************************************************************
 * Copyright (c) 2010 Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the new BSD license
 * which accompanies this distribution, and is available at
 * http://www.opensource.org/licenses/bsd-license.html
 * 
 * Contributors:
 *     Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams - initial API and implementation
 ******************************************************************************/
package org.vivoweb.ingest.util;

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
public class HtmlEntities {
	/**
	 * encode mapping
	 */
	private static Map<String,String> encode = null;
	/**
	 * decode mapping
	 */
	private static Map<String,String> decode = null;
	
	/**
	 * Gets the encode mapping
	 * @return encode mapping
	 */
	private static Map<String,String> getEncode() {
		if(encode == null) {
			encode = new HashMap<String, String>();
			encode.put("&#161;", "&iexcl;");
			encode.put("&#162;", "&cent;");
			encode.put("&#163;", "&pound;");
			encode.put("&#164;", "&curren;");
			encode.put("&#165;", "&yen;");
			encode.put("&#166;", "&brvbar;");
			encode.put("&#167;", "&sect;");
//			encode.put("&#168;", "&uml;");
			encode.put("&#169;", "&copy;");
			encode.put("&#170;", "&ordf;");
			encode.put("&#171;", "&laquo;");
			encode.put("&#172;", "&not;");
			encode.put("&#173;", "&shy;");
			encode.put("&#174;", "&reg;");
			encode.put("&#175;", "&macr;");
			encode.put("&#176;", "&deg;");
			encode.put("&#177;", "&plusmn;");
			encode.put("&#178;", "&sup2;");
			encode.put("&#179;", "&sup3;");
			encode.put("&#180;", "&acute;");
			encode.put("&#181;", "&micro;");
			encode.put("&#182;", "&para;");
			encode.put("&#183;", "&middot;");
			encode.put("&#184;", "&cedil;");
			encode.put("&#185;", "&sup1;");
			encode.put("&#186;", "&ordm;");
			encode.put("&#187;", "&raquo;");
			encode.put("&#188;", "&frac14;");
			encode.put("&#189;", "&frac12;");
			encode.put("&#190;", "&frac34;");
			encode.put("&#191;", "&iquest;");
			encode.put("&#192;", "&Agrave;");
			encode.put("&#193;", "&Aacute;");
			encode.put("&#194;", "&Acirc;");
			encode.put("&#195;", "&Atilde;");
			encode.put("&#196;", "&Auml;");
			encode.put("&#197;", "&Aring;");
			encode.put("&#198;", "&AElig;");
			encode.put("&#199;", "&Ccedil;");
			encode.put("&#200;", "&Egrave;");
			encode.put("&#201;", "&Eacute;");
			encode.put("&#202;", "&Ecirc;");
			encode.put("&#203;", "&Euml;");
			encode.put("&#204;", "&Igrave;");
			encode.put("&#205;", "&Iacute;");
			encode.put("&#206;", "&Icirc;");
			encode.put("&#207;", "&Iuml;");
			encode.put("&#208;", "&ETH;");
			encode.put("&#209;", "&Ntilde;");
			encode.put("&#210;", "&Ograve;");
			encode.put("&#211;", "&Oacute;");
			encode.put("&#212;", "&Ocirc;");
			encode.put("&#213;", "&Otilde;");
			encode.put("&#214;", "&Ouml;");
			encode.put("&#215;", "&times;");
			encode.put("&#216;", "&Oslash;");
			encode.put("&#217;", "&Ugrave;");
			encode.put("&#218;", "&Uacute;");
			encode.put("&#219;", "&Ucirc;");
			encode.put("&#220;", "&Uuml;");
//			encode.put("&#221;", "&Yacute;");
			encode.put("&#222;", "&THORN;");
			encode.put("&#223;", "&szlig;");
			encode.put("&#224;", "&agrave;");
			encode.put("&#225;", "&aacute;");
			encode.put("&#226;", "&acirc;");
			encode.put("&#227;", "&atilde;");
			encode.put("&#228;", "&auml;");
			encode.put("&#229;", "&aring;");
			encode.put("&#230;", "&aelig;");
			encode.put("&#231;", "&ccedil;");
			encode.put("&#232;", "&egrave;");
			encode.put("&#233;", "&eacute;");
			encode.put("&#234;", "&ecirc;");
			encode.put("&#235;", "&euml;");
			encode.put("&#236;", "&igrave;");
			encode.put("&#237;", "&iacute;");
			encode.put("&#238;", "&icirc;");
			encode.put("&#239;", "&iuml;");
			encode.put("&#240;", "&eth;");
			encode.put("&#241;", "&ntilde;");
			encode.put("&#242;", "&ograve;");
			encode.put("&#243;", "&oacute;");
			encode.put("&#244;", "&ocirc;");
			encode.put("&#245;", "&otilde;");
			encode.put("&#246;", "&ouml;");
			encode.put("&#247;", "&divide;");
			encode.put("&#248;", "&oslash;");
			encode.put("&#249;", "&ugrave;");
			encode.put("&#250;", "&uacute;");
			encode.put("&#251;", "&ucirc;");
			encode.put("&#252;", "&uuml;");
			encode.put("&#253;", "&yacute;");
			encode.put("&#254;", "&thorn;");
			encode.put("&#255;", "&yuml;");
			encode.put("&#402;", "&fnof;");
			encode.put("&#913;", "&Alpha;");
			encode.put("&#914;", "&Beta;");
			encode.put("&#915;", "&Gamma;");
			encode.put("&#916;", "&Delta;");
			encode.put("&#917;", "&Epsilon;");
			encode.put("&#918;", "&Zeta;");
			encode.put("&#919;", "&Eta;");
			encode.put("&#920;", "&Theta;");
			encode.put("&#921;", "&Iota;");
			encode.put("&#922;", "&Kappa;");
			encode.put("&#923;", "&Lambda;");
			encode.put("&#924;", "&Mu;");
			encode.put("&#925;", "&Nu;");
			encode.put("&#926;", "&Xi;");
			encode.put("&#927;", "&Omicron;");
			encode.put("&#928;", "&Pi;");
			encode.put("&#929;", "&Rho;");
			encode.put("&#931;", "&Sigma;");
			encode.put("&#932;", "&Tau;");
			encode.put("&#933;", "&Upsilon;");
			encode.put("&#934;", "&Phi;");
			encode.put("&#935;", "&Chi;");
			encode.put("&#936;", "&Psi;");
			encode.put("&#937;", "&Omega;");
			encode.put("&#945;", "&alpha;");
			encode.put("&#946;", "&beta;");
			encode.put("&#947;", "&gamma;");
			encode.put("&#948;", "&delta;");
			encode.put("&#949;", "&epsilon;");
			encode.put("&#950;", "&zeta;");
			encode.put("&#951;", "&eta;");
			encode.put("&#952;", "&theta;");
			encode.put("&#953;", "&iota;");
			encode.put("&#954;", "&kappa;");
			encode.put("&#955;", "&lambda;");
			encode.put("&#956;", "&mu;");
			encode.put("&#957;", "&nu;");
			encode.put("&#958;", "&xi;");
			encode.put("&#959;", "&omicron;");
			encode.put("&#960;", "&pi;");
			encode.put("&#961;", "&rho;");
			encode.put("&#962;", "&sigmaf;");
			encode.put("&#963;", "&sigma;");
			encode.put("&#964;", "&tau;");
			encode.put("&#965;", "&upsilon;");
			encode.put("&#966;", "&phi;");
			encode.put("&#967;", "&chi;");
			encode.put("&#968;", "&psi;");
			encode.put("&#969;", "&omega;");
			encode.put("&#977;", "&thetasym;");
			encode.put("&#978;", "&upsih;");
			encode.put("&#982;", "&piv;");
			encode.put("&#8226;", "&bull;");
			encode.put("&#8230;", "&hellip;");
			encode.put("&#8242;", "&prime;");
			encode.put("&#8243;", "&Prime;");
			encode.put("&#8254;", "&oline;");
			encode.put("&#8260;", "&frasl;");
			encode.put("&#8472;", "&weierp;");
			encode.put("&#8465;", "&image;");
			encode.put("&#8476;", "&real;");
			encode.put("&#8482;", "&trade;");
			encode.put("&#8501;", "&alefsym;");
			encode.put("&#8592;", "&larr;");
			encode.put("&#8593;", "&uarr;");
			encode.put("&#8594;", "&rarr;");
			encode.put("&#8595;", "&darr;");
			encode.put("&#8596;", "&harr;");
			encode.put("&#8629;", "&crarr;");
			encode.put("&#8656;", "&lArr;");
			encode.put("&#8657;", "&uArr;");
			encode.put("&#8658;", "&rArr;");
			encode.put("&#8659;", "&dArr;");
			encode.put("&#8660;", "&hArr;");
			encode.put("&#8704;", "&forall;");
			encode.put("&#8706;", "&part;");
			encode.put("&#8707;", "&exist;");
			encode.put("&#8709;", "&empty;");
			encode.put("&#8711;", "&nabla;");
			encode.put("&#8712;", "&isin;");
			encode.put("&#8713;", "&notin;");
			encode.put("&#8715;", "&ni;");
			encode.put("&#8719;", "&prod;");
			encode.put("&#8721;", "&sum;");
			encode.put("&#8722;", "&minus;");
			encode.put("&#8727;", "&lowast;");
			encode.put("&#8730;", "&radic;");
			encode.put("&#8733;", "&prop;");
			encode.put("&#8734;", "&infin;");
			encode.put("&#8736;", "&ang;");
			encode.put("&#8743;", "&and;");
			encode.put("&#8744;", "&or;");
			encode.put("&#8745;", "&cap;");
			encode.put("&#8746;", "&cup;");
			encode.put("&#8747;", "&int;");
			encode.put("&#8756;", "&there4;");
			encode.put("&#8764;", "&sim;");
			encode.put("&#8773;", "&cong;");
			encode.put("&#8776;", "&asymp;");
			encode.put("&#8800;", "&ne;");
			encode.put("&#8801;", "&equiv;");
			encode.put("&#8804;", "&le;");
			encode.put("&#8805;", "&ge;");
			encode.put("&#8834;", "&sub;");
			encode.put("&#8835;", "&sup;");
			encode.put("&#8836;", "&nsub;");
			encode.put("&#8838;", "&sube;");
			encode.put("&#8839;", "&supe;");
			encode.put("&#8853;", "&oplus;");
			encode.put("&#8855;", "&otimes;");
			encode.put("&#8869;", "&perp;");
			encode.put("&#8901;", "&sdot;");
			encode.put("&#8968;", "&lceil;");
			encode.put("&#8969;", "&rceil;");
			encode.put("&#8970;", "&lfloor;");
			encode.put("&#8971;", "&rfloor;");
			encode.put("&#9001;", "&lang;");
			encode.put("&#9002;", "&rang;");
			encode.put("&#9674;", "&loz;");
			encode.put("&#9824;", "&spades;");
			encode.put("&#9827;", "&clubs;");
			encode.put("&#9829;", "&hearts;");
			encode.put("&#9830;", "&diams;");
			encode.put("&#34;", "&quot;");
			encode.put("&#38;", "&amp;");
			encode.put("&#60;", "&lt;");
			encode.put("&#62;", "&gt;");
			encode.put("&#338;", "&OElig;");
			encode.put("&#339;", "&oelig;");
			encode.put("&#352;", "&Scaron;");
			encode.put("&#353;", "&scaron;");
			encode.put("&#376;", "&Yuml;");
			encode.put("&#710;", "&circ;");
			encode.put("&#732;", "&tilde;");
			encode.put("&#8194;", "&ensp;");
			encode.put("&#8195;", "&emsp;");
			encode.put("&#8201;", "&thinsp;");
			encode.put("&#8204;", "&zwnj;");
			encode.put("&#8205;", "&zwj;");
			encode.put("&#8206;", "&lrm;");
			encode.put("&#8207;", "&rlm;");
			encode.put("&#8211;", "&ndash;");
			encode.put("&#8212;", "&mdash;");
			encode.put("&#8216;", "&lsquo;");
			encode.put("&#8217;", "&rsquo;");
			encode.put("&#8218;", "&sbquo;");
			encode.put("&#8220;", "&ldquo;");
			encode.put("&#8221;", "&rdquo;");
			encode.put("&#8222;", "&bdquo;");
			encode.put("&#8224;", "&dagger;");
			encode.put("&#8225;", "&Dagger;");
			encode.put("&#8240;", "&permil;");
			encode.put("&#8249;", "&lsaquo;");
			encode.put("&#8250;", "&rsaquo;");
			encode.put("&#8364;", "&euro;");
			// be carefull with this one (non-breaking whitee space)
			encode.put("&#160;", "&nbsp;");
		}
		return encode;
	}
	
	/**
	 * Gets the decode mapping
	 * @return decode mapping
	 */
	private static Map<String, String> getDecode() {
		if(decode == null) {
			decode = new HashMap<String, String>();
			for(String ch : getEncode().keySet()) {
				decode.put(getEncode().get(ch), ch);
			}
		}
		return decode;
	}
	
	/**
	 * Converts all special characters to HTML-entities
	 * @param s input string
	 * @return html encoded string
	 */
	public static String htmlEncode(String s) {
		List<Character> acceptedTypes = new LinkedList<Character>();
    acceptedTypes.add(Character.valueOf('a'));
    acceptedTypes.add(Character.valueOf('b'));
    acceptedTypes.add(Character.valueOf('c'));
    acceptedTypes.add(Character.valueOf('d'));
    acceptedTypes.add(Character.valueOf('e'));
    acceptedTypes.add(Character.valueOf('f'));
    acceptedTypes.add(Character.valueOf('g'));
    acceptedTypes.add(Character.valueOf('h'));
    acceptedTypes.add(Character.valueOf('i'));
    acceptedTypes.add(Character.valueOf('j'));
    acceptedTypes.add(Character.valueOf('k'));
    acceptedTypes.add(Character.valueOf('l'));
    acceptedTypes.add(Character.valueOf('m'));
    acceptedTypes.add(Character.valueOf('n'));
    acceptedTypes.add(Character.valueOf('o'));
    acceptedTypes.add(Character.valueOf('p'));
    acceptedTypes.add(Character.valueOf('q'));
    acceptedTypes.add(Character.valueOf('r'));
    acceptedTypes.add(Character.valueOf('s'));
    acceptedTypes.add(Character.valueOf('t'));
    acceptedTypes.add(Character.valueOf('u'));
    acceptedTypes.add(Character.valueOf('v'));
    acceptedTypes.add(Character.valueOf('w'));
    acceptedTypes.add(Character.valueOf('x'));
    acceptedTypes.add(Character.valueOf('y'));
    acceptedTypes.add(Character.valueOf('z'));
    acceptedTypes.add(Character.valueOf('A'));
    acceptedTypes.add(Character.valueOf('B'));
    acceptedTypes.add(Character.valueOf('C'));
    acceptedTypes.add(Character.valueOf('D'));
    acceptedTypes.add(Character.valueOf('E'));
    acceptedTypes.add(Character.valueOf('F'));
    acceptedTypes.add(Character.valueOf('G'));
    acceptedTypes.add(Character.valueOf('H'));
    acceptedTypes.add(Character.valueOf('I'));
    acceptedTypes.add(Character.valueOf('J'));
    acceptedTypes.add(Character.valueOf('K'));
    acceptedTypes.add(Character.valueOf('L'));
    acceptedTypes.add(Character.valueOf('M'));
    acceptedTypes.add(Character.valueOf('N'));
    acceptedTypes.add(Character.valueOf('O'));
    acceptedTypes.add(Character.valueOf('P'));
    acceptedTypes.add(Character.valueOf('Q'));
    acceptedTypes.add(Character.valueOf('R'));
    acceptedTypes.add(Character.valueOf('S'));
    acceptedTypes.add(Character.valueOf('T'));
    acceptedTypes.add(Character.valueOf('U'));
    acceptedTypes.add(Character.valueOf('V'));
    acceptedTypes.add(Character.valueOf('W'));
    acceptedTypes.add(Character.valueOf('X'));
    acceptedTypes.add(Character.valueOf('Y'));
    acceptedTypes.add(Character.valueOf('Z'));
    acceptedTypes.add(Character.valueOf('0'));
    acceptedTypes.add(Character.valueOf('1'));
    acceptedTypes.add(Character.valueOf('2'));
    acceptedTypes.add(Character.valueOf('3'));
    acceptedTypes.add(Character.valueOf('4'));
    acceptedTypes.add(Character.valueOf('5'));
    acceptedTypes.add(Character.valueOf('6'));
    acceptedTypes.add(Character.valueOf('7'));
    acceptedTypes.add(Character.valueOf('8'));
    acceptedTypes.add(Character.valueOf('9'));
		acceptedTypes.add(Character.valueOf('\''));
		acceptedTypes.add(Character.valueOf('.'));
		acceptedTypes.add(Character.valueOf(','));
		acceptedTypes.add(Character.valueOf(';'));
		acceptedTypes.add(Character.valueOf(':'));
		acceptedTypes.add(Character.valueOf('?'));
		acceptedTypes.add(Character.valueOf('/'));
		acceptedTypes.add(Character.valueOf('\\'));
		acceptedTypes.add(Character.valueOf('|'));
		acceptedTypes.add(Character.valueOf('('));
		acceptedTypes.add(Character.valueOf(')'));
		acceptedTypes.add(Character.valueOf('{'));
		acceptedTypes.add(Character.valueOf('}'));
		acceptedTypes.add(Character.valueOf('['));
		acceptedTypes.add(Character.valueOf(']'));
		acceptedTypes.add(Character.valueOf('!'));
		acceptedTypes.add(Character.valueOf('@'));
		acceptedTypes.add(Character.valueOf('#'));
		acceptedTypes.add(Character.valueOf('$'));
		acceptedTypes.add(Character.valueOf('%'));
		acceptedTypes.add(Character.valueOf('^'));
		acceptedTypes.add(Character.valueOf('*'));
		acceptedTypes.add(Character.valueOf('-'));
		acceptedTypes.add(Character.valueOf('_'));
		acceptedTypes.add(Character.valueOf('+'));
		acceptedTypes.add(Character.valueOf('='));
		acceptedTypes.add(Character.valueOf('`'));
		acceptedTypes.add(Character.valueOf('='));
		acceptedTypes.add(Character.valueOf(' '));
		acceptedTypes.add(Character.valueOf('\t'));
		acceptedTypes.add(Character.valueOf('\n'));
		acceptedTypes.add(Character.valueOf('\r'));
		acceptedTypes.add(Character.valueOf('\f'));
		StringBuilder b = new StringBuilder(s.length());
		for(int i = 0; i < s.length(); i++ ) {
			char ch = s.charAt(i);
			if(acceptedTypes.contains(Character.valueOf(ch))) {
				b.append(ch);
			} else {
				StringBuilder b2 = new StringBuilder();
				if(Character.isWhitespace(ch)) {
					b2.append("&#").append((int)ch).append(";");
				} else if(Character.isISOControl(ch)) {
					//ignore
				} else if(Character.isHighSurrogate(ch)) {
					int codePoint;
					if(i + 1 < s.length() && Character.isSurrogatePair(ch, s.charAt(i + 1)) && Character.isDefined(codePoint = (Character.toCodePoint(ch, s.charAt(i + 1))))) {
						b2.append("&#").append(codePoint).append(";");
					}
					i++;
				} else if(Character.isLowSurrogate(ch)) {
					i++;
				} else if(Character.isDefined(ch)) {
					b2.append("&#").append((int)ch).append(";");
				}
				if(getEncode().containsKey(b2.toString())) {
					b.append(getEncode().get(b2.toString()));
				} else {
					b.append(b2.toString());
				}
			}
		}
		return b.toString();
	}
	
	/**
	 * Converts all HTML-special-character-entities to special characters
	 * @param s input string
	 * @return html decoded string
	 */
	public static String htmlDecode(String s) {
		String str = s;
		for(String htmlEntity : getDecode().keySet()) {
			str = str.replaceAll(htmlEntity, getDecode().get(htmlEntity));
		}
		Matcher m = Pattern.compile("&#(.*?);").matcher(str);
		Set<String> charCodes = new HashSet<String>();
		while(m.find()) {
			charCodes.add(m.group(1));
		}
		for(String ch : charCodes) {
			int charCode = Integer.parseInt(ch);
			if(Character.isDefined((char)charCode)) {
				str = str.replaceAll("&#"+ch+";", ""+(char)charCode);
			} else if(Character.isDefined(charCode)) {
				StringBuilder b2 = new StringBuilder();
				for(char c : Character.toChars(charCode)) {
					b2.append(c);
				}
				str = str.replaceAll("&#"+ch+";", b2.toString());
			}
		}
		return str;
	}
}

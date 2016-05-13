package org.vivoweb.harvester.util.xslt;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author jaf30
 *
 */
public class StringHash {
	/**
	 * hex chars
	 */
	private static final char[] hex = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
	/**
	 * constructor
	 */
	public StringHash() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param args main arguments
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub 
	}
	
	/**
	 * @param md5 input string
	 * @return encoded string
	 */
	public static String getMD5(String md5) {
		try {
		    MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] array = md.digest(md5.getBytes());
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < array.length; ++i) {
				sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1, 3));
			}
			return sb.toString();
		} catch (NoSuchAlgorithmException e) {
			// todo
		} catch (NullPointerException e) {
		    // todo
		}
		return "";
	}
	
	
	/**
	 * @param bytes input as bytes
	 * @return hex string
	 */
	public static String byteArray2Hex(byte[] bytes) {
	    StringBuffer sb = new StringBuffer(bytes.length * 2);
	    for(final byte b : bytes) {
	        sb.append(hex[(b & 0xF0) >> 4]);
	        sb.append(hex[b & 0x0F]);
	    }
	    return sb.toString();
	}

	/**
	 * @param stringToEncrypt input
	 * @return sha256 string
	 */
	public static String getSHA256(String stringToEncrypt)  {
		try {
	       MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
	       messageDigest.update(stringToEncrypt.getBytes());
	       return byteArray2Hex(messageDigest.digest());
		} catch (NoSuchAlgorithmException e) {
			// todo
		} catch (NullPointerException e) {
		    // todo
		}
		return "";
	}

}

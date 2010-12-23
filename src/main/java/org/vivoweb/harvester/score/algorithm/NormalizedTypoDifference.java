package org.vivoweb.harvester.score.algorithm;

import java.util.HashMap;
import java.util.Map;
import org.vivoweb.harvester.util.MathHelper;

/**
 * Calculates the difference of two strings and accounts for typos
 * @author Christopher Haines hainesc@ctrip.ufl.edu
 */
@SuppressWarnings("boxing")
public class NormalizedTypoDifference implements Algorithm {
	
	@Override
	public float calculate(CharSequence itemX, CharSequence itemY) {
		int score = 0;
		char[] x = itemX.toString().toCharArray();
		char[] y = itemY.toString().toCharArray();
		int lenDiff = x.length - y.length;
		if(lenDiff == 0) {
			//same length
			
		} else if(lenDiff > 0) {
			//x is longer
			
		} else if(lenDiff < 0) {
			//y is longer
			
		}
		for(int a = 0; a < x.length; a++) {
			for(int b = 0; b < y.length; b++) {
				if(x[a] == y[b]) {
					score++;
				}
			}
		}
		return 0f;
	}
	
	/**
	 * Reduction for things only 1 key away
	 */
	public static final float reduce1Weight = .5f;
	/**
	 * Reduction for things only 1 key away and shifted (for numbers)
	 */
	public static final float reduce1WeightShift = .3f;
	/**
	 * US Enlgish Standard Keyboard Proximity Map
	 */
	public static final Map<Character,Map<Character,Float>> USEngKeyboard;
	static {
		Map<Character,String> reduce1Map = new HashMap<Character, String>();
		Map<Character,String> reduce1ShiftMap = new HashMap<Character, String>();
		
		reduce1Map.put('`', "1q");
		reduce1ShiftMap.put('`', "!Q");
		reduce1Map.put('~', "!Q");
		reduce1ShiftMap.put('~', "1q");
		
		reduce1Map.put('1', "2wq`");
		reduce1ShiftMap.put('1', "@WQ~");
		reduce1Map.put('!', "@WQ~");
		reduce1ShiftMap.put('!', "2wq`");
		
		reduce1Map.put('2', "3ewq1");
		reduce1ShiftMap.put('2', "#EWQ!");
		reduce1Map.put('@', "#EWQ!");
		reduce1ShiftMap.put('@', "3ewq1");
		
		reduce1Map.put('3', "4rew2");
		reduce1ShiftMap.put('3', "$REW@");
		reduce1Map.put('#', "$REW@");
		reduce1ShiftMap.put('#', "4rew2");
		
		reduce1Map.put('4', "5tre3");
		reduce1ShiftMap.put('4', "%TRE#");
		reduce1Map.put('$', "%TRE#");
		reduce1ShiftMap.put('$', "5tre3");
		
		reduce1Map.put('5', "6ytr4");
		reduce1ShiftMap.put('5', "^YTR$");
		reduce1Map.put('%', "^YTR$");
		reduce1ShiftMap.put('%', "6ytr4");
		
		reduce1Map.put('6', "7uyt5");
		reduce1ShiftMap.put('6', "&UYT%");
		reduce1Map.put('^', "&UYT%");
		reduce1ShiftMap.put('^', "7uyt5");
		
		reduce1Map.put('7', "8iuy6");
		reduce1ShiftMap.put('7', "*IUY^");
		reduce1Map.put('&', "*IUY^");
		reduce1ShiftMap.put('&', "8iuy6");
		
		reduce1Map.put('8', "9oiu7");
		reduce1ShiftMap.put('8', "(OIU&");
		reduce1Map.put('*', "(OIU&");
		reduce1ShiftMap.put('*', "9oiu7");
		
		reduce1Map.put('9', "0poi8");
		reduce1ShiftMap.put('9', ")POI*");
		reduce1Map.put('(', ")POI*");
		reduce1ShiftMap.put('(', "0poi8");
		
		reduce1Map.put('0', "-[po9");
		reduce1ShiftMap.put('0', "_{PO(");
		reduce1Map.put(')', "_{PO(");
		reduce1ShiftMap.put(')', "-[po9");
		
		reduce1Map.put('-', "=][p0");
		reduce1ShiftMap.put('-', "+}{P)");
		reduce1Map.put('_', "+}{P)");
		reduce1ShiftMap.put('_', "=][p0");
		
		reduce1Map.put('=', "][-");
		reduce1ShiftMap.put('=', "}{_");
		reduce1Map.put('+', "}{_");
		reduce1ShiftMap.put('+', "][-");
		
		reduce1Map.put('q', "wsa12");
		reduce1ShiftMap.put('q', "WSA!@");
		
		reduce1Map.put('w', "qasde321");
		reduce1ShiftMap.put('w', "QASDE#@!");
		
		reduce1Map.put('e', "rfdsw234");
		reduce1ShiftMap.put('e', "RFDSW@#$");
		
		reduce1Map.put('r', "tgfde345");
		reduce1ShiftMap.put('r', "TGFDE#$%");
		
		reduce1Map.put('t', "yhgfr456");
		reduce1ShiftMap.put('t', "YHGFR$%^");
		
		reduce1Map.put('y', "ujhgt567");
		reduce1ShiftMap.put('y', "UJHGT%^&");
		
		reduce1Map.put('u', "ikjhy678");
		reduce1ShiftMap.put('u', "IKJHY^&*");
		
		reduce1Map.put('i', "olkju789");
		reduce1ShiftMap.put('i', "OLKJU&*(");
		
		reduce1Map.put('o', "p;lki890");
		reduce1ShiftMap.put('o', "P:LKI*()");
		
		reduce1Map.put('p', "[';lo90-");
		reduce1ShiftMap.put('p', "{\":LO()_");
		
		reduce1Map.put('[', "]';p0-=");
		reduce1ShiftMap.put('[', "}\":P)_+");
		reduce1Map.put('{', "}\":P)_+");
		reduce1ShiftMap.put('{', "]';p0-=");
		
		reduce1Map.put(']', "\'[-=");
		reduce1ShiftMap.put(']', "|\"{_+");
		reduce1Map.put('}', "|\"{+");
		reduce1ShiftMap.put('}', "\'[=");
		
		reduce1Map.put('\'', "]=");
		reduce1ShiftMap.put('\'', "}+");
		reduce1Map.put('|', "}+");
		reduce1ShiftMap.put('|', "]=");
		
		reduce1Map.put('/', ".;'");
		reduce1ShiftMap.put('/',"?>:\"");
		reduce1Map.put('?', ">:\"");
		reduce1ShiftMap.put('?', ".;'");
		
		reduce1Map.put('.', ",l;/");
		reduce1ShiftMap.put('.',"<L:?");
		reduce1Map.put('>', "<L:?");
		reduce1ShiftMap.put('>', ",l;/");
		
		reduce1Map.put(',', "mkl.");
		reduce1ShiftMap.put(',',"MKL>");
		reduce1Map.put('<', "MKL>");
		reduce1ShiftMap.put('<', "mkl.");
		
		reduce1Map.put('m', "njk,");
		reduce1ShiftMap.put('m',"NJK<");
		
		reduce1Map.put('n', "bhjm");
		reduce1ShiftMap.put('n',"BHJM");
		
		reduce1Map.put('b', "vghn");
		reduce1ShiftMap.put('b',"VGHN");
		
		reduce1Map.put('v', "cfgb");
		reduce1ShiftMap.put('v',"CFGB");
		
		reduce1Map.put('c', "xdfv");
		reduce1ShiftMap.put('c',"XDFV");
		
		reduce1Map.put('x', "zsdc");
		reduce1ShiftMap.put('x',"ZSDC");
		
		reduce1Map.put('z', "asx");
		reduce1ShiftMap.put('z',"ASX");
		
		reduce1Map.put('a', "qwsxz");
		reduce1ShiftMap.put('a',"QWSXZ");
		
		reduce1Map.put('s', "qwedcxza");
		reduce1ShiftMap.put('s',"QWEDCXZA");
		
		reduce1Map.put('d', "werfvcxs");
		reduce1ShiftMap.put('d',"WERFVCXS");
		
		reduce1Map.put('f', "ertgbvcd");
		reduce1ShiftMap.put('f',"ERTGBVCD");
		
		reduce1Map.put('g', "rtyhnbvf");
		reduce1ShiftMap.put('g',"RTYHNBVF");
		
		reduce1Map.put('h', "tyujmnbg");
		reduce1ShiftMap.put('h',"TYUJMNBG");
		
		reduce1Map.put('j', "yuik,mnh");
		reduce1ShiftMap.put('j',"YUIK<MNH");
		
		reduce1Map.put('k', "uiol.,mj");
		reduce1ShiftMap.put('k',"UIOL><MJ");
		
		reduce1Map.put('l', "iop;/.,k");
		reduce1ShiftMap.put('l',"IOP:?><K");
		
		reduce1Map.put(';', "op['/.l");
		reduce1ShiftMap.put(';',"OP{\"?>L");
		
		Map<Character, Map<Character, Float>> tmp = new HashMap<Character, Map<Character,Float>>();
		for(Character c : reduce1Map.keySet()) {
			// handle uppercase variants
			char d = Character.toUpperCase(c);
			if(!tmp.containsKey(c)) {
				tmp.put(c, new HashMap<Character, Float>());
			}
			if(!tmp.containsKey(d)) {
				tmp.put(d, new HashMap<Character, Float>());
			}
			for(char x : reduce1Map.get(c).toCharArray()) {
				tmp.get(d).put(x, reduce1WeightShift);
				tmp.get(c).put(x, reduce1Weight);
			}
		}
		for(Character c : reduce1ShiftMap.keySet()) {
			// handle uppercase variants
			char d = Character.toUpperCase(c);
			if(!tmp.containsKey(c)) {
				tmp.put(c, new HashMap<Character, Float>());
			}
			if(!tmp.containsKey(d)) {
				tmp.put(d, new HashMap<Character, Float>());
			}
			for(char x : reduce1Map.get(c).toCharArray()) {
				tmp.get(c).put(x, reduce1WeightShift);
				tmp.get(d).put(x, reduce1Weight);
			}
		}
		USEngKeyboard = tmp;
	}
	
	/**
	 * Typo-Accounting Modified Damerau-Levenshtein Distance
	 * @param x a string
	 * @param y another string
	 * @param keyWeights map of charA to charB to float showing how much to decrease distance when a substition of charA to charB happens
	 * @return the distance
	 */
	public static float getTypoDamerauLevenshtein(CharSequence x, CharSequence y, Map<Character,Map<Character,Float>> keyWeights) {
        if(x == null) {
        	throw new IllegalArgumentException("x cannot be null");
        }
        if(y == null) {
        	throw new IllegalArgumentException("y cannot be null");
        }
		int sLen = x.length();
		int bLen = y.length();
		if(sLen == 0) {
			return bLen;
		}
		if (bLen == 0) {
			return sLen;
		}
		CharSequence small;
		CharSequence big;
		if(sLen > bLen) {
			big = x;
			small = y;
			int tmpLen = bLen;
			bLen = sLen;
			sLen = tmpLen;
		} else {
			big = y;
			small = x;
		}
		int[] prevprev = new int[sLen+1];
		int[] prev = new int[sLen+1];
		int[] dist = new int[sLen+1];
		int[] editCosts = new int[3];
		int[] tmp; // memory holder
		char bigJm1; // big[j-1]
		int sameChar; // is small[i-1] == big[j-1]
		int editTypeIndex; // the edit type
		float decost = 0f;
		for(int i = 0; i <= sLen; i++) {
			prev[i] = i;
			prevprev[i] = i;
		}
		for(int j = 1; j <= bLen; j++) {
			dist[0] = j;
			bigJm1 = big.charAt(j-1);
			for(int i = 1; i <= sLen; i++) {
				sameChar = (small.charAt(i-1) == bigJm1) ? 0 : 1;
				editCosts[0] = dist[i-1] + 1; // deletion
				editCosts[1] = prev[i] + 1; // addition
				editCosts[2] = prev[i-1] + sameChar; //substitution
				editTypeIndex = MathHelper.minIntIndex(editCosts);
				if(editTypeIndex == 2) {
					try {
						System.out.println(small.charAt(i)+" => "+big.charAt(j));
						decost += keyWeights.get(Character.valueOf(small.charAt(i))).get(Character.valueOf(big.charAt(j))).floatValue();
					} catch(NullPointerException e) {
						// mapping must for small[i] to big[j] must not exist - ignore
					}
				}
				dist[i] = editCosts[editTypeIndex];
				if(i > 1 && j > 1 && small.charAt(i) == big.charAt(j-1) && small.charAt(i-1) == big.charAt(j)) {
					dist[i] = MathHelper.minIntIndex(dist[i], prevprev[i-2] + sameChar); // transposition
				}
			}
			tmp = prevprev; // hold existing memory
			prevprev = prev;
			prev = dist;
			dist = tmp; // reuse existing memory
		}
		return prev[sLen]-decost;
	}
}

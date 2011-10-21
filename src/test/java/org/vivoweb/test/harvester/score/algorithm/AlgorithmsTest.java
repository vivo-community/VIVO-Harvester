package org.vivoweb.test.harvester.score.algorithm;

import junit.framework.TestCase;
import org.vivoweb.harvester.score.algorithm.Algorithm;
import org.vivoweb.harvester.score.algorithm.CaseInsensitiveInitialTest;
import org.vivoweb.harvester.score.algorithm.EqualityExtraTest;
import org.vivoweb.harvester.score.algorithm.EqualityTest;
import org.vivoweb.harvester.score.algorithm.NameCompare;
import org.vivoweb.harvester.score.algorithm.NameExtraCompare;
import org.vivoweb.harvester.score.algorithm.NormalizedDamerauLevenshteinDifference;
import org.vivoweb.harvester.score.algorithm.NormalizedDoubleMetaphoneDifference;
import org.vivoweb.harvester.score.algorithm.NormalizedLevenshteinDifference;
import org.vivoweb.harvester.score.algorithm.NormalizedSoundExDifference;
import org.vivoweb.harvester.score.algorithm.NormalizedTypoDifference;

/**
 * Test Algorithms
 * @author Christopher Haines (hainesc@ufl.edu)
 */
public class AlgorithmsTest extends TestCase {
	
	/**
	 * Test method for {@link org.vivoweb.harvester.score.algorithm.NormalizedDamerauLevenshteinDifference#calculate(java.lang.CharSequence, java.lang.CharSequence) calculate(CharSequence itemX, CharSequence itemY)}
	 */
	public final void testNormalizedDamerauLevenshteinDifferenceCalculate() {
		Algorithm calc = new NormalizedDamerauLevenshteinDifference();
		assertEquals(Float.valueOf(1f), Float.valueOf(calc.calculate("test", "test")));
		assertEquals(Float.valueOf(0f), Float.valueOf(calc.calculate("", "")));
		assertEquals(Float.valueOf(0f), Float.valueOf(calc.calculate("", "a")));
		assertEquals(Float.valueOf(0f), Float.valueOf(calc.calculate("aaapppp", "")));
		assertEquals(Float.valueOf(3/4f), Float.valueOf(calc.calculate("frog", "fog")));
		assertEquals(Float.valueOf(3/4f), Float.valueOf(calc.calculate("frog", "forg")));
		assertEquals(Float.valueOf(0f), Float.valueOf(calc.calculate("fly", "ant")));
		assertEquals(Float.valueOf(1/8f), Float.valueOf(calc.calculate("elephant", "hippo")));
		assertEquals(Float.valueOf(1/8f), Float.valueOf(calc.calculate("hippo", "elephant")));
		assertEquals(Float.valueOf(0f), Float.valueOf(calc.calculate("hippo", "zzzzzzzz")));
		assertEquals(Float.valueOf(4/5f), Float.valueOf(calc.calculate("hallo", "halo")));
		assertEquals(Float.valueOf(3/5f), Float.valueOf(calc.calculate("hallo", "ahlo")));
		assertEquals(Float.valueOf(2/5f), Float.valueOf(calc.calculate("hallo", "ehlo")));
		assertEquals(Float.valueOf(3/4f), Float.valueOf(calc.calculate("frog", "frig")));
		assertEquals(Float.valueOf(4/5f), Float.valueOf(calc.calculate("hello", "hallo")));
	}
	
	/**
	 * Test method for {@link org.vivoweb.harvester.score.algorithm.EqualityTest#calculate(java.lang.CharSequence, java.lang.CharSequence) calculate(CharSequence itemX, CharSequence itemY)}
	 */
	public final void testEqualityTestCalculate() {
		Algorithm calc = new EqualityTest();
		assertEquals(Float.valueOf(1f), Float.valueOf(calc.calculate("test", "test")));
		assertEquals(Float.valueOf(0f), Float.valueOf(calc.calculate("", "")));
		assertEquals(Float.valueOf(0f), Float.valueOf(calc.calculate("", "a")));
		assertEquals(Float.valueOf(0f), Float.valueOf(calc.calculate("aaapppp", "")));
		assertEquals(Float.valueOf(0f), Float.valueOf(calc.calculate("frog", "fog")));
		assertEquals(Float.valueOf(0f), Float.valueOf(calc.calculate("frog", "forg")));
		assertEquals(Float.valueOf(0f), Float.valueOf(calc.calculate("fly", "ant")));
		assertEquals(Float.valueOf(0f), Float.valueOf(calc.calculate("elephant", "hippo")));
		assertEquals(Float.valueOf(0f), Float.valueOf(calc.calculate("hippo", "elephant")));
		assertEquals(Float.valueOf(0f), Float.valueOf(calc.calculate("hippo", "zzzzzzzz")));
		assertEquals(Float.valueOf(0f), Float.valueOf(calc.calculate("hallo", "halo")));
		assertEquals(Float.valueOf(0f), Float.valueOf(calc.calculate("hallo", "ahlo")));
		assertEquals(Float.valueOf(0f), Float.valueOf(calc.calculate("hallo", "ehlo")));
		assertEquals(Float.valueOf(0f), Float.valueOf(calc.calculate("frog", "frig")));
		assertEquals(Float.valueOf(0f), Float.valueOf(calc.calculate("hello", "hallo")));
	}

	/**
	 * Test methods for 
	 * {@link org.vivoweb.harvester.score.algorithm.EqualityExtraTest#calculate(java.lang.CharSequence, java.lang.CharSequence) calculate(CharSequence itemX, CharSequence itemY)}
	 * {@link org.vivoweb.harvester.score.algorithm.EqualityExtraTest#calculate(java.lang.CharSequence, java.lang.CharSequence, java.lang.String) calculate(CharSequence itemX, CharSequence itemY, String commonNames)}
	 */
	public final void testEqualityExtraTestCalculate() {
		Algorithm calc = new EqualityExtraTest();
		assertEquals(Float.valueOf(1.1f), Float.valueOf(calc.calculate("hajjar", "hajjar", "smith")));
		assertEquals(Float.valueOf(1f), Float.valueOf(calc.calculate("smith", "smith", "smith")));
		assertEquals(Float.valueOf(1f), Float.valueOf(calc.calculate("test", "test")));
		assertEquals(Float.valueOf(0f), Float.valueOf(calc.calculate("", "")));
		assertEquals(Float.valueOf(0f), Float.valueOf(calc.calculate("", "a")));
		assertEquals(Float.valueOf(0f), Float.valueOf(calc.calculate("aaapppp", "")));
		assertEquals(Float.valueOf(0f), Float.valueOf(calc.calculate("frog", "fog")));
		assertEquals(Float.valueOf(0f), Float.valueOf(calc.calculate("frog", "forg")));
		assertEquals(Float.valueOf(0f), Float.valueOf(calc.calculate("fly", "ant")));
		assertEquals(Float.valueOf(0f), Float.valueOf(calc.calculate("elephant", "hippo")));
		assertEquals(Float.valueOf(0f), Float.valueOf(calc.calculate("hippo", "elephant")));
		assertEquals(Float.valueOf(0f), Float.valueOf(calc.calculate("hippo", "zzzzzzzz")));
		assertEquals(Float.valueOf(0f), Float.valueOf(calc.calculate("hallo", "halo")));
		assertEquals(Float.valueOf(0f), Float.valueOf(calc.calculate("hallo", "ahlo")));
		assertEquals(Float.valueOf(0f), Float.valueOf(calc.calculate("hallo", "ehlo")));
		assertEquals(Float.valueOf(0f), Float.valueOf(calc.calculate("frog", "frig")));
		assertEquals(Float.valueOf(0f), Float.valueOf(calc.calculate("hello", "hallo")));
	}

	/**
	 * Test method for {@link org.vivoweb.harvester.score.algorithm.CaseInsensitiveInitialTest#calculate(java.lang.CharSequence, java.lang.CharSequence) calculate(CharSequence itemX, CharSequence itemY)}
	 */
	public final void testCaseInsensitiveInitialTestCalculate() {
		Algorithm calc = new CaseInsensitiveInitialTest();
		assertEquals(Float.valueOf(1f), Float.valueOf(calc.calculate("test", "test")));
		assertEquals(Float.valueOf(0f), Float.valueOf(calc.calculate("", "")));
		assertEquals(Float.valueOf(0f), Float.valueOf(calc.calculate("", "a")));
		assertEquals(Float.valueOf(0f), Float.valueOf(calc.calculate("aaapppp", "")));
		assertEquals(Float.valueOf(1f), Float.valueOf(calc.calculate("frog", "fog")));
		assertEquals(Float.valueOf(1f), Float.valueOf(calc.calculate("Frog", "forg")));
		assertEquals(Float.valueOf(0f), Float.valueOf(calc.calculate("fly", "ant")));
		assertEquals(Float.valueOf(0f), Float.valueOf(calc.calculate("elephant", "hippo")));
		assertEquals(Float.valueOf(0f), Float.valueOf(calc.calculate("hippo", "elephant")));
		assertEquals(Float.valueOf(0f), Float.valueOf(calc.calculate("hippo", "zzzzzzzz")));
		assertEquals(Float.valueOf(1f), Float.valueOf(calc.calculate("hallo", "halo")));
		assertEquals(Float.valueOf(0f), Float.valueOf(calc.calculate("hallo", "ahlo")));
		assertEquals(Float.valueOf(0f), Float.valueOf(calc.calculate("hallo", "ehlo")));
		assertEquals(Float.valueOf(1f), Float.valueOf(calc.calculate("frog", "frig")));
		assertEquals(Float.valueOf(1f), Float.valueOf(calc.calculate("hello", "hallo")));
	}
	
	/**
	 * Test method for {@link org.vivoweb.harvester.score.algorithm.NormalizedLevenshteinDifference#calculate(java.lang.CharSequence, java.lang.CharSequence) calculate(CharSequence itemX, CharSequence itemY)}
	 */
	public final void testNormalizedLevenshteinDifferenceCalculate() {
		Algorithm calc = new NormalizedLevenshteinDifference();
		assertEquals(Float.valueOf(1f), Float.valueOf(calc.calculate("test", "test")));
		assertEquals(Float.valueOf(0f), Float.valueOf(calc.calculate("", "")));
		assertEquals(Float.valueOf(0f), Float.valueOf(calc.calculate("", "a")));
		assertEquals(Float.valueOf(0f), Float.valueOf(calc.calculate("aaapppp", "")));
		assertEquals(Float.valueOf(3/4f), Float.valueOf(calc.calculate("frog", "fog")));
		assertEquals(Float.valueOf(1/2f), Float.valueOf(calc.calculate("frog", "forg")));
		assertEquals(Float.valueOf(0f), Float.valueOf(calc.calculate("fly", "ant")));
		assertEquals(Float.valueOf(1/8f), Float.valueOf(calc.calculate("elephant", "hippo")));
		assertEquals(Float.valueOf(1/8f), Float.valueOf(calc.calculate("hippo", "elephant")));
		assertEquals(Float.valueOf(0f), Float.valueOf(calc.calculate("hippo", "zzzzzzzz")));
		assertEquals(Float.valueOf(4/5f), Float.valueOf(calc.calculate("hallo", "halo")));
		assertEquals(Float.valueOf(3/5f), Float.valueOf(calc.calculate("hallo", "ahlo")));
		assertEquals(Float.valueOf(2/5f), Float.valueOf(calc.calculate("hallo", "ehlo")));
		assertEquals(Float.valueOf(3/4f), Float.valueOf(calc.calculate("frog", "frig")));
		assertEquals(Float.valueOf(4/5f), Float.valueOf(calc.calculate("hello", "hallo")));
	}
	
	/**
	 * Test method for {@link org.vivoweb.harvester.score.algorithm.NormalizedTypoDifference#calculate(java.lang.CharSequence, java.lang.CharSequence) calculate(CharSequence itemX, CharSequence itemY)}
	 */
	public final void testNormalizedTypoDifferenceCalculate() {
		Algorithm calc = new NormalizedTypoDifference();
		assertEquals(Float.valueOf(1f), Float.valueOf(calc.calculate("test", "test")));
		assertEquals(Float.valueOf(0f), Float.valueOf(calc.calculate("", "")));
		assertEquals(Float.valueOf(0f), Float.valueOf(calc.calculate("", "a")));
		assertEquals(Float.valueOf(0f), Float.valueOf(calc.calculate("aaapppp", "")));
		assertEquals(Float.valueOf(3/4f), Float.valueOf(calc.calculate("frog", "fog")));
		assertEquals(Float.valueOf(3/4f), Float.valueOf(calc.calculate("frog", "forg")));
		assertEquals(Float.valueOf((3-2.7f)/3), Float.valueOf(calc.calculate("fly", "ant")));
		assertEquals(Float.valueOf((8-6.7f)/8), Float.valueOf(calc.calculate("elephant", "hippo")));
		assertEquals(Float.valueOf((8-6.7f)/8), Float.valueOf(calc.calculate("hippo", "elephant")));
		assertEquals(Float.valueOf(0f), Float.valueOf(calc.calculate("hippo", "zzzzzzzz")));
		assertEquals(Float.valueOf((5-0.7f)/5), Float.valueOf(calc.calculate("hallo", "halo"))); // TODO: eh? what happened here
		assertEquals(Float.valueOf((5-1.7f)/5), Float.valueOf(calc.calculate("hallo", "ahlo")));
		assertEquals(Float.valueOf((5-2.7f)/5), Float.valueOf(calc.calculate("hallo", "ehlo")));
		assertEquals(Float.valueOf((4-0.7f)/4), Float.valueOf(calc.calculate("frog", "frig"))); // TODO: eh? what happened here
		assertEquals(Float.valueOf(4/5f), Float.valueOf(calc.calculate("hello", "hallo"))); // TODO: eh? what happened here
	}
	
	/**
	 * Test method for {@link org.vivoweb.harvester.score.algorithm.NameCompare#calculate(java.lang.CharSequence, java.lang.CharSequence) calculate(CharSequence itemX, CharSequence itemY)}
	 */
	public final void testNameCompareCalculate() {
		Algorithm calc = new NameCompare();
		assertEquals(Float.valueOf(1f), Float.valueOf(calc.calculate("test", "test")));
		assertEquals(Float.valueOf(0f), Float.valueOf(calc.calculate("", "")));
		assertEquals(Float.valueOf(0f), Float.valueOf(calc.calculate("", "a")));
		assertEquals(Float.valueOf(0f), Float.valueOf(calc.calculate("aaapppp", "")));
		assertEquals(Float.valueOf(3/4f), Float.valueOf(calc.calculate("frog", "fog")));
		assertEquals(Float.valueOf(3/4f), Float.valueOf(calc.calculate("frog", "forg")));
		assertEquals(Float.valueOf(0f), Float.valueOf(calc.calculate("fly", "ant")));
		assertEquals(Float.valueOf(1/8f), Float.valueOf(calc.calculate("elephant", "hippo")));
		assertEquals(Float.valueOf(1/8f), Float.valueOf(calc.calculate("hippo", "elephant")));
		assertEquals(Float.valueOf(0f), Float.valueOf(calc.calculate("hippo", "zzzzzzzz")));
		assertEquals(Float.valueOf(4/5f), Float.valueOf(calc.calculate("hallo", "halo")));
		assertEquals(Float.valueOf(3/5f), Float.valueOf(calc.calculate("hallo", "ahlo")));
		assertEquals(Float.valueOf(2/5f), Float.valueOf(calc.calculate("hallo", "ehlo")));
		assertEquals(Float.valueOf(3/4f), Float.valueOf(calc.calculate("frog", "frig")));
		assertEquals(Float.valueOf(4/5f), Float.valueOf(calc.calculate("hello", "hallo")));
	}

	/**
	 * Test method for {@link org.vivoweb.harvester.score.algorithm.NameExtraCompare#calculate(java.lang.CharSequence, java.lang.CharSequence) calculate(CharSequence itemX, CharSequence itemY)}
	 */
	public final void testNameExtraCompareCalculate() {
		Algorithm calc = new NameExtraCompare();
		assertEquals(Float.valueOf(0.9f), Float.valueOf(calc.calculate("t", "test")));
		assertEquals(Float.valueOf(0.9f), Float.valueOf(calc.calculate("t", "t")));
		assertEquals(Float.valueOf(1f), Float.valueOf(calc.calculate("test", "test")));
		assertEquals(Float.valueOf(0f), Float.valueOf(calc.calculate("", "")));
		assertEquals(Float.valueOf(0f), Float.valueOf(calc.calculate("", "a")));
		assertEquals(Float.valueOf(0f), Float.valueOf(calc.calculate("aaapppp", "")));
		assertEquals(Float.valueOf(3/4f), Float.valueOf(calc.calculate("frog", "fog")));
		assertEquals(Float.valueOf(3/4f), Float.valueOf(calc.calculate("frog", "forg")));
		assertEquals(Float.valueOf(0f), Float.valueOf(calc.calculate("fly", "ant")));
		assertEquals(Float.valueOf(1/8f), Float.valueOf(calc.calculate("elephant", "hippo")));
		assertEquals(Float.valueOf(1/8f), Float.valueOf(calc.calculate("hippo", "elephant")));
		assertEquals(Float.valueOf(0f), Float.valueOf(calc.calculate("hippo", "zzzzzzzz")));
		assertEquals(Float.valueOf(4/5f), Float.valueOf(calc.calculate("hallo", "halo")));
		assertEquals(Float.valueOf(3/5f), Float.valueOf(calc.calculate("hallo", "ahlo")));
		assertEquals(Float.valueOf(2/5f), Float.valueOf(calc.calculate("hallo", "ehlo")));
		assertEquals(Float.valueOf(3/4f), Float.valueOf(calc.calculate("frog", "frig")));
		assertEquals(Float.valueOf(4/5f), Float.valueOf(calc.calculate("hello", "hallo")));
	}

	/**
	 * Test method for {@link org.vivoweb.harvester.score.algorithm.NormalizedDoubleMetaphoneDifference#calculate(java.lang.CharSequence, java.lang.CharSequence) calculate(CharSequence itemX, CharSequence itemY)}
	 */
	public final void testNormalizedDoubleMetaphoneDifferenceCalculate() {
		Algorithm calc = new NormalizedDoubleMetaphoneDifference();
		assertEquals(Float.valueOf(1f), Float.valueOf(calc.calculate("test", "test")));
		assertEquals(Float.valueOf(0f), Float.valueOf(calc.calculate("", "")));
		assertEquals(Float.valueOf(0f), Float.valueOf(calc.calculate("", "a")));
		assertEquals(Float.valueOf(0f), Float.valueOf(calc.calculate("aaapppp", "")));
		assertEquals(Float.valueOf(2/3f), Float.valueOf(calc.calculate("frog", "fog")));
		assertEquals(Float.valueOf(1f), Float.valueOf(calc.calculate("frog", "forg")));
		assertEquals(Float.valueOf(0f), Float.valueOf(calc.calculate("fly", "ant")));
		assertEquals(Float.valueOf(0f), Float.valueOf(calc.calculate("elephant", "hippo")));
		assertEquals(Float.valueOf(0f), Float.valueOf(calc.calculate("hippo", "elephant")));
		assertEquals(Float.valueOf(0f), Float.valueOf(calc.calculate("hippo", "zzzzzzzz")));
		assertEquals(Float.valueOf(1f), Float.valueOf(calc.calculate("hallo", "halo")));
		assertEquals(Float.valueOf(1/2f), Float.valueOf(calc.calculate("hallo", "ahlo")));
		assertEquals(Float.valueOf(1/2f), Float.valueOf(calc.calculate("hallo", "ehlo")));
		assertEquals(Float.valueOf(1f), Float.valueOf(calc.calculate("frog", "frig")));
		assertEquals(Float.valueOf(1f), Float.valueOf(calc.calculate("hello", "hallo")));
	}
	
	/**
	 * Test method for {@link org.vivoweb.harvester.score.algorithm.NormalizedSoundExDifference#calculate(java.lang.CharSequence, java.lang.CharSequence) calculate(CharSequence itemX, CharSequence itemY)}
	 */
	public final void testNormalizedSoundExDifferenceCalculate() {
		Algorithm calc = new NormalizedSoundExDifference();
		assertEquals(Float.valueOf(1f), Float.valueOf(calc.calculate("test", "test")));
		assertEquals(Float.valueOf(0f), Float.valueOf(calc.calculate("", "")));
		assertEquals(Float.valueOf(0f), Float.valueOf(calc.calculate("", "a")));
		assertEquals(Float.valueOf(0f), Float.valueOf(calc.calculate("aaapppp", "")));
		assertEquals(Float.valueOf(1/2f), Float.valueOf(calc.calculate("frog", "fog")));
		assertEquals(Float.valueOf(1f), Float.valueOf(calc.calculate("frog", "forg")));
		assertEquals(Float.valueOf(1/4f), Float.valueOf(calc.calculate("fly", "ant")));
		assertEquals(Float.valueOf(0f), Float.valueOf(calc.calculate("elephant", "hippo")));
		assertEquals(Float.valueOf(0f), Float.valueOf(calc.calculate("hippo", "elephant")));
		assertEquals(Float.valueOf(1/2f), Float.valueOf(calc.calculate("hippo", "zzzzzzzz")));
		assertEquals(Float.valueOf(1f), Float.valueOf(calc.calculate("hallo", "halo")));
		assertEquals(Float.valueOf(3/4f), Float.valueOf(calc.calculate("hallo", "ahlo")));
		assertEquals(Float.valueOf(3/4f), Float.valueOf(calc.calculate("hallo", "ehlo")));
		assertEquals(Float.valueOf(1f), Float.valueOf(calc.calculate("frog", "frig")));
		assertEquals(Float.valueOf(1f), Float.valueOf(calc.calculate("hello", "hallo")));
	}

}

package ca.phon.phonex;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import ca.phon.ipa.IPATranscript;
import ca.phon.ipa.features.FeatureMatrix;
import ca.phon.ipa.parser.IPATokenType;
import ca.phon.ipa.parser.IPATokens;
import ca.phon.phonex.PhonexMatcher;
import ca.phon.phonex.PhonexPattern;

@RunWith(JUnit4.class)
public class TestPredefinedPhoneClasses {

	@Test
	public void testConsonantPhoneClass() throws ParseException {
		final String phonex = "\\c";
		final PhonexPattern pattern = PhonexPattern.compile(phonex);
		
		final FeatureMatrix fm = FeatureMatrix.getInstance();
		final Collection<Character> testChars = fm.getCharactersWithFeature("Consonant");
		
		// test all ipa characters
		for(Character c:testChars) {
			final IPATranscript transcript = IPATranscript.parseIPATranscript(c + "");
			Assert.assertEquals(1, transcript.length());
			final PhonexMatcher matcher = pattern.matcher(transcript);
		
			Assert.assertEquals(true, matcher.matches());
		}
	}
	
	@Test
	public void testVowelPhoneClass() throws ParseException {
		final String phonex = "\\v";
		final PhonexPattern pattern = PhonexPattern.compile(phonex);
		
		final FeatureMatrix fm = FeatureMatrix.getInstance();
		final Collection<Character> testChars = fm.getCharactersWithFeature("Vowel");
		
		// test all ipa characters
		for(Character v:testChars) {
			final IPATranscript transcript = IPATranscript.parseIPATranscript(v + "");
			final PhonexMatcher matcher = pattern.matcher(transcript);
		
			Assert.assertEquals(true, matcher.matches());
		}
	}
	
	@Test
	public void testGlidePhoneClass() throws ParseException {
		final String phonex = "\\g";
		final PhonexPattern pattern = PhonexPattern.compile(phonex);
		
		final FeatureMatrix fm = FeatureMatrix.getInstance();
		final Collection<Character> testChars = fm.getCharactersWithFeature("Glide");
		
		// test all ipa characters
		for(Character g:testChars) {
			final IPATranscript transcript = IPATranscript.parseIPATranscript(g + "");
			final PhonexMatcher matcher = pattern.matcher(transcript);
		
			Assert.assertEquals(true, matcher.matches());
		}
	}
	
	@Test
	public void testWordClass() throws ParseException {
		final String phonex = "\\w";
		final PhonexPattern pattern = PhonexPattern.compile(phonex);
		
		final FeatureMatrix fm = FeatureMatrix.getInstance();
		final Set<Character> testChars = new TreeSet<Character>();
		testChars.addAll(fm.getCharactersWithFeature("Consonant"));
		testChars.addAll(fm.getCharactersWithFeature("Vowel"));
		
		// test all ipa characters
		for(Character v:testChars) {
			final IPATranscript transcript = IPATranscript.parseIPATranscript(v + "");
			final PhonexMatcher matcher = pattern.matcher(transcript);
		
			Assert.assertEquals(true, matcher.matches());
		}
	}
}

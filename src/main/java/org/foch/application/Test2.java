package org.foch.application;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Test2 {

	    public static void main(String[] args) {
//	        String sentence = "This is a test \"for me\"";
//	        List<String> words = splitSentence(sentence);
//	        for (String word : words) {
//	            System.out.println(word);
//	        }
	    	
	    	var w = "test00é";
	    	System.out.println(normalize(w));
	    }

	    public static List<String> splitSentence(String sentence) {
	        List<String> words = new ArrayList<>();
	        Pattern pattern = Pattern.compile("\"[^\"]*\"|\\S+");
	        Matcher matcher = pattern.matcher(sentence);
	        while (matcher.find()) {
	            words.add(matcher.group().replaceAll("\"", ""));
	        }
	        return words;
	    }
	    
	    private static String normalize(String word) {
	        // Remove special characters except '*', and convert to lowercase
	        String normalized = removeAccents(word).replaceAll("[^\\p{L}'\\d\\s*]", "");
	        normalized = normalized.replaceAll("[~/\\\\]", "").replace("*", "\\w*");
	        return normalized.toLowerCase();
	    }

	    private static String removeAccents(String input) {
	        return Normalizer.normalize(input, Normalizer.Form.NFD)
	                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
	    }
		
}

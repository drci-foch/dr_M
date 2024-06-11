package org.foch.application;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.text.StringEscapeUtils;


	public class Test {
		
	
	    public static void main(String[] args) throws Exception {
//	    	String htmlContent = "<span> this is l'a test with MULTIPLé words</span>";
	    	String htmlContent = Files.readString(Paths.get("D:/test.txt"));
	    	htmlContent = convertHtmlString(htmlContent);
	    	List<String> wordsToHighlight = new ArrayList<>();
	        wordsToHighlight.add("angiosc*");
	        wordsToHighlight.add("kaliémie");
	        wordsToHighlight.add("dose");
	        wordsToHighlight.add("l'a");
	        wordsToHighlight.add("tes*");
	        wordsToHighlight.add("multiplé wo*");
	        String highlightedContent = highlightWords(htmlContent, wordsToHighlight, "highlighted");
	        Files.write(Paths.get("D:/test2.txt"), highlightedContent.getBytes("UTF-8"));
	        System.out.println(highlightedContent);
	    }

	    public static String convertHtmlString(String htmlContent) {
	        return StringEscapeUtils.unescapeHtml4(htmlContent);
	    }
	    
	    public static String highlightWords(String htmlContent, List<String> wordsToHighlight, String cssClass) {
	        // Build regex pattern to match the words
	        String regex = "\\b(" + buildRegexPattern(wordsToHighlight) + ")\\b";
	        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);

	        // Replace matched words with highlighted versions
	        Matcher matcher = pattern.matcher(removeAccents(htmlContent));
	        List<MatchPosition> matchPositions = new ArrayList<>();
	        while (matcher.find()) {
	            matchPositions.add(new MatchPosition(matcher.start(), matcher.end()));
	        }
	        StringBuilder result = new StringBuilder();
	        int prevEnd = 0;
	        for (MatchPosition matchPosition : matchPositions) {
	            int start = matchPosition.start;
	            int end = matchPosition.end;
	            result.append(htmlContent.substring(prevEnd, start))
	                  .append("<span class=\"").append(cssClass).append("\">")
	                  .append(htmlContent.substring(start, end))
	                  .append("</span>");
	            prevEnd = end;
	        }
	        result.append(htmlContent.substring(prevEnd));
	        return result.toString();
	    }

	    private static String buildRegexPattern(List<String> wordsToHighlight) {
	        List<String> normalizedWords = new ArrayList<>();
	        for (String word : wordsToHighlight) {
	            normalizedWords.add(normalize(word));
	        }
	        return String.join("|", normalizedWords);
	    }

	    private static String normalize(String word) {
	        // Remove special characters except '*', and convert to lowercase
	    	String normalized = removeAccents(word).replaceAll("[^\\p{L}'\\s*]", "");
	        normalized = normalized.replaceAll("[~/\\\\]", "").replace("*", "\\w*");
	        return normalized.toLowerCase();
	    }
	    
	    private static String removeAccents(String input) {
	        return Normalizer.normalize(input, Normalizer.Form.NFD)
	                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
	    }
	    
	    private static class MatchPosition {
	        int start;
	        int end;

	        MatchPosition(int start, int end) {
	            this.start = start;
	            this.end = end;
	        }
	    }
	}

	


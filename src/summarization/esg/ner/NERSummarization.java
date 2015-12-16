package summarization.esg.ner;

import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ie.crf.*;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.util.ArrayUtils;
import edu.stanford.nlp.util.Triple;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FileUtils;

import com.boundary.sentence.TextContent;

/**
 * This is a demo of calling CRFClassifier programmatically.
 * <p>
 * Usage:
 * {@code java -mx400m -cp "*" NERDemo [serializedClassifier [fileName]] }
 * <p>
 * If arguments aren't specified, they default to
 * classifiers/english.all.3class.distsim.crf.ser.gz and some hardcoded sample
 * text. If run with arguments, it shows some of the ways to get k-best
 * labelings and probabilities out with CRFClassifier. If run without arguments,
 * it shows some of the alternative output formats that you can get.
 * <p>
 * To use CRFClassifier from the command line:
 * </p>
 * <blockquote>
 * {@code java -mx400m edu.stanford.nlp.ie.crf.CRFClassifier -loadClassifier [classifier] -textFile [file] }
 * </blockquote>
 * <p>
 * Or if the file is already tokenized and one word per line, perhaps in a
 * tab-separated value format with extra columns for part-of-speech tag, etc.,
 * use the version below (note the 's' instead of the 'x'):
 * </p>
 * <blockquote>
 * {@code java -mx400m edu.stanford.nlp.ie.crf.CRFClassifier -loadClassifier [classifier] -testFile [file] }
 * </blockquote>
 *
 * @author Jenny Finkel
 * @author Christopher Manning
 */

public class NERSummarization {

	private static final String SEVEN_CLASS_CLASSIFIER = "classifiers/english.muc.7class.distsim.crf.ser.gz";
	private static AbstractSequenceClassifier<CoreLabel> classifier;
	private File[] listOfFiles;
	private String[] articleContents;
	private int[] groupOne, groupTwo;

	public NERSummarization(String folderPath) {
		try {
			classifier = CRFClassifier.getClassifier(SEVEN_CLASS_CLASSIFIER);
		} catch (ClassCastException | ClassNotFoundException | IOException e) {
			System.out.println("Error loading classification model");
		}

		listOfFiles = new File(folderPath).listFiles();
		articleContents = new String[listOfFiles.length];
	}

	public void parseFiles() {

		for (int i = 0; i < listOfFiles.length; i++) {
			try {
				articleContents[i] = FileUtils.readFileToString(listOfFiles[i]);
			} catch (IOException e) {
				System.out.println("Error parsing file: " + listOfFiles[i]);
			}
		}
	}

	public String[] getArticleContents() {
		return articleContents;
	}

	public String[] extractSentence(String content) {
		TextContent t = new TextContent(); // creating TextContent object
		t.setText(content);
		t.setSentenceBoundary();
		String[] sentences = t.getSentence();
		return sentences;
	}

	public void extractNER(String[] articleSentences) {

		groupOne = new int[articleSentences.length];
		groupTwo = new int[articleSentences.length];
		int j = 0;
		String first = "", second = "", none = "";
		for (String sentence : articleSentences) {
			groupOne[j] = 0;
			groupTwo[j] = 0;
			
//			articleSentences[j] = removeGarbage(articleSentences[j]);

			List<Triple<String, Integer, Integer>> triples = classifier.classifyToCharacterOffsets(sentence);
			
			System.out.println(triples.toString());
			for (Triple<String, Integer, Integer> trip : triples) {

				if (trip.asList().contains("MONEY") || trip.asList().contains("PERCENT")) {
					groupTwo[j]++;
				}
				if (trip.asList().contains("ORGANIZATION") || trip.asList().contains("PERSON")
						|| trip.asList().contains("LOCATION") || trip.asList().contains("DATE")
						|| trip.asList().contains("TIME")) {
					groupOne[j]++;
				}

			}
			j++;
		}
		for(int i = 0; i < groupOne.length; i++){
			System.out.print(groupOne[i] + " ");
		}
		System.out.println("---");
		for(int i = 0; i < groupOne.length; i++){
			System.out.print(groupTwo[i] + " ");
		}
		System.out.println("---");
		int length = groupOne.length;
		for(int i = 0; i < groupOne.length; i++){
			groupOne[i] = groupOne[i]* (length - i);
		}
		for(int i = 0; i < groupOne.length; i++){
			groupTwo[i] = groupTwo[i]* (length - i);
		}
		
		
		for(int i = 0; i < groupOne.length; i++){
			System.out.print(groupOne[i] + "\t");
		}
		System.out.println("---");
		for(int i = 0; i < groupOne.length; i++){
			System.out.print(groupTwo[i] + "\t");
		}
		int[] finalGroupOne = new int[groupOne.length];
		int[] finalGroupTwo = new int[groupTwo.length];
		for(int i = 0; i < 5; i++){
			int max = Arrays.stream(groupOne).max().getAsInt();
			if(max > 0){
			for (int x = 0; x < groupOne.length; x++){
				if(groupOne[x] == max){
					groupOne[x] = 0;
					finalGroupOne[x] = max;
				}
			}
			}
			max = Arrays.stream(groupTwo).max().getAsInt();
			if(max > 0){
			for (int x = 0; x < groupTwo.length; x++){
				if(groupTwo[x] == max){
					groupTwo[x] = 0;
					finalGroupTwo[x] = max;
				}
			}
			}
		}
		System.out.println("\n");
		for (int i = 0; i <  finalGroupOne.length; i++){
			System.out.print(finalGroupOne[i] + "\t");
		}
		System.out.println("\n");

		for (int i = 0; i <  finalGroupOne.length; i++){
			System.out.print(finalGroupTwo[i] + "\t");
		}
		
		String firstPara = "", secondPara = "";
		
		System.out.println("\n\n\n");
		for (int i = 0; i < finalGroupOne.length; i ++){
			if(finalGroupOne[i] == 0 && finalGroupTwo[i] ==0){
				continue;
			}
			else if(finalGroupOne[i] != 0 && finalGroupTwo[i] == 0){
				firstPara += articleSentences[i] + "\n";
			}
			else if(finalGroupOne [i] == 0 && finalGroupTwo[i] != 0){
				secondPara += articleSentences[i] + "\n";
			}
			else{
				if(finalGroupOne[i] > finalGroupTwo[i]){
					firstPara += articleSentences[i] + "\n";
				}
				else if (finalGroupOne[i] < finalGroupTwo[i]){
					secondPara += articleSentences[i] + "\n";
				}
				else{
					firstPara += articleSentences[i] + "\n";
				}
			}
		}

		System.out.println(firstPara + "\n\n---\n\n" + secondPara);
	}
	
	public static String removeGarbage(String sentence) {
		sentence = sentence.replaceAll("[^\\p{ASCII}]", ""); // Strips off non-ascii
													// characters
		sentence = sentence.replaceAll("\\s+", " ");
		sentence = sentence.replaceAll("\\p{Cntrl}", ""); // Strips off ascii
															// control
															// characters
		sentence = sentence.replaceAll("[\\P{Print}]", "");; // Strips off ascii
																// non-printable
																// characters
		sentence = sentence.replaceAll("\\p{C}", ""); // Strips off
														// non-printable
														// characters from
														// unicode
		return sentence;
	}// end method

	public static void main(String[] args) throws Exception {

		NERSummarization nerSum = new NERSummarization(args[0]);
		nerSum.parseFiles();
		String[] documents = nerSum.getArticleContents();
		for (String document : documents) {
			String[] sentences = nerSum.extractSentence(document);
			nerSum.extractNER(sentences);
		}
	}
}
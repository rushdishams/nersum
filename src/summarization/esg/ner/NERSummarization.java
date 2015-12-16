package summarization.esg.ner;
import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ie.crf.*;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.util.Triple;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;

import com.boundary.sentence.TextContent;


/** This is a demo of calling CRFClassifier programmatically.
 *  <p>
 *  Usage: {@code java -mx400m -cp "*" NERDemo [serializedClassifier [fileName]] }
 *  <p>
 *  If arguments aren't specified, they default to
 *  classifiers/english.all.3class.distsim.crf.ser.gz and some hardcoded sample text.
 *  If run with arguments, it shows some of the ways to get k-best labelings and
 *  probabilities out with CRFClassifier. If run without arguments, it shows some of
 *  the alternative output formats that you can get.
 *  <p>
 *  To use CRFClassifier from the command line:
 *  </p><blockquote>
 *  {@code java -mx400m edu.stanford.nlp.ie.crf.CRFClassifier -loadClassifier [classifier] -textFile [file] }
 *  </blockquote><p>
 *  Or if the file is already tokenized and one word per line, perhaps in
 *  a tab-separated value format with extra columns for part-of-speech tag,
 *  etc., use the version below (note the 's' instead of the 'x'):
 *  </p><blockquote>
 *  {@code java -mx400m edu.stanford.nlp.ie.crf.CRFClassifier -loadClassifier [classifier] -testFile [file] }
 *  </blockquote>
 *
 *  @author Jenny Finkel
 *  @author Christopher Manning
 */

public class NERSummarization {

	private static final String SEVEN_CLASS_CLASSIFIER = "classifiers/english.muc.7class.distsim.crf.ser.gz";
	private static AbstractSequenceClassifier<CoreLabel> classifier;
	private File[] listOfFiles;
	private String[] articleContents;
	private String[] featureVector;

	public NERSummarization(String folderPath){
		try {
			classifier = CRFClassifier.getClassifier(SEVEN_CLASS_CLASSIFIER);
		} catch (ClassCastException | ClassNotFoundException | IOException e) {
			System.out.println("Error loading classification model");
		}

		listOfFiles = new File(folderPath).listFiles();
		articleContents = new String[listOfFiles.length];
	}

	public void parseFiles(){

		for(int i = 0; i < listOfFiles.length; i ++){
			try {
				articleContents[i] = FileUtils.readFileToString(listOfFiles[i]);
			} catch (IOException e) {
				System.out.println("Error parsing file: " + listOfFiles[i]);
			}
		}
	}
	
	public String[] getArticleContents(){
		return articleContents;
	}
	
	public String[] extractSentence(String content){
		TextContent t = new TextContent(); //creating TextContent object
		t.setText(content);
		t.setSentenceBoundary();
		String[] sentences = t.getSentence();
		return sentences;
	}
	
	public void extractNER(String[] articleSentences){
		int j = 0;
		String first = "", second = "", none = "";
		for (String sentence : articleSentences) {
			boolean foundFirst = false, foundSecond = false, foundNone = true;
			j++;
			List<Triple<String,Integer,Integer>> triples = classifier.classifyToCharacterOffsets(sentence);
			System.out.println(sentence);
			for (int i = 0; i < triples.size(); i++){
				System.out.print (triples.get(i) + " ");
			}
			System.out.println("\n");
			
			for (Triple<String,Integer,Integer> trip : triples) {
				
			
					foundNone = false;
				
//				System.out.println(trip.asList().toString());
				if(trip.asList().contains("MONEY") || trip.asList().contains("PERCENT")){
					foundSecond = true;
//					break;
				}
				else{
					foundFirst = true;
				}
				
			}
			if(foundNone){
				none += sentence;
			}
			else if(foundSecond){
				
				second += sentence + "\n";
			}
			else{
				first += sentence + "\n";
			}
	/*		for (Triple<String,Integer,Integer> trip : triples) {
				System.out.printf("%s over character offsets [%d, %d) in sentence %d.%n",
						trip.first(), trip.second(), trip.third, j); 
				System.out.println(str.substring(trip.second(), trip.third));
				
			}*/
		}
		System.out.println("===========FIRST================\n" + first);
		System.out.println("===========SECOND================\n" + second);
		System.out.println("---");
	}
	
	public static void main(String[] args) throws Exception {

		NERSummarization nerSum = new NERSummarization(args[0]);
		nerSum.parseFiles();
		String[] documents = nerSum.getArticleContents();
		for(String document: documents){
			String[] sentences = nerSum.extractSentence(document);
			nerSum.extractNER(sentences);
		}
	}
}
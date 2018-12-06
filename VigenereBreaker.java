import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.stream.Collectors;

import javax.swing.JFileChooser;
import org.apache.commons.io.FileUtils;

public class VigenereBreaker implements AutoCloseable {

	public String sliceString(String message, int whichSlice, int totalSlices) {
		StringBuilder sb = new StringBuilder();

		for (int k = whichSlice; k < message.length(); k += totalSlices) {
			char desiredChar = message.charAt(k);
			sb.append(desiredChar);
		}

		return sb.toString();
	}

	public int[] tryKeyLength(String encrypted, int klength, char mostCommon) {
		int[] key = new int[klength];
		CaesarBreaker cc = new CaesarBreaker();

		for (int i = 0; i < klength; i++) {
			String sliString = sliceString(encrypted, i, klength);
			int currKey = cc.getKey(sliString);
			key[i] = currKey;
		}

		return key;
	}

	public File[] FileResource() throws FileNotFoundException, IOException {
		JFileChooser chooser = new JFileChooser();
		chooser.setCurrentDirectory(new java.io.File("."));
		chooser.setMultiSelectionEnabled(true);
		chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		chooser.showOpenDialog(null);

		File[] files = chooser.getSelectedFiles();

//		for (File file : files) {
//			FileInputStream fileStream = FileUtils.openInputStream(file);
//			InputStream inStream = fileStream;
//		}

		return files;
	}

	public HashSet<String> readDictionary() throws FileNotFoundException, IOException {
		System.out.println("Choose your dictionary...");
		System.out.println("");
		File[] files = FileResource();
		HashSet<String> words = new HashSet<String>();
		for (File file : files) {
			if (file.isFile()) {
				FileInputStream fileStream = FileUtils.openInputStream(file);
				InputStream inStream = fileStream;
				try (BufferedReader br = new BufferedReader(new InputStreamReader(inStream, "UTF-8"))) {
					String line;
					while ((line = br.readLine()) != null) {
						line = line.toLowerCase();
						words.add(line);
					}
				}
			}
		}

		return words;
	}

	public int countWords(String message, HashSet<String> dictionary) {
		int count = 0;
		// split the text in words
		for (String word : message.split("\\W")) {
			String lowerCaseWord = word.toLowerCase();
			// if the word is in the language (ex. English) we count +1
			if (dictionary.contains(lowerCaseWord)) {
				count += 1;
			}

		}

		return count;
	}

	public char mostCommonCharIn(HashSet<String> dictionary) {
		char mostCommonChar = '\0';
		int maxAppearNumber = 0;
		String alphabet = "abcdefghijklmnopqrstuvwxyz";
		HashMap<Character, Integer> charMap = new HashMap<Character, Integer>();

		for (String word : dictionary) {
			word = word.toLowerCase();
			for (int k = 0; k < alphabet.length(); k++) {
				char currChar = alphabet.charAt(k);

				if (word.indexOf(currChar) != -1) {
					if (!charMap.containsKey(currChar)) {
						charMap.put(currChar, 1);
					} else {
						charMap.put(currChar, charMap.get(currChar) + 1);
					}
				}

			}
		}

		for (Character c : charMap.keySet()) {
			int currAppearNumber = charMap.get(c);
			if (currAppearNumber > maxAppearNumber) {
				maxAppearNumber = currAppearNumber;
				mostCommonChar = c;
			}
		}

		return mostCommonChar;
	}

	public String breakForLanguage(String encrypted, HashSet<String> dictionary) {
		String bestDecryption = null;
		int bestCount = 0;
		// try all key lengths from 1 to 100
		int keyRange = 100;
		int bestK = 0;
		char mostCommonChar = '\0';
		String bestKey = null;

		for (int k = 1; k <= keyRange; k++) {
			// use tryKeyLength method to try every key 'k'
			mostCommonChar = mostCommonCharIn(dictionary);
			int[] key = tryKeyLength(encrypted, k, mostCommonChar);
			// use decrypt method form VigenereCipher to decrypt with every key
			VigenereCipher vc = new VigenereCipher(key);
			String decrypted = vc.decrypt(encrypted);
			int currCount = countWords(decrypted, dictionary);

			if (currCount > bestCount) {
				bestCount = currCount;
				bestDecryption = decrypted;
				bestK = k;
				bestKey = Arrays.toString(key);
				// System.out.println("This key: " + Arrays.toString(key) + " with " + k +
				// " length " + " counted " + currCount + " words");
			}

		}
		System.out.println("The most common character is: " + "'" + mostCommonChar + "'");
		System.out.println("This key: " + bestKey + " with " + bestK + " length " + " counted " + bestCount + " words");
		System.out.println("");
		System.out.println("");
		return bestDecryption;
	}

	public void breakForAllLangs(String encrypted, HashMap<String, HashSet<String>> languages) {
		int mostCountWords = 0;
		String bestBreakForLanguage = null;
		String bestLanguage = null;
		for (String language : languages.keySet()) {
			System.out.println("The current language is: " + language);
			// start a dictionary for every language in HashMap
			HashSet<String> currDictionary = languages.get(language);
			// find the best decryption key for every language
			String currBreakForLanguage = breakForLanguage(encrypted, currDictionary);
			// count the most words found in every dictionary
			int currCountWords = countWords(currBreakForLanguage, currDictionary);

			if (currCountWords > mostCountWords) {
				mostCountWords = currCountWords;
				bestBreakForLanguage = currBreakForLanguage;
				bestLanguage = language;
			}

		}
		System.out.println(mostCountWords + " words found in " + bestLanguage);
		System.out.println("The decrypted message is......");
		System.out.println("");
		System.out.println(bestBreakForLanguage);
	}

	public void breakVigenere() throws IOException { // for any language
		System.out.println("Choose your encrypted file...");
		System.out.println("");
		File[] files = FileResource();
		String encrypted = null;
		for (File file : files) {
			if (file.isFile()) {
				FileInputStream fileStream = FileUtils.openInputStream(file);
				InputStream inStream = fileStream;
				try (BufferedReader br = new BufferedReader(new InputStreamReader(inStream, "UTF-8"))) {
					encrypted = br.lines().collect(Collectors.joining("\n"));
				}
			}
		}
		// String encrypted = fr.asString();
		System.out.println("The message length is: " + encrypted.length());
		System.out.println("");

		String[] lang = { "Danish", "Dutch", "English", "French", "German", "Italian", "Portoguese", "Spanish" };

		HashMap<String, HashSet<String>> mapLanguages = new HashMap<String, HashSet<String>>();
		for (int k = 0; k < lang.length; k++) { // making a HashMap with languages as a key
			String currLanguage = lang[k]; // and the dictionary as a value
			if (!mapLanguages.containsKey(currLanguage)) {
				System.out.println("Enter the dictionary for: " + currLanguage);

				HashSet<String> dictionaryWords = readDictionary();
				mapLanguages.put(currLanguage, dictionaryWords);
				System.out.println(currLanguage + " was loaded to the HashMap!");
				System.out.println("");
				System.out.println("");

			}

		}

		breakForAllLangs(encrypted, mapLanguages);
	}

	@Override
	public void close() throws Exception {
		// TODO Auto-generated method stub

	}

}

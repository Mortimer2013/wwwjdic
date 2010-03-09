package org.nick.wwwjdic;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DictionaryEntry implements Serializable {

	/**
     * 
     */
	private static final long serialVersionUID = 2057121563183659358L;

	private static final int WORD_IDX = 0;
	private static final int READING_IDX = 1;

	private static final int SHORT_TRANSLATION_LENGTH = 40;

	private String word;
	private String reading;
	private String partOfSpeech;
	private List<String> meanings = new ArrayList<String>();

	private String tranlsationString;
	private String shortTranslation;
	private String meaningsString;

	private DictionaryEntry() {
	}

	public static DictionaryEntry parseEdict(String edictStr) {
		DictionaryEntry result = new DictionaryEntry();
		String[] fields = edictStr.split(" ");

		result.word = fields[WORD_IDX];
		int firstSpaceIdx = edictStr.indexOf(" ");
		String translationString = edictStr.substring(firstSpaceIdx + 1);
		result.tranlsationString = translationString.replace("/", " ").trim();
		result.shortTranslation = shorten(result.tranlsationString);

		String meaningsField = null;
		int openingBracketIdx = translationString.indexOf('[');
		if (openingBracketIdx != -1) {
			result.reading = fields[READING_IDX].replaceAll("\\[", "")
					.replaceAll("\\]", "");
			int closingBracketIdx = edictStr.indexOf(']');
			meaningsField = edictStr.substring(closingBracketIdx + 2);
		} else {
			int firstSlashIdx = translationString.indexOf('/');
			if (firstSlashIdx != -1) {
				meaningsField = translationString.substring(firstSlashIdx);
			} else {
				meaningsField = translationString.trim();
			}
		}

		result.meaningsString = meaningsField.replace("/", " ").trim();
		result.shortTranslation = shorten(result.meaningsString);

		String[] meaningsArr = meaningsField.split("/");
		int spaceIdx = meaningsArr[1].indexOf(' ');
		result.partOfSpeech = meaningsArr[1].substring(0, spaceIdx);
		result.meanings.add(meaningsArr[1].substring(spaceIdx).trim());

		for (int i = 2; i < meaningsArr.length; i++) {
			String meaning = meaningsArr[i];
			if (!"".equals(meaning)) {
				result.meanings.add(meaning);
			}
		}

		return result;
	}

	private static String shorten(String translationString) {
		if (translationString.length() <= SHORT_TRANSLATION_LENGTH) {
			return translationString;
		}

		String result = translationString.substring(0,
				SHORT_TRANSLATION_LENGTH - 3);
		result += "...";

		return result;
	}

	public String getWord() {
		return word;
	}

	public String getReading() {
		return reading;
	}

	public String getPartOfSpeech() {
		return partOfSpeech;
	}

	public List<String> getMeanings() {
		return Collections.unmodifiableList(meanings);
	}

	public String getTranslationString() {
		return tranlsationString;
	}

	public String getShortTranslation() {
		return shortTranslation;
	}

	public String getMeaningsString() {
		return meaningsString;
	}

}

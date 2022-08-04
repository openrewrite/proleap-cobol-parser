package io.proleap.cobol;

public class StringWithOriginalPositions {

	public String preprocessedText;
	public String originalText;
	public int[] originalPositions;

	public StringWithOriginalPositions(String text, String originalCode, int[] originalPositions) {
		assert text.length() == originalPositions.length;
		this.preprocessedText = text;
		this.originalText = originalCode;
		this.originalPositions = originalPositions;
	}

	// Scaffolding, to be removed.
	public StringWithOriginalPositions(StringWithOriginalPositions code, String expandedText) {
		this.preprocessedText = expandedText;
		this.originalText = code.originalText;
		this.originalPositions = code.originalPositions; // TODO FIXME
//		this.originalPositions = new int[text.length()];
//		for(int i=0; i<originalPositions.length; i++) {
//			originalPositions[i] = i;
//		}
	}

	public String getPreprocessedText(int start, int stop) {
		return preprocessedText.substring(start, stop+1);
	}

	public String getOriginalText(int start, int stop) {
		return originalText.substring(originalPositions[start], originalPositions[stop]+1);
	}
}

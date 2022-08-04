package io.proleap.cobol;

public class StringWithOriginalPositions {

	public String text;
	public String originalCode;
	public int[] originalPositions;

	public StringWithOriginalPositions(String text, String originalCode, int[] originalPositions) {
		assert text.length() == originalPositions.length;
		this.text = text;
		this.originalCode = originalCode;
		this.originalPositions = originalPositions;
	}

	// Scaffolding, to be removed.
	public StringWithOriginalPositions(String text) {
		this.text = text;
		this.originalPositions = new int[text.length()];
		for(int i=0; i<originalPositions.length; i++) {
			originalPositions[i] = i;
		}
	}
}

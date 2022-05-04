package io.proleap.cobol;

public class Marker
{
	int posInProcessedFile;
	int posInOriginalFile;
	String originalFileName;

	public Marker(int posInProcessedFile, int posInOriginalFile) {
		this.posInProcessedFile = posInProcessedFile;
		this.posInOriginalFile = posInOriginalFile;
		this.originalFileName = null;
	}

	public Marker(int posInProcessedFile, int posInOriginalFile, String originalFileName) {
		this.posInProcessedFile = posInProcessedFile;
		this.posInOriginalFile = posInOriginalFile;
		this.originalFileName = originalFileName;
	}
}

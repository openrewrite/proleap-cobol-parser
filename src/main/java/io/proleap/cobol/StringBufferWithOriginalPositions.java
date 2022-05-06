package io.proleap.cobol;

import java.util.ArrayList;
import java.util.List;

public class StringBufferWithOriginalPositions {

	StringWithOriginalPositions originalCodeWithPositions;
	String originalCode;

	StringBuffer sb;
	List<Integer> originalPositions;
	int currentPositionInOriginalFile;
	
	public StringBufferWithOriginalPositions(String originalCode)
	{
		this.originalCodeWithPositions = null;
		this.originalCode = originalCode;

		this.sb = new StringBuffer();
		this.originalPositions = new ArrayList<>();
		this.currentPositionInOriginalFile = 0;
	}
	
	public StringBufferWithOriginalPositions(StringWithOriginalPositions originalCodeWithPositions)
	{
		this.originalCodeWithPositions = originalCodeWithPositions;
		// This original text is the previous string's preprocessed text
		this.originalCode = originalCodeWithPositions.preprocessedText;

		this.sb = new StringBuffer();
		this.originalPositions = new ArrayList<>();
		this.currentPositionInOriginalFile = 0;
	}
	
	public void append(String s) {
		sb.append(s);
		for(int i=0; i<s.length(); i++) {
			if(currentPositionInOriginalFile == 51) {
				System.out.println();
			}
			originalPositions.add(currentPositionInOriginalFile);
			currentPositionInOriginalFile++;
		}
	}

	public StringWithOriginalPositions toStringWithMarkers() {
		return new StringWithOriginalPositions(sb.toString(), originalCode, originalPositions.stream().mapToInt(Integer::intValue).toArray());
	}

	public void skip(String s) {
		skip(s.length());
	}

	public void skip(int i) {
		currentPositionInOriginalFile += i;
	}
}



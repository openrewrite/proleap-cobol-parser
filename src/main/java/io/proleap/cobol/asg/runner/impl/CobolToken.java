package io.proleap.cobol.asg.runner.impl;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.TokenSource;
import org.antlr.v4.runtime.misc.Pair;

public class CobolToken extends CommonToken {
	public CobolToken(Pair<TokenSource, CharStream> source, int type, int channel, int start, int stop) {
		super(source, type, channel, start, stop);
	}
	
	public CobolToken(int type, String text) {
		super(type, text);
	}
}

/*
 * Copyright (C) 2017, Ulrich Wolffgang <ulrich.wolffgang@proleap.io>
 * All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license. See the LICENSE file for details.
 */

package io.proleap.cobol.preprocessor.sub.document.impl;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import io.proleap.cobol.CobolPreprocessorLexer;
import io.proleap.cobol.CobolPreprocessorParser;
import io.proleap.cobol.CobolPreprocessorParser.StartRuleContext;
import io.proleap.cobol.StringWithOriginalPositions;
import io.proleap.cobol.asg.params.CobolParserParams;
import io.proleap.cobol.asg.runner.ThrowingErrorListener;
import io.proleap.cobol.preprocessor.sub.document.CobolDocumentParser;
import io.proleap.cobol.preprocessor.sub.document.CobolDocumentParserListener;

/**
 * Preprocessor, which parses and processes COPY REPLACE and EXEC SQL
 * statements.
 */
public class CobolDocumentParserImpl implements CobolDocumentParser {

	protected final String[] triggers = new String[] { "cbl", "copy", "exec sql", "exec sqlims", "exec cics", "process",
			"replace", "eject", "skip1", "skip2", "skip3", "title" };

	protected boolean containsTrigger(final String code, final String[] triggers) {
		final String codeLowerCase = code.toLowerCase();
		boolean result = false;

		for (final String trigger : triggers) {
			final boolean containsTrigger = codeLowerCase.contains(trigger);

			if (containsTrigger) {
				result = true;
				break;
			}
		}

		return result;
	}

	protected CobolDocumentParserListener createDocumentParserListener(final CobolParserParams params,
			final CommonTokenStream tokens) {
		return new CobolDocumentParserListenerImpl(params, tokens);
	}

	@Override
	public StringWithOriginalPositions processLines(final StringWithOriginalPositions code, final CobolParserParams params) {
		final boolean requiresProcessorExecution = containsTrigger(code.preprocessedText, triggers);
		final StringWithOriginalPositions result;

		if (requiresProcessorExecution) {
			result = processWithParser(code, params);
		} else {
			result = code;
		}

		return result;
	}

	protected StringWithOriginalPositions processWithParser(final StringWithOriginalPositions code, final CobolParserParams params) {
		// run the lexer
		final CobolPreprocessorLexer lexer = new CobolPreprocessorLexer(CharStreams.fromString(code.preprocessedText));

		if (!params.getIgnoreSyntaxErrors()) {
			// register an error listener, so that preprocessing stops on errors
			lexer.removeErrorListeners();
			lexer.addErrorListener(new ThrowingErrorListener());
		}

		// get a list of matched tokens
		final CommonTokenStream tokens = new CommonTokenStream(lexer);

		// pass the tokens to the parser
		final CobolPreprocessorParser parser = new CobolPreprocessorParser(tokens);

		if (!params.getIgnoreSyntaxErrors()) {
			// register an error listener, so that preprocessing stops on errors
			parser.removeErrorListeners();
			parser.addErrorListener(new ThrowingErrorListener());
		}

		// specify our entry point
		final StartRuleContext startRule = parser.startRule();

		// Fix token positions
		System.out.println("------------------------------");
		for(int i=0; i<tokens.size(); i++) {
			Token t = tokens.get(i);
			int start = t.getStartIndex();
			int stop = t.getStopIndex();
			System.out.print(code.preprocessedText.substring(start, stop+1));
		}
		System.out.println("\n------------------------------");
		
		for(int i=0; i<tokens.size(); i++) {
			Token t = tokens.get(i);
			int start = t.getStartIndex();
			int stop = t.getStopIndex();
			if(t.getType() != Token.EOF) {
				int originalStart = code.originalPositions[start];
				int originalStop = code.originalPositions[stop];
				System.out.print(code.originalText.substring(originalStart, originalStop+1));
				System.out.print("");
			}
		}
		System.out.println("\n------------------------------");
		
		for(int i=0; i<tokens.size(); i++) {
			Token t = tokens.get(i);
			int start = t.getStartIndex();
			int stop = t.getStopIndex();
			if(t.getType() != Token.EOF) {
				int originalStart = code.originalPositions[start];
				int originalStop = code.originalPositions[stop];
				System.out.println("token #" + i + " (" + t.getTokenIndex() + ") :");
				System.out.println("   text start=" + start + ", stop=" + stop + " <" + code.preprocessedText.substring(start, stop+1) + ">");
				System.out.println("   orig start=" + originalStart + ", stop=" + originalStop + " <" + code.originalText.substring(originalStart, originalStop+1) + ">");
				System.out.println("   channel=" + t.getChannel());
				String text = code.preprocessedText.substring(start, stop+1);
				String orig = code.originalText.substring(originalStart, originalStop+1);
				//assert text.equals(orig);
				System.out.print("");
			}
		}

		// analyze contained copy books
		final CobolDocumentParserListener listener = createDocumentParserListener(params, tokens);
		final ParseTreeWalker walker = new ParseTreeWalker();

		walker.walk(listener, startRule);

		final String expandedText = listener.context().read();
		return new StringWithOriginalPositions(code, expandedText);
	}
}

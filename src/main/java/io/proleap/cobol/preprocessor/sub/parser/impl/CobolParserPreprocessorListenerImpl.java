/*
 * Copyright (C) 2017, Ulrich Wolffgang <u.wol@wwu.de>
 * All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the BSD 3-clause license. See the LICENSE file for details.
 */

package io.proleap.cobol.preprocessor.sub.parser.impl;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.antlr.v4.runtime.BufferedTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.plexus.util.StringUtils;

import io.proleap.cobol.Cobol85PreprocessorBaseListener;
import io.proleap.cobol.Cobol85PreprocessorParser;
import io.proleap.cobol.Cobol85PreprocessorParser.PseudoTextContext;
import io.proleap.cobol.Cobol85PreprocessorParser.ReplaceClauseContext;
import io.proleap.cobol.Cobol85PreprocessorParser.ReplaceableContext;
import io.proleap.cobol.Cobol85PreprocessorParser.ReplacementContext;
import io.proleap.cobol.Cobol85PreprocessorParser.ReplacingPhraseContext;
import io.proleap.cobol.applicationcontext.CobolGrammarContext;
import io.proleap.cobol.preprocessor.CobolPreprocessor;
import io.proleap.cobol.preprocessor.CobolPreprocessor.CobolDialect;
import io.proleap.cobol.preprocessor.CobolPreprocessor.CobolSourceFormat;
import io.proleap.cobol.preprocessor.sub.tree.impl.CobolHiddenTokenCollectorListenerImpl;
import io.proleap.cobol.preprocessor.sub.util.TokenUtils;

/**
 * ANTLR visitor, which preprocesses a given COBOL program by executing COPY and
 * REPLACE statements.
 */
public class CobolParserPreprocessorListenerImpl extends Cobol85PreprocessorBaseListener {

	/**
	 * A replacement context that defines, which replaceables should be replaced
	 * by which replacements.
	 */
	public class PreprocessingContext {

		/**
		 * A mapping from a replaceable to a replacement.
		 */
		private class ReplacementMapping implements Comparable<ReplacementMapping> {

			private ReplaceableContext replaceable;

			private ReplacementContext replacement;

			@Override
			public int compareTo(final ReplacementMapping o) {
				return o.replaceable.getText().length() - replaceable.getText().length();
			}

			private String extractPseudoText(final PseudoTextContext pseudoTextCtx) {
				final String pseudoText = getTextIncludingHiddenTokens(pseudoTextCtx, tokens).trim();
				final String content = pseudoText.replaceAll("^==", "").replaceAll("==$", "").trim();
				return content;
			}

			/**
			 * Whitespace in Cobol replaceables matches line breaks. Hence, the
			 * replaceable search string has to be enhanced to a regex, which is
			 * returned by this function.
			 */
			private String getRegexFromReplaceable(final String replaceable) {
				final String result;

				if (replaceable == null) {
					result = null;
				} else {
					final String[] parts = StringUtils.split(replaceable);
					final String[] regexParts = new String[parts.length];
					final String regexSeparator = "[\\r\\n\\s]+";

					for (int i = 0; i < parts.length; i++) {
						final String part = parts[i];
						regexParts[i] = Pattern.quote(part);
					}

					result = StringUtils.join(regexParts, regexSeparator);
				}

				return result;
			}

			private String getText(final ReplaceableContext ctx) {
				final String result;

				if (ctx.pseudoText() != null) {
					result = extractPseudoText(ctx.pseudoText());
				} else if (ctx.charDataLine() != null) {
					result = getTextIncludingHiddenTokens(ctx, tokens);
				} else if (ctx.cobolWord() != null) {
					result = ctx.getText();
				} else if (ctx.literal() != null) {
					result = ctx.literal().getText();
				} else {
					result = null;
				}

				return result;
			}

			private String getText(final ReplacementContext ctx) {
				final String result;

				if (ctx.pseudoText() != null) {
					result = extractPseudoText(ctx.pseudoText());
				} else if (ctx.charDataLine() != null) {
					result = getTextIncludingHiddenTokens(ctx, tokens);
				} else if (ctx.cobolWord() != null) {
					result = ctx.getText();
				} else if (ctx.literal() != null) {
					result = ctx.literal().getText();
				} else {
					result = null;
				}

				return result;
			}

			protected String replace(final String string) {
				final String replaceableString = getText(replaceable);
				final String replacementString = getText(replacement);

				final String result;

				if (replaceableString != null && replacementString != null) {
					// regex for the replaceable
					final String replaceableRegex = getRegexFromReplaceable(replaceableString);

					// regex for the replacement
					final String quotedReplacementRegex = Matcher.quoteReplacement(replacementString);

					result = Pattern.compile(replaceableRegex).matcher(string).replaceAll(quotedReplacementRegex);
				} else {
					result = string;
				}

				return result;
			}

			@Override
			public String toString() {
				return replaceable.getText() + " -> " + replacement.getText();
			}
		}

		private ReplacementMapping[] currentReplaceableReplacements;

		private StringBuffer outputBuffer = new StringBuffer();

		public String read() {
			return outputBuffer.toString();
		}

		/**
		 * Replaces replaceables with replacements.
		 */
		public void replace() {
			if (currentReplaceableReplacements != null) {
				Arrays.sort(currentReplaceableReplacements);

				for (final ReplacementMapping replaceableReplacement : currentReplaceableReplacements) {
					final String currentOutput = outputBuffer.toString();
					final String replacedOutput = replaceableReplacement.replace(currentOutput);

					outputBuffer = new StringBuffer();
					outputBuffer.append(replacedOutput);
				}
			}
		}

		private void storeReplaceablesAndReplacements(final List<ReplaceClauseContext> replaceClauses) {
			if (replaceClauses == null) {
				currentReplaceableReplacements = null;
			} else {
				final int length = replaceClauses.size();
				currentReplaceableReplacements = new ReplacementMapping[length];

				int i = 0;

				for (final ReplaceClauseContext replaceClause : replaceClauses) {
					final ReplacementMapping replaceableReplacement = new ReplacementMapping();

					replaceableReplacement.replaceable = replaceClause.replaceable();
					replaceableReplacement.replacement = replaceClause.replacement();

					currentReplaceableReplacements[i] = replaceableReplacement;
					i++;
				}
			}
		}

		private void write(final String text) {
			outputBuffer.append(text);
		}
	}

	private final static Logger LOG = LogManager.getLogger(CobolParserPreprocessorListenerImpl.class);

	private final Stack<PreprocessingContext> contexts = new Stack<PreprocessingContext>();

	protected final String[] copyFileExtensions = new String[] { "", "CPY", "cpy", "COB", "cob", "CBL", "cbl" };

	private final CobolDialect dialect;

	private final CobolSourceFormat formats;

	private final File libDirectory;

	private final BufferedTokenStream tokens;

	public CobolParserPreprocessorListenerImpl(final File libDirectory, final CobolDialect dialect,
			final CobolSourceFormat formats, final BufferedTokenStream tokens) {
		this.libDirectory = libDirectory;
		this.dialect = dialect;
		this.tokens = tokens;
		this.formats = formats;

		contexts.push(new PreprocessingContext());
	}

	public PreprocessingContext context() {
		return contexts.peek();
	}

	@Override
	public void enterControlSpacingStatement(final Cobol85PreprocessorParser.ControlSpacingStatementContext ctx) {
		push();
	}

	@Override
	public void enterCopyStatement(final Cobol85PreprocessorParser.CopyStatementContext ctx) {
		// push a new context for COPY terminals
		push();
	}

	@Override
	public void enterExecCicsStatement(final Cobol85PreprocessorParser.ExecCicsStatementContext ctx) {
		// push a new context for SQL terminals
		push();
	}

	@Override
	public void enterExecSqlStatement(final Cobol85PreprocessorParser.ExecSqlStatementContext ctx) {
		// push a new context for SQL terminals
		push();
	}

	@Override
	public void enterReplaceArea(final Cobol85PreprocessorParser.ReplaceAreaContext ctx) {
		push();
	}

	@Override
	public void enterReplaceByStatement(final Cobol85PreprocessorParser.ReplaceByStatementContext ctx) {
		push();
	}

	@Override
	public void enterReplaceOffStatement(final Cobol85PreprocessorParser.ReplaceOffStatementContext ctx) {
		push();
	}

	@Override
	public void exitControlSpacingStatement(final Cobol85PreprocessorParser.ControlSpacingStatementContext ctx) {
		// throw away control spacing statement
		pop();
	}

	@Override
	public void exitCopyStatement(final Cobol85PreprocessorParser.CopyStatementContext ctx) {
		// throw away COPY terminals
		pop();

		// a new context for the copy file content
		push();

		/*
		 * replacement phrase
		 */
		final ReplacingPhraseContext replacingPhrase = ctx.replacingPhrase();

		if (replacingPhrase != null) {
			context().storeReplaceablesAndReplacements(replacingPhrase.replaceClause());
		}

		/*
		 * copy the copy file
		 */
		final String copyFileIdentifier = ctx.copySource().getText();
		final String fileContent = getCopyFileContent(copyFileIdentifier, libDirectory, dialect, formats);

		if (fileContent != null) {
			context().write(fileContent + CobolPreprocessor.NEWLINE);
			context().replace();
		}

		final String content = context().read();
		pop();

		context().write(content);
	};

	@Override
	public void exitExecCicsStatement(final Cobol85PreprocessorParser.ExecCicsStatementContext ctx) {
		// throw away EXEC CICS terminals -> TODO
		pop();
	}

	@Override
	public void exitExecSqlStatement(final Cobol85PreprocessorParser.ExecSqlStatementContext ctx) {
		// throw away EXEC SQL terminals -> TODO
		pop();
	}

	@Override
	public void exitReplaceArea(final Cobol85PreprocessorParser.ReplaceAreaContext ctx) {
		/*
		 * replacement phrase
		 */
		final List<ReplaceClauseContext> replaceClauses = ctx.replaceByStatement().replaceClause();
		context().storeReplaceablesAndReplacements(replaceClauses);

		context().replace();
		final String content = context().read();

		pop();
		context().write(content);
	}

	@Override
	public void exitReplaceByStatement(final Cobol85PreprocessorParser.ReplaceByStatementContext ctx) {
		// throw away REPLACE BY terminals
		pop();
	};

	@Override
	public void exitReplaceOffStatement(final Cobol85PreprocessorParser.ReplaceOffStatementContext ctx) {
		// throw away REPLACE OFF terminals
		pop();
	}

	protected String getCopyFileContent(final String filename, final File libDirectory, final CobolDialect dialect,
			final CobolSourceFormat format) {
		final File copyFile = identifyCopyFile(filename, libDirectory);
		String result;

		if (copyFile == null) {
			LOG.warn("Copy file {} not found.", filename);

			result = null;
		} else {
			try {
				result = CobolGrammarContext.getInstance().getCobolPreprocessor().process(copyFile, libDirectory,
						format, dialect);
			} catch (final IOException e) {
				result = null;
				LOG.warn(e.getMessage());
			}
		}

		return result;
	}

	protected String getTextIncludingHiddenTokens(final ParseTree ctx, final BufferedTokenStream tokens) {
		final CobolHiddenTokenCollectorListenerImpl listener = new CobolHiddenTokenCollectorListenerImpl(tokens);
		final ParseTreeWalker walker = new ParseTreeWalker();

		walker.walk(listener, ctx);

		return listener.read();
	}

	/**
	 * Identifies a copy file by its name and directory.
	 */
	protected File identifyCopyFile(final String filename, final File libDirectory) {
		File copyFile = null;

		for (final String extension : copyFileExtensions) {
			final String filenameWithExtension;

			if (extension.isEmpty()) {
				filenameWithExtension = filename;
			} else {
				filenameWithExtension = filename + "." + extension;
			}

			final String canonicalPath = libDirectory.getAbsolutePath() + "/" + filenameWithExtension;
			final File copyFileWithExtension = new File(canonicalPath);

			if (copyFileWithExtension.exists()) {
				copyFile = copyFileWithExtension;
				break;
			}
		}

		return copyFile;
	}

	/**
	 * Pops the current preprocessing context from the stack.
	 */
	private PreprocessingContext pop() {
		return contexts.pop();
	}

	/**
	 * Pushes a new preprocessing context onto the stack.
	 */
	private PreprocessingContext push() {
		return contexts.push(new PreprocessingContext());
	}

	@Override
	public void visitTerminal(final TerminalNode node) {
		final int tokPos = node.getSourceInterval().a;
		context().write(TokenUtils.getHiddenTokensToLeft(tokens, tokPos));

		if (!TokenUtils.isEOF(node)) {
			final String text = node.getText();
			context().write(text);
		}
	}
}

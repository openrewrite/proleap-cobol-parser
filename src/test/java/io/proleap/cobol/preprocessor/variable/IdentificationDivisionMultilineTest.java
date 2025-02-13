package io.proleap.cobol.preprocessor.variable;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

import io.proleap.cobol.preprocessor.sub.CobolLine;
import org.junit.Test;

import io.proleap.cobol.asg.params.CobolParserParams;
import io.proleap.cobol.asg.params.impl.CobolParserParamsImpl;
import io.proleap.cobol.preprocessor.CobolPreprocessor.CobolSourceFormatEnum;
import io.proleap.cobol.preprocessor.impl.CobolPreprocessorImpl;

public class IdentificationDivisionMultilineTest {

	@Test
	public void test() throws Exception {
		final CobolParserParams params = new CobolParserParamsImpl();
		params.setFormat(CobolSourceFormatEnum.VARIABLE);

		final File inputFile = new File(
				"src/test/resources/io/proleap/cobol/preprocessor/variable/IdentificationDivisionMultiline.cbl");
		final String preProcessedInput = new CobolPreprocessorImpl().process(inputFile, params);

		final File expectedFile = new File(
				"src/test/resources/io/proleap/cobol/preprocessor/variable/IdentificationDivisionMultiline.cbl.preprocessed");
		final String expected = Files.readString(expectedFile.toPath(), StandardCharsets.UTF_8);
		assertEquals(expected, preProcessedInput);
	}

	@Test
	public void testCopyBookDirectoriesWithRewriteLines() throws Exception {
		final CobolParserParams params = new CobolParserParamsImpl();
		params.setFormat(CobolSourceFormatEnum.VARIABLE);


		final File inputFile = new File(
				"src/test/resources/io/proleap/cobol/preprocessor/variable/LineContinuationWhitespace.cbl");
		final CobolPreprocessorImpl preprocessor = new CobolPreprocessorImpl();

		final File expectedFile = new File(
				"src/test/resources/io/proleap/cobol/preprocessor/variable/LineContinuationWhitespaceWithOriginalArea.cbl.preprocessed");
		final String expected = Files.readString(expectedFile.toPath(), StandardCharsets.UTF_8);

		final List<CobolLine> cobolLines = preprocessor.getRewrittenLines(inputFile, params);
		final String processRewriteLines = preprocessor.process(cobolLines, params);

		assertEquals(expected, processRewriteLines);
	}
}
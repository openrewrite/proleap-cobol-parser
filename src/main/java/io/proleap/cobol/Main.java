package io.proleap.cobol;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.RuleNode;

import io.proleap.cobol.asg.metamodel.*;
import io.proleap.cobol.asg.metamodel.data.DataDivision;
import io.proleap.cobol.asg.metamodel.data.datadescription.DataDescriptionEntry;
import io.proleap.cobol.asg.params.CobolParserParams;
import io.proleap.cobol.asg.params.impl.CobolParserParamsImpl;
import io.proleap.cobol.asg.runner.impl.CobolParserRunnerImpl;
import io.proleap.cobol.preprocessor.CobolPreprocessor;

public class Main {

	private static final String DIR = "src/test/resources/io/proleap/cobol/preprocessor/copy/cobolword/variable";

	public static void main(String[] args) throws IOException {
		// generate ASG from plain COBOL code
		File inputFile = 
			//new java.io.File("src/test/resources/io/proleap/cobol/asg/HelloWorld.cbl");
			new File(DIR + "/CopyCblWord.cbl");
			//new File("src/test/resources/io/proleap/cobol/preprocessor/fixed/LineContinuation.cbl");
		CobolPreprocessor.CobolSourceFormatEnum format = CobolPreprocessor.CobolSourceFormatEnum.FIXED;

		CobolParserRunnerImpl parser = new CobolParserRunnerImpl();
		CobolParserParams params = new CobolParserParamsImpl();
		
		params.setFormat(format);
		final File copyBooksDirectory = new File(DIR + "/copybooks");
		params.setCopyBookDirectories(Arrays.asList(copyBooksDirectory));
		Program program = parser.analyzeFile(inputFile, params);
		
		// navigate on ASG
		CompilationUnit compilationUnit = program.getCompilationUnit("copycblword");
		ASGPrinter printer = new ASGPrinter(System.out);
		printer.print(compilationUnit.getCtx());

		ProgramUnit programUnit = compilationUnit.getProgramUnit();
		DataDivision dataDivision = programUnit.getDataDivision();
		DataDescriptionEntry dataDescriptionEntry = dataDivision.getWorkingStorageSection().getDataDescriptionEntry("ITEMS");
		Integer levelNumber = dataDescriptionEntry.getLevelNumber();
	}
}

class ASGPrinter extends CobolBaseVisitor<Void> {
	
	final int delta = 4;
	
	PrintStream out;
	int indent;
	
	public ASGPrinter(PrintStream out) {
		this.out = out;
		this.indent = 0;
	}
	
	@Override
	public Void visitChildren(RuleNode node) {
		for(int i=0; i<indent; i++) out.print(' ');
		out.println(node.getClass().getSimpleName().replace("Context", ""));
		indent += delta;
		super.visitChildren(node);
		indent -= delta;	
		return null;
	}

	public void print(RuleNode node) {
		visit(node);
	}
	
}
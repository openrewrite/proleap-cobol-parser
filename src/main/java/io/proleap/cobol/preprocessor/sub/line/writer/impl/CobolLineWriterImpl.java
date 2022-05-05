/*
 * Copyright (C) 2017, Ulrich Wolffgang <ulrich.wolffgang@proleap.io>
 * All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license. See the LICENSE file for details.
 */

package io.proleap.cobol.preprocessor.sub.line.writer.impl;

import java.util.List;

import io.proleap.cobol.StringBufferWithOriginalPositions;
import io.proleap.cobol.StringWithOriginalPositions;
import io.proleap.cobol.asg.params.CobolDialect;
import io.proleap.cobol.preprocessor.CobolPreprocessor;
import io.proleap.cobol.preprocessor.CobolPreprocessor.CobolSourceFormatEnum;
import io.proleap.cobol.preprocessor.sub.CobolLine;
import io.proleap.cobol.preprocessor.sub.CobolLineTypeEnum;
import io.proleap.cobol.preprocessor.sub.line.writer.CobolLineWriter;

public class CobolLineWriterImpl implements CobolLineWriter {

	@Override
	public StringWithOriginalPositions serialize(final String originalCode, final List<CobolLine> lines) {
		final StringBufferWithOriginalPositions sb = new StringBufferWithOriginalPositions(originalCode);

		for (final CobolLine line : lines) {
			final boolean notContinuationLine = !CobolLineTypeEnum.CONTINUATION.equals(line.getType());

			/*
			 Order of line elements :
			 sequenceArea
			 indicatorArea
			 contentAreaA
			 contentAreaB
			 commentArea
			 */
			
			if (notContinuationLine) {
				if (line.getNumber() > 0) {
					sb.append(CobolPreprocessor.NEWLINE);
					sb.skip(1); // because we skip '\n\r' and add '\n'
				}

				sb.append(line.getBlankSequenceArea());
				sb.skip(line.getSequenceAreaOriginal().length() - line.getBlankSequenceArea().length());
				sb.append(line.getIndicatorArea());
			} else {
				sb.skip(line.getSequenceArea());
				sb.skip(line.getSequenceAreaOriginal().length() - line.getSequenceArea().length());
				sb.skip(line.getIndicatorArea());
			}

			sb.append(line.getContentArea());
			sb.skip(line.getContentAreaOriginal().length() - line.getContentArea().length());

			sb.skip(line.getCommentArea());
			sb.skip(line.getCommentAreaOriginal().length() - line.getCommentArea().length());
		}

		return sb.toStringWithMarkers();
	}
}

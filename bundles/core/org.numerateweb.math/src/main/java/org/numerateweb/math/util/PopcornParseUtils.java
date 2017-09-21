package org.numerateweb.math.util;

import org.numerateweb.math.model.Builder;
import org.numerateweb.math.ns.INamespaces;
import org.numerateweb.math.popcorn.PopcornParser;
import org.parboiled.Parboiled;
import org.parboiled.parserunners.ReportingParseRunner;
import org.parboiled.support.ParsingResult;

public class PopcornParseUtils {
	public static <T> ParseResult<T> parse(String input, INamespaces ns,
			Builder<T> builder) {
		PopcornParser parser = Parboiled.createParser(PopcornParser.class, ns);
		ParsingResult<T> result = new ReportingParseRunner<T>(
				parser.Start(builder)).run(input);
		if (result.matched) {
			return new ParseResult<T>(result.resultValue);
		} else {
			return new ParseResult<T>(result.parseErrors);
		}
	}
}

package org.numerateweb.math.popcorn.rules.test;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.junit.Assert;
import org.junit.Test;
import org.numerateweb.math.popcorn.rules.MathRulesParser;
import org.parboiled.Parboiled;
import org.parboiled.Rule;
import org.parboiled.errors.ErrorUtils;
import org.parboiled.parserunners.ReportingParseRunner;
import org.parboiled.support.ParsingResult;

import com.github.parboiled1.grappa.stack.DefaultValueStack;

/**
 * Simple JUnit Test for the POPCORN rules parser
 */
public class RulesParserTest extends GUnitBaseTestCase {
	final MathRulesParser parser = Parboiled.createParser(MathRulesParser.class);

	@Test
	public void test() throws Exception {
		int failures = 0;
		BufferedReader in = new BufferedReader(
				new InputStreamReader(getClass().getResourceAsStream("/gunit/Rules.gunit")));

		for (TextInfo textInfo : getTextInfos(in)) {
			if (textInfo.rule == null) {
				// unknown rule
				continue;
			}
			Rule rule = (Rule) MathRulesParser.class.getMethod(textInfo.rule).invoke(parser);
			// ensure that full text is matched until EOF
			rule = parser.sequence(rule, MathRulesParser.EOI);

			ParsingResult<Object> result = new ReportingParseRunner<Object>(rule)
					.withValueStack(new DefaultValueStack<Object>()).run((CharSequence) textInfo.text);

			boolean passed = !result.isSuccess() && textInfo.result == Result.FAIL
					|| result.isSuccess() && textInfo.result == Result.OK;

			if (result.isSuccess() && !result.getValueStack().isEmpty()) {
				System.out.println(result.getTopStackValue());
			}

			if (!result.isSuccess() && textInfo.result == Result.OK) {
				System.err.println(ErrorUtils.printParseErrors(result));
			}

			if (!passed) {
				failures++;
				System.err.println("<<\n" + textInfo.text + "\n>> " + textInfo.result);
			}
		}
		Assert.assertEquals(0, failures);
	}

}

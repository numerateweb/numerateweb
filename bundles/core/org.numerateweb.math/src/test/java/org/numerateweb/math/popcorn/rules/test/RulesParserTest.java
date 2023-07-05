/*
 * Copyright (c) 2023 Numerate Web contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
			rule = parser.Sequence(rule, MathRulesParser.EOI);

			ParsingResult<Object> result = new ReportingParseRunner<>(rule).run(textInfo.text);

			boolean passed = !result.matched && textInfo.result == Result.FAIL
					|| result.matched && textInfo.result == Result.OK;

			if (result.matched && result.resultValue != null) {
				System.out.println(result.resultValue);
			}

			if (!result.matched && textInfo.result == Result.OK) {
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

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

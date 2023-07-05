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
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GUnitBaseTestCase {
	protected enum Result {
		OK, FAIL
	};

	protected class TextInfo {
		public final String rule;
		public final Result result;
		public final String text;

		public TextInfo(String rule, Result result, String text) {
			this.rule = rule;
			this.result = result;
			this.text = text;
		}
	}

	private TextInfo parseText(BufferedReader in, String rule) throws Exception {
		Pattern unicodes = Pattern.compile("\\\\u([0-9a-fA-F]{4})");

		StringBuffer text = new StringBuffer();
		while (in.ready()) {
			String line = in.readLine();
			if (line.startsWith(">>")) {
				// remove last new-line character
				String textStr = text.substring(0, text.length() - 1);
				TextInfo textInfo = new TextInfo(rule, line.toLowerCase().contains("ok") ? Result.OK : Result.FAIL,
						textStr);
				return textInfo;
			} else {
				Matcher matcher = unicodes.matcher(line);
				while (matcher.find()) {
					matcher.appendReplacement(text, Character.toString((char) Integer.parseInt(matcher.group(1), 16)));
				}
				matcher.appendTail(text);
				text.append('\n');
			}
		}
		throw new NoSuchElementException("Expected \">>\"");
	}

	public List<TextInfo> getTextInfos(BufferedReader in) throws Exception {
		String currentRule = null;
		List<TextInfo> textInfos = new ArrayList<TextInfo>();

		while (in.ready()) {
			in.mark(1);
			if (in.read() == '<' && in.ready() && in.read() == '<') {
				in.readLine();

				textInfos.add(parseText(in, currentRule));
			} else {
				in.reset();
				String line = in.readLine();
				if (line.matches("^[a-zA-Z0-9]+:\\s*$")) {
					currentRule = line.trim().replaceAll(":", "");
				}
			}
		}
		return textInfos;
	}
}
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
package org.numerateweb.math.popcorn;

import java.util.HashMap;
import java.util.Map;

import org.numerateweb.math.ns.INamespaces;
import org.numerateweb.math.search.PATTERNS;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import net.enilink.komma.core.URI;
import net.enilink.komma.core.URIs;

public class PopcornSymbols {
	public static final String CDBASE = "http://www.openmath.org/cd/";

	static Map<URI, Integer> infix = new HashMap<>();

	// see also http://en.wikipedia.org/wiki/Order_of_operations
	static {
		infix.put(symbol("prog1", "block"), 150);
		infix.put(symbol("prog1", "assignment"), 140);
		infix.put(symbol("logic1", "implies"), 130);
		infix.put(symbol("logic1", "equivalent"), 130);
		infix.put(symbol("logic1", "or"), 120);
		infix.put(symbol("logic1", "and"), 110);
		infix.put(symbol("relation1", "eq"), 70);
		infix.put(symbol("relation1", "neq"), 70);
		infix.put(symbol("relation1", "lt"), 60);
		infix.put(symbol("relation1", "leq"), 60);
		infix.put(symbol("relation1", "gt"), 60);
		infix.put(symbol("relation1", "geq"), 60);
		infix.put(symbol("interval1", "interval"), 50);
		infix.put(symbol("arith1", "plus"), 40);
		infix.put(symbol("arith1", "minus"), 40);
		infix.put(symbol("arith1", "times"), 30);
		infix.put(symbol("arith1", "divide"), 30);
		infix.put(symbol("arith1", "power"), 20);
		infix.put(symbol("complex1", "complex_cartesian"), 10);
		infix.put(symbol("nums1", "rational"), 10);
	}
	static BiMap<String, URI> symbolMap = HashBiMap.create();
	static {
		// operators
		symbolMap.put(";", symbol("prog1", "block"));
		symbolMap.put(":=", symbol("prog1", "assignment"));
		symbolMap.put("==>", symbol("logic1", "implies"));
		symbolMap.put("<=>", symbol("logic1", "equivalent"));
		symbolMap.put("or", symbol("logic1", "or"));
		symbolMap.put("and", symbol("logic1", "and"));
		symbolMap.put("<", symbol("relation1", "lt"));
		symbolMap.put("<=", symbol("relation1", "leq"));
		symbolMap.put(">", symbol("relation1", "gt"));
		symbolMap.put("=", symbol("relation1", "eq"));
		symbolMap.put(">=", symbol("relation1", "geq"));
		//symbolMap.put("<>", symbol("relation1", "neq"));
		symbolMap.put("!=", symbol("relation1", "neq"));
		symbolMap.put("..", symbol("interval1", "interval"));
		symbolMap.put("until", symbol("interval1", "interval_oo"));
		symbolMap.put("+", symbol("arith1", "plus"));
		symbolMap.put("-", symbol("arith1", "minus"));
		symbolMap.put("*", symbol("arith1", "times"));
		symbolMap.put("/", symbol("arith1", "divide"));
		symbolMap.put("^", symbol("arith1", "power"));
		symbolMap.put("|", symbol("complex1", "complex_cartesian"));
		symbolMap.put("//", symbol("nums1", "rational"));

		// shorthand symbols
		symbolMap.put("arccos", symbol("transc1", "arccos"));
		symbolMap.put("arcsin", symbol("transc1", "arcsin"));
		symbolMap.put("arctan", symbol("transc1", "arctan"));
		symbolMap.put("cos", symbol("transc1", "cos"));
		symbolMap.put("cosh", symbol("transc1", "cosh"));
		symbolMap.put("cot", symbol("transc1", "cot"));
		symbolMap.put("coth", symbol("transc1", "coth"));
		symbolMap.put("csc", symbol("transc1", "csc"));
		symbolMap.put("csch", symbol("transc1", "csch"));
		symbolMap.put("exp", symbol("transc1", "exp"));
		symbolMap.put("sec", symbol("transc1", "sec"));
		symbolMap.put("sech", symbol("transc1", "sech"));
		symbolMap.put("sin", symbol("transc1", "sin"));
		symbolMap.put("sinh", symbol("transc1", "sinh"));
		symbolMap.put("tan", symbol("transc1", "tan"));
		symbolMap.put("tanh", symbol("transc1", "tanh"));
		symbolMap.put("abs", symbol("arith1", "abs"));
		symbolMap.put("root", symbol("arith1", "root"));
		symbolMap.put("sum", symbol("arith1", "sum"));
		symbolMap.put("product", symbol("arith1", "product"));
		symbolMap.put("diff", symbol("calculus1", "diff"));
		symbolMap.put("int", symbol("calculus1", "int"));
		symbolMap.put("defint", symbol("calculus1", "defint"));
		symbolMap.put("pi", symbol("nums1", "pi"));
		symbolMap.put("e", symbol("nums1", "e"));
		symbolMap.put("i", symbol("nums1", "i"));
		symbolMap.put("infinity", symbol("nums1", "infinity"));
		symbolMap.put("min", symbol("minmax1", "min"));
		symbolMap.put("max", symbol("minmax1", "max"));
		symbolMap.put("lambda", symbol("fns1", "lambda"));
		symbolMap.put("true", symbol("logic1", "true"));
		symbolMap.put("false", symbol("logic1", "false"));
		symbolMap.put("binomial", symbol("combinat1", "binomial"));
		symbolMap.put("factorial", symbol("integer1", "factorial"));

		symbolMap.put("map", symbol("list1", "map"));
		symbolMap.put("list_selector", symbol("list2", "list_selector"));

		// rounding functions
		symbolMap.put("ceiling", symbol("rounding1", "ceiling"));
		symbolMap.put("floor", symbol("rounding1", "floor"));
		symbolMap.put("round", symbol("rounding1", "round"));
		symbolMap.put("trunc", symbol("rounding1", "trunc"));

		// RDF extensions
		symbolMap.put("prefixes", symbol("rdf", "prefixes"));
		symbolMap.put("prefix", symbol("rdf", "prefix"));

		// Support for patterns
		symbolMap.put(".!", PATTERNS.NONE_OF);
		symbolMap.put(".|", PATTERNS.ANY_OF);
		symbolMap.put(".&", PATTERNS.ALL_OF);
		symbolMap.put(".^", PATTERNS.ROOT);
		symbolMap.put(".,", PATTERNS.ARGUMENT);
		symbolMap.put("...", PATTERNS.SELF_OR_DESCENDANT);
		symbolMap.put("..+", PATTERNS.DESCENDANT);
		symbolMap.put("?", PATTERNS.ANY);

		// null symbol
		symbolMap.put("null", symbol("sys", "null"));
	}

	static Map<URI, String> prefixShorthands = new HashMap<>();
	static {
		prefixShorthands.put(symbol("arith1", "unary_minus"), "-");
		prefixShorthands.put(symbol("logic1", "not"), "not");
	}

	public static URI symbol(String cd, String name) {
		return URIs.createURI(PopcornSymbols.CDBASE + cd + "#" + name);
	}

	public static PopcornExpr toPopcorn(INamespaces ns, URI symbol) {
		String symbolStr = symbol.toString();
		String text = symbolStr;
		String shorthand = prefixShorthands.get(symbol);
		if (shorthand != null) {
			text = shorthand;
		} else {
			Object key = symbolMap.inverse().get(symbol);
			if (key != null) {
				text = key.toString();
			} else {
				try {
					String prefix = ns != null ? ns.getPrefix(symbol
							.namespace()) : null;
					if (prefix != null) {
						text = prefix.length() == 0 ? symbol.localPart()
								: prefix + ":" + symbol.localPart();
					} else if (symbolStr.startsWith(CDBASE)) {
						text = symbol.lastSegment() + ":" + symbol.fragment();
					} else {
						text = "<" + symbolStr + ">";
					}
				} catch (Exception e) {
					// ignore
				}
			}
		}
		Integer p = infix.get(symbol);
		return new PopcornExpr(symbol, text, p != null ? p : 0, p != null);
	}

	public static URI toOpenMath(String operatorOrShortHand) {
		return (URI) symbolMap.get(operatorOrShortHand);
	}
}

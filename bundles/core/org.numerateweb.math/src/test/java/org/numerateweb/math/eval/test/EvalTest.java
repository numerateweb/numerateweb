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
package org.numerateweb.math.eval.test;

import static org.junit.Assert.*;
import org.junit.Test;
import org.numerateweb.math.eval.ExprBuilder;
import org.numerateweb.math.eval.expr.Expr;
import org.numerateweb.math.ns.INamespaces;
import org.numerateweb.math.util.ParseResult;
import org.numerateweb.math.util.PopcornParseUtils;

/**
 * Basic tests for numerical evaluation of OM objects.
 */
public class EvalTest {
	Expr parse(String expr) {
		ParseResult<Expr> result = PopcornParseUtils.parse(expr, INamespaces.empty(), new ExprBuilder());
		if (!result.matched()) {
			throw new IllegalArgumentException(result.errorMessage());
		}
		return result.value;
	}

	Object eval(String expr) {
		return parse(expr).eval();
	}

	Number evalNumber(String expr) {
		return (Number) parse(expr).eval();
	}

	@Test
	public void testBasicOperators() {
		assertEquals(8, evalNumber("2^3").intValue());
		assertEquals(64, evalNumber("(2^3)^2").intValue());
		assertEquals(64, evalNumber("(2^3)^(1 + 0 + 1)").intValue());
		assertEquals(32, evalNumber("(2^3)^(1 + 0 + 1)/2").intValue());
	}

	@Test
	public void testSetOperators() {
		assertEquals(12, evalNumber("sum(1..3, $x -> $x * 2)").intValue());
		assertEquals(12, evalNumber("$y := 2; sum(1..3, $x -> $x * $y)").intValue());
		assertEquals(12, evalNumber("$y := 3; $y := 2; $x := 5; sum(1..3, $x -> $x * $y)").intValue());
		assertEquals(12, evalNumber("sum({1,2,3}, $x -> $x * 2)").intValue());
		assertEquals(12, evalNumber("sum([1,2,3], $x -> $x * 2)").intValue());
		assertEquals(8, evalNumber("product({1,2}, $x -> $x * 2)").intValue());
	}

	@Test
	public void testSets() {
		assertEquals(eval("{3,2,1}"), eval("{1,2,3}"));
		assertNotSame(eval("{1,2}"), eval("{3,2,1}"));
		assertEquals(eval("{2}"), eval("set1:intersect({1,2},{2,3,4})"));
		assertEquals(eval("{2,3}"), eval("set1:intersect({1,2,3},{2,3,4},{7,2,6,3})"));
		assertEquals(eval("{}"), eval("set1:intersect({1,2,3},{},{7,2,6,3})"));
		assertEquals(eval("{{2,{},10},3}"), eval("set1:intersect({1,{2,{},10},3},{{2,{},10},3,4})"));
	}

	@Test
	public void testLambdas() {
		assertEquals(10, evalNumber("$lambda := $x -> $x * 2; $lambda(5)").intValue());
		assertEquals(12, evalNumber("($x -> $x * 2)(6)").intValue());
	}

	@Test
	public void testWhile() {
		assertEquals(1, evalNumber("$i := 5; while $i > 1 do $i := $i - 1 end; $i").intValue());
		// sum of first n integers
		assertEquals(5 * 6 / 2,
				evalNumber("$i := 5; $sum := 0; while $i > 0 do $sum := $sum + $i; $i := $i - 1 end; $sum")
						.intValue());
	}

	@Test
	public void testComplexExpression() {
		String expression = String.join(";\n", //
				"$degrees := $r -> $r * 180 / pi", //
				"$radians := $d -> $d * pi / 180", //
				"$sigma := -20", //
				"$r_2 := 80; $r_0 := 50; $r_2 := 85", //
				"$kappa_2 := 30", "$kappa_0 := $degrees(arctan(tan($radians($kappa_2))) * cos($radians($sigma)))", //
				"$xb := $r_2 * cos($radians($kappa_2))", //
				"$yb := $r_2 * sin($radians($kappa_2))", //
				"$zb := 0", //
				"$xm := $xb + $r_0 * cos($radians($kappa_0))", //
				"$ym := $yb + $r_0 * sin($radians($kappa_0))*cos($radians($sigma))", //
				"$zm := $yb + $r_0 * sin($radians($kappa_0))*sin($radians($sigma))", //
				"$alpha0 := if $sigma <0 then 90 else -90 end", //

				"$z0_prev := null", //
				"$diff_z0 := 1", //
				"while $diff_z0 > 0.001 do\n" + //
						String.join(";\n", //
								"$x0 := $xm - $r_0 * cos($radians($alpha0))", //
								"$y0 := $ym + $r_0 * sin($radians($alpha0))*cos($radians($sigma))", //
								"$z0 := $r_0 * sin($radians($alpha0))*sin($radians($sigma))", //
								// "out:println($diff_z0 + \" - \" + $z0_prev)", //
								"if $z0_prev != null then $diff_z0 := abs($z0 - $z0_prev) end", //
								"$z0_prev := $z0", //

								"$alpha2 := $degrees(arctan($y0/$x0))", //
								"$x2 := cos($radians($alpha2)) * $r_2", //
								"$y2 := sin($radians($alpha2)) * $r_2", //

								"$alpha0 := $degrees(arctan(($y2 - $ym) / cos($radians($sigma)) / ($xm - $x2)))" //
						) + //
						"end\n");
		eval(expression);
	}
}

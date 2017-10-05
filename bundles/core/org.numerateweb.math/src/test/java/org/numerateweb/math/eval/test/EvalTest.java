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
}

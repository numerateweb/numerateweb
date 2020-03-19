package org.numerateweb.math.popcorn.rules.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.Test;
import org.numerateweb.math.eval.PojoEvaluator;
import org.numerateweb.math.model.OMObject;
import org.numerateweb.math.popcorn.rules.MathRulesParser;
import org.numerateweb.math.reasoner.CacheManager;
import org.numerateweb.math.reasoner.GuavaCacheFactory;
import org.numerateweb.math.reasoner.PojoModelAccess;
import org.parboiled.Parboiled;
import org.parboiled.errors.ErrorUtils;
import org.parboiled.parserunners.ReportingParseRunner;
import org.parboiled.support.ParsingResult;

import net.enilink.komma.core.URI;
import net.enilink.komma.core.URIs;

/**
 * Simple JUnit Test for execution of rules over POJO models.
 */
public class SimpleRulesTest {
	final MathRulesParser parser = Parboiled.createParser(MathRulesParser.class);
	final URI prefix = URIs.createURI("http://example.org/");

	static class Rectangle {
		double a, b;
		double area;

		public Rectangle(double a, double b) {
			this.a = a;
			this.b = b;
		}

		public void setArea(double area) {
			this.area = area;
		}

		public double getArea() {
			return area;
		}
	}
	
	static class Rectangles {
		List<Rectangle> rectangles = new ArrayList<>();
	}

	static class NAry {
		double a, b, c;
		double add, multiply, subtract, divide, power;
		public NAry(double a, double b, double c) {
			this.a = a;
			this.b = b;
			this.c = c;
		}
	}

	@Test
	public void test() throws Exception {
		final String rules;
		try (BufferedReader br = new BufferedReader(
				new InputStreamReader(getClass().getResourceAsStream("/rules/simple.nwrules")))) {
			rules = br.lines().collect(Collectors.joining("\n"));
		}

		ParsingResult<Object> result = new ReportingParseRunner<Object>(parser.Document()).run(rules.toCharArray());

		if (result.matched && result.resultValue != null) {
			OMObject constraintSet = (OMObject) result.resultValue;
			List<OMObject> constraints = Arrays.stream(constraintSet.getArgs(), 1, constraintSet.getArgs().length)
					.map(r -> (OMObject) r).collect(Collectors.toList());

			PojoModelAccess modelAccess = new PojoModelAccess(constraints);
			CacheManager cacheManager = new CacheManager(new GuavaCacheFactory());
			PojoEvaluator evaluator = new PojoEvaluator(modelAccess, cacheManager);

			Rectangle rect = new Rectangle(2, 4);

			System.out.println(evaluator.evaluate(rect, prefix.appendLocalPart("area"), Optional.empty()).asOpenMath());
			
			Rectangles rectangles = new Rectangles();
			rectangles.rectangles.add(new Rectangle(1, 2));
			rectangles.rectangles.add(new Rectangle(1, 3));
			rectangles.rectangles.add(new Rectangle(1, 4));
			
			System.out.println("sum = " + evaluator.evaluate(rectangles, prefix.appendLocalPart("areaSum"), Optional.empty()).asOpenMath());
			System.out.println("sum == 9 ? " + evaluator.evaluate(rectangles, prefix.appendLocalPart("sumIs9"), Optional.empty()).asOpenMath());

			Rectangle r0 = rectangles.rectangles.get(0);
			System.out.println("r[0].b = " + modelAccess.getPropertyValues(r0, prefix.appendLocalPart("b"), null).toList());
			r0.b = 3;
			evaluator.invalidate(r0, prefix.appendLocalPart("b"), true);
			System.out.println("r[0].b = " + modelAccess.getPropertyValues(r0, prefix.appendLocalPart("b"), null).toList());
			System.out.println("sum = " + evaluator.evaluate(rectangles, prefix.appendLocalPart("areaSum"), Optional.empty()).asOpenMath());
			System.out.println("sum == 9 ? " + evaluator.evaluate(rectangles, prefix.appendLocalPart("sumIs9"), Optional.empty()).asOpenMath());

			NAry nary = new NAry(16, 4, 2);
			System.out.println("n-add = " + evaluator.evaluate(nary, prefix.appendLocalPart("add"), Optional.empty()).asOpenMath());
			System.out.println("n-multiply = " + evaluator.evaluate(nary, prefix.appendLocalPart("multiply"), Optional.empty()).asOpenMath());
			System.out.println("n-subtract = " + evaluator.evaluate(nary, prefix.appendLocalPart("subtract"), Optional.empty()).asOpenMath());
			System.out.println("n-divide = " + evaluator.evaluate(nary, prefix.appendLocalPart("divide"), Optional.empty()).asOpenMath());
			System.out.println("n-power = " + evaluator.evaluate(nary, prefix.appendLocalPart("power"), Optional.empty()).asOpenMath());

			assertEquals(nary.add, nary.a + nary.b + nary.c, 1e-5);
			assertEquals(nary.multiply, nary.a * nary.b * nary.c, 1e-5);
			assertEquals(nary.subtract, nary.a - nary.b - nary.c, 1e-5);
			assertEquals(nary.divide, nary.a / nary.b / nary.c, 1e-5);
			assertEquals(nary.power, Math.pow(Math.pow(nary.a, nary.b), nary.c), 1e-5);
		} else {
			System.err.println(ErrorUtils.printParseErrors(result));
			fail("Invalid rules format.");
		}
	}
}

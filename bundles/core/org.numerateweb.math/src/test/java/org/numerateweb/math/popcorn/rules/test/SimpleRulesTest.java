package org.numerateweb.math.popcorn.rules.test;

import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.Test;
import org.numerateweb.math.eval.SimpleEvaluator;
import org.numerateweb.math.model.OMObject;
import org.numerateweb.math.popcorn.rules.MathRulesParser;
import org.numerateweb.math.reasoner.CacheManager;
import org.numerateweb.math.reasoner.GuavaCacheFactory;
import org.numerateweb.math.reasoner.IModelAccess;
import org.numerateweb.math.reasoner.PojoModelAccess;
import org.numerateweb.math.reasoner.RdfModelAccess;
import org.numerateweb.math.reasoner.Reasoner;
import org.parboiled.Parboiled;
import org.parboiled.errors.ErrorUtils;
import org.parboiled.parserunners.ReportingParseRunner;
import org.parboiled.support.ParsingResult;

import com.github.parboiled1.grappa.stack.DefaultValueStack;

import net.enilink.komma.core.URIs;

/**
 * Simple JUnit Test for execution of rules over POJO models.
 */
public class SimpleRulesTest {
	final MathRulesParser parser = Parboiled.createParser(MathRulesParser.class);

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

	@Test
	public void test() throws Exception {
		final String rules;
		try (BufferedReader br = new BufferedReader(
				new InputStreamReader(getClass().getResourceAsStream("/rules/simple.nwrules")))) {
			rules = br.lines().collect(Collectors.joining("\n"));
		}

		ParsingResult<Object> result = new ReportingParseRunner<Object>(parser.Document())
				.withValueStack(new DefaultValueStack<Object>()).run(rules.toCharArray());

		if (result.isSuccess() && !result.getValueStack().isEmpty()) {
			OMObject constraintSet = (OMObject) result.getTopStackValue();
			List<OMObject> constraints = Arrays.stream(constraintSet.getArgs(), 1, constraintSet.getArgs().length)
					.map(r -> (OMObject) r).collect(Collectors.toList());

			IModelAccess modelAccess = new PojoModelAccess(constraints);
			CacheManager cacheManager = new CacheManager(new GuavaCacheFactory());
			SimpleEvaluator evaluator = new SimpleEvaluator(modelAccess, cacheManager);

			Rectangle rect = new Rectangle(2, 4);

			System.out.println(evaluator.evaluate(rect, URIs.createURI("java:area"), Optional.empty()).asOpenMath());
			
			Rectangles rectangles = new Rectangles();
			rectangles.rectangles.add(new Rectangle(1, 2));
			rectangles.rectangles.add(new Rectangle(1, 3));
			rectangles.rectangles.add(new Rectangle(1, 4));
			
			System.out.println(evaluator.evaluate(rectangles, URIs.createURI("java:areaSum"), Optional.empty()).asOpenMath());
			System.out.println(evaluator.evaluate(rectangles, URIs.createURI("java:sumOk"), Optional.empty()).asOpenMath());
		} else {
			System.err.println(ErrorUtils.printParseErrors(result));
			fail("Invalid rules format.");
		}
	}
}

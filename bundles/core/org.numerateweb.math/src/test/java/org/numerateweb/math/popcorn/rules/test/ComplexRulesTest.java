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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.Test;
import org.numerateweb.math.eval.IEvaluator;
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
public class ComplexRulesTest {
	final MathRulesParser parser = Parboiled.createParser(MathRulesParser.class);
	final URI prefix = URIs.createURI("http://example.org/");

	static abstract class Shape2D {
		double area, perimeter;

		public double getArea() {
			return area;
		}

		public double getPerimeter() {
			return perimeter;
		}
	}

	static class Rectangle extends Shape2D {
		double a, b;

		public Rectangle(double a, double b) {
			this.a = a;
			this.b = b;
		}
	}

	static class Circle extends Shape2D {
		double r;

		public Circle(double r) {
			this.r = r;
		}
	}

	static abstract class Shape3D {
		double volume, surface;

		public double getVolume() {
			return volume;
		}

		public double getSurface() {
			return surface;
		}
	}

	static class Extrusion extends Shape3D {
		Shape2D base;
		double h;

		public Extrusion(Shape2D base, double h) {
			this.base = base;
			this.h = h;
		}
	}

	protected OMObject evaluate(IEvaluator evaluator, Object obj, String property) {
		return evaluator.evaluate(obj, prefix.appendLocalPart(property), Optional.empty()).asOpenMath();
	}

	@Test
	public void test() throws Exception {
		final String rules;
		try (BufferedReader br = new BufferedReader(
				new InputStreamReader(getClass().getResourceAsStream("/rules/complex.nwrules")))) {
			rules = br.lines().collect(Collectors.joining("\n"));
		}

		ParsingResult<Object> result = new ReportingParseRunner<>(parser.Document()).run(rules.toCharArray());
		if (result.matched && result.resultValue != null) {
			OMObject constraintSet = (OMObject) result.resultValue;
			List<OMObject> constraints = Arrays.stream(constraintSet.getArgs(), 1, constraintSet.getArgs().length)
					.map(r -> (OMObject) r).collect(Collectors.toList());

			PojoModelAccess modelAccess = new PojoModelAccess(constraints);
			CacheManager cacheManager = new CacheManager(new GuavaCacheFactory());
			PojoEvaluator evaluator = new PojoEvaluator(modelAccess, cacheManager);

			// create prism and cylinder
			Rectangle rect = new Rectangle(2.0, 4.0);
			Extrusion prism = new Extrusion(rect, 5.0);
			Circle circ = new Circle(3.0);
			Extrusion cyl = new Extrusion(circ, 1.0);

			// trigger rule evaluation for shape 2d/3d properties
			evaluate(evaluator, rect, "area");
			evaluate(evaluator, rect, "perimeter");
			evaluate(evaluator, circ, "area");
			evaluate(evaluator, circ, "perimeter");

			evaluate(evaluator, prism, "volume");
			evaluate(evaluator, prism, "surface");
			evaluate(evaluator, cyl, "volume");
			evaluate(evaluator, cyl, "surface");

			assertEquals("A(rect)", rect.a * rect.b, rect.area, 10e-5);
			assertEquals("P(rect)", 2 * rect.a + 2 * rect.b, rect.perimeter, 10e-5);
			assertEquals("A(circle)", Math.PI * circ.r * circ.r, circ.area, 10e-5);
			assertEquals("P(circle)", 2 * Math.PI * circ.r, circ.perimeter, 10e-5);

			assertEquals("V(prism)", prism.base.area * prism.h, prism.volume, 10e-5);
			assertEquals("A(prism)", 2 * prism.base.area + prism.base.perimeter * prism.h, prism.surface, 10e-5);
			assertEquals("V(cylinder)", cyl.base.area * cyl.h, cyl.volume, 10e-5);
			assertEquals("A(cylinder)", 2 * cyl.base.area + cyl.base.perimeter * cyl.h, cyl.surface, 10e-5);

			// modify prism
			rect.a = 1.0;
			prism.h = 2.0;
			evaluator.invalidate(rect, prefix.appendLocalPart("a"), true);
			evaluator.invalidate(prism, prefix.appendLocalPart("h"), true);

			assertEquals("A(rect)", rect.a * rect.b, rect.area, 10e-5);
			assertEquals("P(rect)", 2 * rect.a + 2 * rect.b, rect.perimeter, 10e-5);
			assertEquals("V(prism)", prism.base.area * prism.h, prism.volume, 10e-5);
			assertEquals("A(prism)", 2 * prism.base.area + prism.base.perimeter * prism.h, prism.surface, 10e-5);

			// modify cylinder
			circ.r = 1.0;
			cyl.h = Math.PI;
			evaluator.invalidate(circ, prefix.appendLocalPart("r"), true);
			evaluator.invalidate(cyl, prefix.appendLocalPart("h"), true);

			assertEquals("A(circle)", Math.PI * circ.r * circ.r, circ.area, 10e-5);
			assertEquals("P(circle)", 2 * Math.PI * circ.r, circ.perimeter, 10e-5);
			assertEquals("V(cylinder)", cyl.base.area * cyl.h, cyl.volume, 10e-5);
			assertEquals("A(cylinder)", 2 * cyl.base.area + cyl.base.perimeter * cyl.h, cyl.surface, 10e-5);
		} else {
			System.err.println(ErrorUtils.printParseErrors(result));
			fail("Invalid rules format.");
		}
	}
}

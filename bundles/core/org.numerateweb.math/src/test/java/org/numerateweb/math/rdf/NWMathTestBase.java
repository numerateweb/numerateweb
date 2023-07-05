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
package org.numerateweb.math.rdf;

import net.enilink.komma.core.IGraph;
import net.enilink.komma.core.IReference;
import net.enilink.komma.core.URI;
import org.numerateweb.math.model.OMObject;
import org.numerateweb.math.ns.INamespaces;
import org.numerateweb.math.popcorn.PopcornBuilder;
import org.numerateweb.math.popcorn.PopcornExpr;
import org.numerateweb.math.popcorn.rules.MathRulesParser;
import org.numerateweb.math.rdf.rules.NWRULES;
import org.parboiled.Parboiled;
import org.parboiled.errors.ErrorUtils;
import org.parboiled.parserunners.ReportingParseRunner;
import org.parboiled.support.ParsingResult;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.Assert.fail;

abstract class NWMathTestBase {
	final MathRulesParser parser = Parboiled.createParser(MathRulesParser.class);

	private URI omrToUri(OMObject omr) {
		return (URI) omr.getArgs()[0];
	}

	protected Map<URI, ClassSpec> parsePopcornRules(String rules) {
		Map<URI, ClassSpec> classSpecs = new HashMap<>();
		ParsingResult<Object> result = new ReportingParseRunner<>(parser.Document()).run(rules.toCharArray());
		if (result.matched && result.resultValue != null) {
			OMObject constraintSet = (OMObject) result.resultValue;
			List<OMObject> constraints = Arrays.stream(constraintSet.getArgs(), 1,
					constraintSet.getArgs().length).map(r -> (OMObject) r).collect(Collectors.toList());
			for (OMObject constraint : constraints) {
				if (constraint.getType() == OMObject.Type.OMA) {
					// load constraints into a map
					URI classUri = omrToUri((OMObject) constraint.getArgs()[1]);
					URI propertyUri = omrToUri((OMObject) constraint.getArgs()[2]);
					OMObject expression = (OMObject) constraint.getArgs()[3];

					ClassSpec spec = classSpecs.computeIfAbsent(classUri, uri -> new ClassSpec());
					spec.constraints.put(propertyUri, expression);
				}
			}
		} else {
			System.err.println(ErrorUtils.printParseErrors(result));
			fail("Invalid rules format.");
		}
		return classSpecs;
	}

	protected void printRules(IGraph graph, INamespaces namespaces) {
		boolean first = true;
		for (IReference clazz : graph.filter(null, NWRULES.PROPERTY_CONSTRAINT, null).subjects()) {
			if (!first) {
				System.out.println();
			}
			first = false;
			System.out.println("Class: " + toString(clazz.getURI(), namespaces));
			System.out.println("Constraint:");
			for (Iterator<Object> it = graph.filter(clazz, NWRULES.PROPERTY_CONSTRAINT, null).objects().iterator();
			     it.hasNext(); ) {
				IReference constraint = (IReference) it.next();
				IReference expr = graph.filter(constraint, NWRULES.PROPERTY_EXPRESSION, null).objectReference();
				IReference property = graph.filter(constraint, NWRULES.PROPERTY_ONPROPERTY, null).objectReference();
				System.out.print(toString(property.getURI(), namespaces) + " = ");
				PopcornExpr popcorn = new NWMathGraphParser(graph, INamespaces.empty()).parse(expr,
						new PopcornBuilder(namespaces));
				System.out.println(popcorn.toString() + (it.hasNext() ? "," : ""));
			}
		}
	}

	protected String toString(URI uri, INamespaces namespaces) {
		String prefix = namespaces.getPrefix(uri.namespace());
		String localPart = uri.localPart();
		boolean hasLocalPart = localPart != null && localPart.length() > 0;
		StringBuilder text = new StringBuilder();
		if (prefix != null && prefix.length() > 0 && hasLocalPart) {
			text.append(prefix).append(":");
		}
		if (hasLocalPart && prefix != null) {
			text.append(localPart);
		} else {
			text.append("<").append(uri).append(">");
		}
		return text.toString();
	}

	static class ClassSpec {
		final Map<URI, OMObject> constraints = new HashMap<>();
	}
}

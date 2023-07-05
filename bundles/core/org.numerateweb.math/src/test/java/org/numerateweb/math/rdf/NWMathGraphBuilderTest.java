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

import net.enilink.komma.core.*;
import net.enilink.komma.core.visitor.IDataVisitor;
import net.enilink.komma.model.ModelUtil;
import org.junit.Assert;
import org.junit.Test;
import org.numerateweb.math.model.OMObject;
import org.numerateweb.math.model.OMObjectBuilder;
import org.numerateweb.math.model.OMObjectParser;
import org.numerateweb.math.ns.INamespaces;
import org.numerateweb.math.popcorn.PopcornBuilder;
import org.numerateweb.math.popcorn.PopcornExpr;
import org.numerateweb.math.popcorn.rules.MathRulesParser;
import org.numerateweb.math.rdf.rules.NWRULES;
import org.parboiled.Parboiled;
import org.parboiled.errors.ErrorUtils;
import org.parboiled.parserunners.ReportingParseRunner;
import org.parboiled.support.ParsingResult;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.Assert.fail;

/**
 * Basic tests for numerical evaluation of OM objects.
 */
public class NWMathGraphBuilderTest extends NWMathTestBase {
	@Test
	public void testMathRdfBuilder() throws IOException {
		final String rules;
		try (BufferedReader br = new BufferedReader(
				new InputStreamReader(getClass().getResourceAsStream("/rdf/constraints.nwrules")))) {
			rules = br.lines().collect(Collectors.joining("\n"));
		}
		Map<URI, ClassSpec> classSpecs = parsePopcornRules(rules);

		IGraph graph = new LinkedHashGraph();

		for (Map.Entry<URI, ClassSpec> specEntry : classSpecs.entrySet()) {
			Map<URI, OMObject> constraints = specEntry.getValue().constraints;
			for (OMObject mathObj : constraints.values()) {
				IReference rdfExpr = new OMObjectParser().parse(mathObj, new NWMathGraphBuilder(graph,
						INamespaces.empty()));
				OMObject rdfExprAsObj = new NWMathGraphParser(graph, INamespaces.empty()).parse(rdfExpr,
						new OMObjectBuilder());

				Assert.assertEquals(mathObj, rdfExprAsObj);
			}
		}
	}
}
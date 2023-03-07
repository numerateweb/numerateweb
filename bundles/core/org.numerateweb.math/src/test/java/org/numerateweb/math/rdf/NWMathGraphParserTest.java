package org.numerateweb.math.rdf;

import net.enilink.komma.core.*;
import net.enilink.komma.core.visitor.IDataVisitor;
import net.enilink.komma.model.ModelUtil;
import org.junit.Assert;
import org.junit.Test;
import org.numerateweb.math.model.OMObject;
import org.numerateweb.math.model.OMObjectBuilder;
import org.numerateweb.math.ns.INamespaces;
import org.numerateweb.math.rdf.rules.NWRULES;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Basic tests for numerical evaluation of OM objects.
 */
public class NWMathGraphParserTest extends NWMathTestBase {
	@Test
	public void testMathRdfParser() throws IOException {
		IGraph graph = new LinkedHashGraph();
		ModelUtil.readData(getClass().getResourceAsStream("/rdf/constraints.ttl"), null,
				"text/turtle", false, new IDataVisitor<Object>() {
					@Override
					public Object visitBegin() {
						return null;
					}

					@Override
					public Object visitEnd() {
						return null;
					}

					@Override
					public Object visitStatement(IStatement stmt) {
						graph.add(stmt);
						return null;
					}
				});

		/*
		// can be used to generate initial reference data
		INamespaces namespaces = new INamespaces() {
			URI EXAMPLE = URIs.createURI("http://example.org/vocab#");

			@Override
			public URI getNamespace(String prefix) {
				switch (prefix) {
					case "":
						return EXAMPLE;
				}
				return null;
			}

			@Override
			public String getPrefix(URI namespace) {
				if (EXAMPLE.equals(namespace)) {
					return "";
				}
				return null;
			}
		};
		printRules(graph, namespaces);
		*/

		final String rules;
		try (BufferedReader br = new BufferedReader(
				new InputStreamReader(getClass().getResourceAsStream("/rdf/constraints.nwrules")))) {
			rules = br.lines().collect(Collectors.joining("\n"));
		}
		Map<URI, ClassSpec> classSpecs = parsePopcornRules(rules);
		for (Map.Entry<URI, ClassSpec> specEntry : classSpecs.entrySet()) {
			IReference clazz = specEntry.getKey();
			Map<URI, OMObject> constraints = specEntry.getValue().constraints;
			Set<Object> rdfConstraints = graph.filter(clazz, NWRULES.PROPERTY_CONSTRAINT, null).objects();

			Assert.assertEquals(constraints.size(), rdfConstraints.size());

			for (Object constraint : rdfConstraints) {
				IReference property = graph.filter((IReference) constraint, NWRULES.PROPERTY_ONPROPERTY, null)
						.objectReference();
				OMObject expectedExpr = constraints.get(property);
				IReference rdfExpr = graph.filter((IReference) constraint,
						NWRULES.PROPERTY_EXPRESSION, null).objectReference();
				OMObject rdfExprAsObj = new NWMathGraphParser(graph, INamespaces.empty()).parse(rdfExpr,
						new OMObjectBuilder());

				Assert.assertEquals(expectedExpr, rdfExprAsObj);
			}
		}
	}
}
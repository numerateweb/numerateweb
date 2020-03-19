package org.numerateweb.math.cli;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.numerateweb.math.model.OMObject;
import org.numerateweb.math.model.OMObjectParser;
import org.numerateweb.math.ns.Namespaces;
import org.numerateweb.math.popcorn.rules.MathRulesParser;
import org.numerateweb.math.rdf.NWMathBuilder;
import org.numerateweb.math.rdf.RdfHelpers;
import org.numerateweb.math.rdf.rules.Constraint;
import org.numerateweb.math.rdf.rules.NWRULES;
import org.numerateweb.math.rdf.vocab.NWMATH;
import org.parboiled.Parboiled;
import org.parboiled.parserunners.ReportingParseRunner;
import org.parboiled.support.ParsingResult;

import net.enilink.komma.core.IEntityManager;
import net.enilink.komma.core.IEntityManagerFactory;
import net.enilink.komma.core.IReference;
import net.enilink.komma.core.IStatement;
import net.enilink.komma.core.ITransaction;
import net.enilink.komma.core.Namespace;
import net.enilink.komma.core.Statement;
import net.enilink.komma.core.URI;
import net.enilink.komma.core.URIs;
import net.enilink.komma.core.visitor.IDataAndNamespacesVisitor;
import net.enilink.komma.model.ModelUtil;
import net.enilink.vocab.rdf.RDF;

public class RulesToRdf implements CLICommand {
	@Override
	public String description() {
		return "Converts a rules file to RDF.";
	}

	@Override
	public String name() {
		return "convert-rules";
	}

	public void run(String... args) {
		List<String> files = new ArrayList<>();
		String type = null;
		for (int i = 0; i < args.length; i++) {
			if ("-t".equals(args[i]) && i < args.length - 1) {
				type = args[++i];
			} else {
				files.add(args[i]);
			}
		}
		if (files.isEmpty()) {
			System.err.println("Usage: " + usage());
			return;
		}
		Map<String, URI> nsMap = new HashMap<>();
		nsMap.put("", URIs.createURI("urn:numerateweb:"));
		nsMap.put("rdf", RDF.NAMESPACE_URI);
		nsMap.put("math", NWMATH.NAMESPACE_URI);
		nsMap.put("math-rules", NWRULES.NAMESPACE_URI);

		MathRulesParser parser = Parboiled.createParser(MathRulesParser.class);
		try (IEntityManagerFactory emFactory = RdfHelpers.createInMemoryEMFactory();
				final IEntityManager em = emFactory.get()) {
			for (Map.Entry<String, URI> e : nsMap.entrySet()) {
				em.setNamespace(e.getKey(), e.getValue());
			}
			for (String file : files) {
				ITransaction tx = em.getTransaction();
				try {
					tx.begin();
					
					String data = new String(Files.readAllBytes(Paths.get(file)));
					ParsingResult<Object> result = new ReportingParseRunner<Object>(parser.Document()).run(data);

					if (result.matched && result.resultValue != null) {
						OMObject constraintSet = (OMObject) result.resultValue;
						List<OMObject> constraints = Arrays
								.stream(constraintSet.getArgs(), 1, constraintSet.getArgs().length)
								.map(r -> (OMObject) r).collect(Collectors.toList());
						
						constraints.forEach(c -> {
							URI classUri = (URI)((OMObject)c.getArgs()[1]).getArgs()[0];
							URI propertyUri = (URI)((OMObject)c.getArgs()[2]).getArgs()[0];
							OMObject expression = (OMObject)c.getArgs()[3];
							
							// create constraint
							Constraint rdfConstraint = em.create(Constraint.class);
							rdfConstraint.setOnProperty(propertyUri);
							IReference rdfExpression = new OMObjectParser().parse(expression, new NWMathBuilder(em, new Namespaces(em)));
							rdfConstraint.setExpression(rdfExpression);
							// attach constraint to class
							em.add(new Statement(classUri, NWRULES.PROPERTY_CONSTRAINT, rdfConstraint));
						});
					}
					tx.commit();
				} catch (IOException e) {
					if (tx.isActive()) {
						tx.rollback();
					}
					System.err.println("Converting rules failed: " + e.getMessage());
				}
			}
			if (type != null) {
				type = ModelUtil.mimeType("example." + type);
			}
			IDataAndNamespacesVisitor<Void> visitor = ModelUtil.writeData(System.out, "urn:numerateweb:", type,
					"UTF-8");
			visitor.visitBegin();
			for (Map.Entry<String, URI> e : nsMap.entrySet()) {
				visitor.visitNamespace(new Namespace(e.getKey(), e.getValue()));
			}
			for (IStatement stmt : em.match(null, null, null).mapWith(new NodeIdShortener())) {
				visitor.visitStatement(stmt);
			}
			visitor.visitEnd();
		}
	}

	@Override
	public String usage() {
		return name() + " file [file1 ...] [-t ttl|xml|nt]";
	}
}
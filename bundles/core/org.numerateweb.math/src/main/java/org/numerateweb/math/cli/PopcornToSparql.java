package org.numerateweb.math.cli;

import org.numerateweb.math.ns.INamespaces;
import org.numerateweb.math.search.PopcornPatternToSparql;

public class PopcornToSparql implements CLICommand {
	@Override
	public String description() {
		return "Convert POPCORN pattern to SPARQL.";
	}

	@Override
	public String name() {
		return "popcorn-to-sparql";
	}

	public void run(String... args) {
		if (args.length == 0) {
			System.err.println("Usage: " + usage());
			return;
		}
		INamespaces ns = INamespaces.empty();
		PopcornPatternToSparql ppToSparql = new PopcornPatternToSparql(ns);
		String sparql = ppToSparql.toSparqlSelect(args[0]);
		System.out.println(sparql);
	}

	@Override
	public String usage() {
		return name() + " pattern";
	}
}
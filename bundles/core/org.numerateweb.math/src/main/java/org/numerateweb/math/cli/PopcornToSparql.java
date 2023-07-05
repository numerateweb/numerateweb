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
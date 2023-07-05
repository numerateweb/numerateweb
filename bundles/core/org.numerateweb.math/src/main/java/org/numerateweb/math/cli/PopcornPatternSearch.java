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

import java.util.ArrayList;
import java.util.List;

import net.enilink.komma.core.IEntity;
import net.enilink.komma.core.IEntityManager;
import net.enilink.komma.core.IEntityManagerFactory;
import net.enilink.komma.core.INamespace;
import net.enilink.komma.core.IStatement;
import net.enilink.komma.core.visitor.IDataAndNamespacesVisitor;
import net.enilink.komma.model.ModelUtil;

import org.numerateweb.math.ns.INamespaces;
import org.numerateweb.math.ns.Namespaces;
import org.numerateweb.math.popcorn.PopcornBuilder;
import org.numerateweb.math.rdf.RdfHelpers;
import org.numerateweb.math.rdf.NWMathParser;
import org.numerateweb.math.search.PopcornPatternToSparql;
import org.numerateweb.math.util.SparqlUtils;

public class PopcornPatternSearch implements CLICommand {
	@Override
	public String description() {
		return "Find mathematical expressions by pattern in POPCORN notation.";
	}

	@Override
	public String name() {
		return "search";
	}

	public void run(String... args) {
		List<String> patterns = new ArrayList<>();
		String type = null;
		for (int i = 0; i < args.length; i++) {
			if ("-t".equals(args[i]) && i < args.length - 1) {
				type = args[++i];
			} else {
				patterns.add(args[i]);
			}
		}
		if (patterns.isEmpty()) {
			System.err.println("Usage: " + usage());
			return;
		}
		String mimeType = null;
		if (type != null) {
			mimeType = ModelUtil.mimeType("file." + type);
		}
		String baseUri = "urn:base:";
		try (IEntityManagerFactory emFactory = RdfHelpers.createInMemoryEMFactory();
				IEntityManager em = emFactory.get()) {
			final List<IStatement> stmts = new ArrayList<>();
			ModelUtil.readData(System.in, baseUri, mimeType,
					new IDataAndNamespacesVisitor<Void>() {
						@Override
						public Void visitBegin() {
							return null;
						}

						@Override
						public Void visitEnd() {
							return null;
						}

						@Override
						public Void visitStatement(IStatement stmt) {
							stmts.add(stmt);
							return null;
						}

						@Override
						public Void visitNamespace(INamespace namespace) {
							em.setNamespace(namespace.getPrefix(),
									namespace.getURI());
							return null;
						}
					});
			em.getTransaction().begin();
			em.add(stmts);
			em.getTransaction().commit();
			stmts.clear();

			INamespaces ns = new Namespaces(em);
			PopcornPatternToSparql ppToSparql = new PopcornPatternToSparql(ns);
			for (String pattern : patterns) {
				String sparql = ppToSparql.toSparqlSelect(pattern);
				if (sparql != null) {
					sparql = SparqlUtils.prefix("", baseUri) + sparql;
					System.out.println(sparql);
					for (IEntity result : em.createQuery(sparql).evaluate(
							IEntity.class)) {
						String popcorn = new NWMathParser(ns).parse(result,
								new PopcornBuilder(new Namespaces(em)))
								.toString();
						System.out.println(popcorn);
					}
				} else {
					System.err.println("Invalid pattern: " + pattern);
				}
			}
		}
	}

	@Override
	public String usage() {
		return name() + " [-t ttl|rdf|owl|xml] pattern [pattern1 ...]";
	}
}
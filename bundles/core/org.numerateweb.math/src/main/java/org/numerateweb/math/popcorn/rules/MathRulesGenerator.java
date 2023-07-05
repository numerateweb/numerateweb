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
package org.numerateweb.math.popcorn.rules;

import org.numerateweb.math.ns.INamespaces;
import org.numerateweb.math.ns.Namespaces;
import org.numerateweb.math.popcorn.PopcornBuilder;
import org.numerateweb.math.rdf.NWMathParser;
import org.numerateweb.math.rdf.rules.Constraint;
import org.numerateweb.math.rdf.rules.NWRULES;
import org.numerateweb.math.util.SparqlUtils;

import net.enilink.commons.iterator.IExtendedIterator;
import net.enilink.komma.core.IQuery;
import net.enilink.komma.em.util.ISparqlConstants;
import net.enilink.komma.parser.manchester.ManchesterSyntaxGenerator;
import net.enilink.vocab.rdfs.Class;

public class MathRulesGenerator extends ManchesterSyntaxGenerator {
	@Override
	protected void classDefinitionExt(Class clazz) {
		super.classDefinitionExt(clazz);
		constraints(clazz);
		append("\n");
	}

	private MathRulesGenerator constraints(Class clazz) {
		IQuery<?> query = clazz.getEntityManager().createQuery(ISparqlConstants.PREFIX + //
				SparqlUtils.prefix("mathrl", NWRULES.NAMESPACE) + //
				"SELECT DISTINCT ?constraint WHERE { " + //
				"?class mathrl:constraint ?constraint . ?constraint mathrl:onProperty ?property . " + //
				"} ORDER BY ?property");
		query.setParameter("class", clazz);
		try (IExtendedIterator<Constraint> it = query.evaluate(Constraint.class)) {
			if (it.hasNext()) {
				withIndent(() -> {
					append("Constraint: \n");
					withIndent(() -> {
						while (it.hasNext()) {
							Constraint c = it.next();
							value(c.getOnProperty());
							append(" = ");
							INamespaces ns = new Namespaces(clazz.getEntityManager());
							String popcornExpression = new NWMathParser(ns).resolveURIs(false)
									.parse(c.getExpression(), new PopcornBuilder(ns)).toString();
							append(popcornExpression);
							if (it.hasNext()) {
								append(",");
							}
							append("\n");
						}
					});
				});
			}
		}
		return this;
	}
}

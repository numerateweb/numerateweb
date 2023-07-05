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

import net.enilink.commons.iterator.IExtendedIterator;
import net.enilink.komma.core.IEntity;
import net.enilink.komma.core.IGraph;
import net.enilink.komma.core.Initializable;
import net.enilink.komma.em.util.ISparqlConstants;
import net.enilink.komma.em.util.RESULTS;

import org.numerateweb.math.rdf.vocab.NWMATH;
import org.numerateweb.math.util.SparqlUtils;

public abstract class ObjectSupport implements IEntity,
		org.numerateweb.math.rdf.vocab.Object, Initializable {
	@Override
	public void init(IGraph graph) {
		if (graph == null) {
			StringBuilder sb = new StringBuilder();
			sb.append(ISparqlConstants.PREFIX
					+ SparqlUtils.prefix("math", NWMATH.NAMESPACE));
			sb.append("CONSTRUCT {");
			sb.append("?result a <").append(RESULTS.TYPE_RESULT).append("> . ");
			sb.append("?s ?p ?o . ");
			sb.append("} WHERE {");
			sb.append("{ select ?s where { ?this (math:arguments|math:symbol|math:operator|math:target|math:variables|math:binder|math:body|math:attributeKey|math:attributeValue|rdf:rest|rdf:first)* ?s . }} ");
			sb.append("?s ?p ?o .");
			// filter list tails
			sb.append("optional { ?s ?p ?o . bind (?s as ?result) filter not exists { [] rdf:rest ?s } }");
			sb.append("}");

			long start = System.currentTimeMillis();
			// prefetch all entities without inferred statements
			for (IExtendedIterator<?> it = getEntityManager()
					.createQuery(sb.toString(), false)
					.setParameter("this", this).evaluate(IEntity.class); it
					.hasNext(); it.next()) {
			}
			long end = System.currentTimeMillis();
			// System.out.println("Support in ms:" + (end - start));
		}
	}
}
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
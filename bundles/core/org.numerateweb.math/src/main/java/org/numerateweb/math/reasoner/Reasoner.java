package org.numerateweb.math.reasoner;

import java.util.ArrayList;
import java.util.List;

import org.numerateweb.math.model.OMObject;
import org.numerateweb.math.rdf.rules.NWRULES;
import org.numerateweb.math.util.SparqlUtils;

import net.enilink.komma.core.IBindings;
import net.enilink.komma.core.IEntityManager;
import net.enilink.komma.core.IQuery;
import net.enilink.komma.core.ITransaction;
import net.enilink.komma.em.concepts.IResource;
import net.enilink.vocab.rdf.RDF;

public class Reasoner {
	private AbstractEvaluator<?> evaluator;
	private IEntityManager em;

	public Reasoner(IEntityManager em, AbstractEvaluator<?> evaluator) {
		this.em = em;
		this.evaluator = evaluator;
	}

	public void run() {
		run(null);
	}

	protected Object convertToRdfValue(OMObject obj) {
		switch (obj.getType()) {
		case OMI:
		case OMF:
		case OMR:
			return obj.getArgs()[0];
		default:
			return null;
		}
	}

	public void run(final net.enilink.vocab.owl.Class clazz) {
		IQuery<?> query = em.createQuery(SparqlUtils.prefix("rdf", RDF.NAMESPACE)
				+ SparqlUtils.prefix("mathrl", NWRULES.NAMESPACE) + "SELECT DISTINCT ?instance ?property WHERE { "
				+ (clazz != null ? "?instance a ?class . " : "")
				+ "?instance a [mathrl:constraint [mathrl:onProperty ?property]] . " + "}");
		if (clazz != null) {
			query.setParameter("class", clazz);
		}

		// do not use transactions here, else
		// read data will never be cached by entity manager
		List<Runnable> setCmds = new ArrayList<>();
		try {
			for (IBindings<?> bindings : query.evaluate(IBindings.class)) {
				final IResource resource = (IResource) bindings.get("instance");
				final IResource property = (IResource) bindings.get("property");
				final OMObject result = evaluator.evaluate(resource, property, true);
				if (result != null) {
					setCmds.add(new Runnable() {
						@Override
						public void run() {
							resource.set(property, convertToRdfValue(result));
						}
					});
				}
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		// TODO use batched transactions to prevent out-of-memory errors
		// saves data within a transaction
		ITransaction transaction = em.getTransaction();
		try {
			transaction.begin();
			for (Runnable cmd : setCmds) {
				cmd.run();
			}
			transaction.commit();
		} finally {
			if (transaction.isActive()) {
				transaction.rollback();
			}
		}
	}

}

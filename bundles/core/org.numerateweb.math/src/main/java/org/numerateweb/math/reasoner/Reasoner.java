package org.numerateweb.math.reasoner;

import java.util.ArrayList;
import java.util.List;

import org.numerateweb.math.model.OMObject;
import org.numerateweb.math.model.OMObjectParser;
import org.numerateweb.math.ns.Namespaces;
import org.numerateweb.math.rdf.NWMathBuilder;
import org.numerateweb.math.rdf.rules.NWRULES;
import org.numerateweb.math.util.SparqlUtils;

import net.enilink.komma.core.IBindings;
import net.enilink.komma.core.IEntityManager;
import net.enilink.komma.core.IQuery;
import net.enilink.komma.core.IReference;
import net.enilink.komma.core.ITransaction;
import net.enilink.komma.em.concepts.IResource;
import net.enilink.vocab.rdf.RDF;

public class Reasoner {
	private AbstractEvaluator<?> evaluator;
	private IEntityManager em;
	private NWMathBuilder nwMathBuilder;

	public Reasoner(IEntityManager em, AbstractEvaluator<?> evaluator) {
		this.em = em;
		this.evaluator = evaluator;
		this.nwMathBuilder = new NWMathBuilder(em, new Namespaces(em));
	}

	public void run() {
		run(null);
	}

	protected Object convertToRdfValue(OMObject obj) {
		switch (obj.getType()) {
		case OMI:
		case OMF:
		case OMR:
		case OMSTR:
			return obj.getArgs()[0];
		default:
			return new OMObjectParser().parse(obj, nwMathBuilder);
		}
	}

	public void run(final net.enilink.vocab.owl.Class clazz) {
		IQuery<?> query = em.createQuery(SparqlUtils.prefix("rdf", RDF.NAMESPACE) + //
				SparqlUtils.prefix("mathrl", NWRULES.NAMESPACE) + //
				"SELECT DISTINCT ?instance ?property ?currentValue WHERE { " + //
				(clazz != null ? "?instance a ?class . " : "") + //
				"?instance a [mathrl:constraint [ mathrl:onProperty ?property ]] . " + //
				"optional { ?instance ?property ?currentValue }" + //
				"}");
		if (clazz != null) {
			query.setParameter("class", clazz);
		}

		// do not use transactions here, else
		// read data will never be cached by entity manager
		List<Runnable> setCmds = new ArrayList<>();
		List<Runnable> removeCmds = new ArrayList<>();
		try {
			for (IBindings<?> bindings : query.evaluate(IBindings.class)) {
				final IResource resource = (IResource) bindings.get("instance");
				final IResource property = (IResource) bindings.get("property");
				final Object currentValue = bindings.get("currentValue");
				final OMObject result = evaluator.evaluate(resource, property, true);
				if (result != null) {
					System.out.println(resource + " - " + property);
					if (currentValue instanceof IReference && ((IReference) currentValue).getURI() == null) {
						removeCmds.add(() -> {
							// remove RDF resources completely, e.g. OM error
							// objects or results of symbolic computations
							em.removeRecursive(currentValue, true);
						});
					}
					setCmds.add(() -> {
						resource.set(property, convertToRdfValue(result));
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
			for (Runnable cmd : removeCmds) {
				cmd.run();
			}
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

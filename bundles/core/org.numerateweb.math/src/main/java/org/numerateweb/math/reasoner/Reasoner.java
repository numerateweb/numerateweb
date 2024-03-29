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
package org.numerateweb.math.reasoner;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.numerateweb.math.eval.IEvaluator;
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
import net.enilink.vocab.rdfs.RDFS;

public class Reasoner {
	private IEvaluator evaluator;
	private IEntityManager em;
	private NWMathBuilder nwMathBuilder;

	public Reasoner(IEntityManager em, IEvaluator evaluator) {
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
				SparqlUtils.prefix("rdfs", RDFS.NAMESPACE) + //
				SparqlUtils.prefix("mathrl", NWRULES.NAMESPACE) + //
				"SELECT DISTINCT ?instance ?property ?currentValue WHERE { " + //
				(clazz != null ? "?instance a ?class . " : "") + //
				"?instance a [ rdfs:subClassOf* [ mathrl:constraint [ mathrl:onProperty ?property ] ] ] . " + //
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
				final OMObject result = evaluator.evaluate(resource, property, Optional.empty()).asOpenMath();
				if (result != null) {
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

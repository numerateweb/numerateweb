package org.numerateweb.math.reasoner;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.numerateweb.math.model.OMObject;
import org.numerateweb.math.model.OMObjectBuilder;
import org.numerateweb.math.ns.Namespaces;
import org.numerateweb.math.rdf.NWMathParser;
import org.numerateweb.math.rdf.rules.Constraint;
import org.numerateweb.math.rdf.rules.NWRULES;
import org.numerateweb.math.util.SparqlUtils;

import com.google.inject.TypeLiteral;

import net.enilink.commons.iterator.IExtendedIterator;
import net.enilink.commons.util.Pair;
import net.enilink.komma.core.Bindings;
import net.enilink.komma.core.IBindings;
import net.enilink.komma.core.IEntity;
import net.enilink.komma.core.IEntityManager;
import net.enilink.komma.core.IQuery;
import net.enilink.komma.core.IReference;
import net.enilink.komma.core.URI;
import net.enilink.komma.em.concepts.IClass;
import net.enilink.komma.em.concepts.IResource;
import net.enilink.komma.em.util.ISparqlConstants;
import net.enilink.vocab.rdfs.RDFS;

public class RdfModelAccess implements IModelAccess {
	protected static class ConstraintSpec {
		protected final Constraint constraint;
		protected final IReference onClass;

		ConstraintSpec(IReference onClass, Constraint constraint) {
			this.onClass = onClass;
			this.constraint = constraint;
		}
	}

	public ICache<Pair<Object, IReference>, ResultSpec<OMObject>> expressionCache;

	protected final IEntityManager manager;

	private ThreadLocal<IQuery<?>> propertyValuesQuery = new ThreadLocal<>();

	private ThreadLocal<IQuery<?>> propertyValuesQueryWRestriction = new ThreadLocal<>();

	public RdfModelAccess(IEntityManager manager, CacheManager cacheManager) {
		this.manager = manager;

		this.expressionCache = cacheManager
				.get(new TypeLiteral<ICache<Pair<Object, IReference>, ResultSpec<OMObject>>>() {
				});
	}

	private ResultSpec<OMObject> cacheExpression(IReference clazz, IReference property, ResultSpec<OMObject> result) {
		expressionCache.put(new Pair<>(clazz, property), result);
		return result;
	}

	public ResultSpec<ConstraintSpec> getDirectConstraint(Object subject, IReference property) {
		IQuery<?> q = manager.createQuery(ISparqlConstants.PREFIX + SparqlUtils.prefix("mathrl", NWRULES.NAMESPACE)
				+ "SELECT DISTINCT ?class ?constraint WHERE { " + (subject != null ? "?subject a ?class . " : "")
				+ "?class mathrl:constraint ?constraint . " + "?constraint mathrl:onProperty ?property } LIMIT 2");
		if (subject != null) {
			q.setParameter("subject", subject);
		}
		q.setParameter("property", property).bindResultType("constraint", Constraint.class).restrictResultType("class",
				IReference.class);

		try (IExtendedIterator<IBindings<Object>> it = q.evaluate(Bindings.typed(Object.class))) {
			while (it.hasNext()) {
				IBindings<?> bindings = it.next();
				IReference clazz = (IReference) bindings.get("class");
				Constraint constraint = (Constraint) bindings.get("constraint");
				if (it.hasNext()) {
					return ResultSpec.create(Cardinality.MULTI, null);
				} else {
					return ResultSpec.create(Cardinality.SINGLE, new ConstraintSpec(clazz, constraint));
				}
			}
		}
		return ResultSpec.create(Cardinality.NONE, null);
	}

	@Override
	public ResultSpec<OMObject> getExpressionSpec(Object subject, IReference property) {
		if (subject == null) {
			return getSubjectIndependentExpressionSpec(property);
		} else {
			return getSubjectDependentExpressionSpec(subject, property);
		}
	}

	protected Constraint getInheritedConstraint(IClass clazz, IReference property) {
		Constraint constraint = null;
		IQuery<?> q = manager
				.createQuery(ISparqlConstants.PREFIX + SparqlUtils.prefix("mathrl", NWRULES.NAMESPACE)
						+ "SELECT distinct ?constraint WHERE { " + "?c mathrl:constraint ?constraint . "
						+ "?constraint mathrl:onProperty ?property " + "} LIMIT 2")
				.setParameter("c", clazz).setParameter("property", property);

		try (IExtendedIterator<Constraint> it = q.evaluate(Constraint.class)) {
			if (it.hasNext()) {
				constraint = it.next();
			}
			if (it.hasNext()) {
				// more than one constraint on same class and property
				throw new RuntimeException("More than one constraint on same class and property");
			}
		}

		if (constraint == null) {
			for (IClass superClass : sort(clazz.getSuperClasses(true, true).toList())) {
				constraint = getInheritedConstraint(superClass, property);
				if (constraint != null) {
					break;
				}
			}
		}
		return constraint;
	}

	@Override
	public IExtendedIterator<?> getInstances(IReference clazz) {
		return manager.find(clazz, IClass.class).getInstancesAsReferences();
	}

	@Override
	public IReference createInstance(URI uri, IReference clazz, Map<URI, Object> args) {
		// TODO: test this initial implementation
		IEntity entity = manager.createNamed(uri, clazz, RDFS.TYPE_RESOURCE);
		args.forEach((key, value) -> ((IResource) entity).addProperty(key, value));
		return entity.getReference();
	}

	public OMObject getOMObject(Constraint constraint) {
		Namespaces namespaces = new Namespaces(manager);
		return new NWMathParser(namespaces).parse(constraint.getExpression(), new OMObjectBuilder());
	}

	@Override
	public IExtendedIterator<?> getPropertyValues(Object subject, IReference property,
			Optional<IReference> restriction) {
		IQuery<?> query;
		// DISTINCT is required to suppress duplicates due to explicit and
		// inferred statements
		if (restriction.isPresent() && propertyValuesQueryWRestriction.get() == null) {
			query = manager.createQuery("SELECT DISTINCT ?value WHERE { ?subject ?property ?value }");
			propertyValuesQueryWRestriction.set(query);
		} else {
			query = manager
					.createQuery("SELECT DISTINCT ?value WHERE { ?subject ?property ?value . ?value a ?restriction }");
			propertyValuesQuery.set(query);
		}

		query.setParameter("subject", subject).setParameter("property", property);
		restriction.ifPresent(r -> {
			query.setParameter("restriction", restriction);
		});

		IExtendedIterator<?> it = query.evaluate();
		return it;
	}

	private ResultSpec<OMObject> getSubjectDependentExpressionSpec(Object subject, IReference property) {
		ResultSpec<ConstraintSpec> result = getDirectConstraint(subject, property);

		switch (result.cardinality) {
		case NONE:
			return cacheExpression(null, property, ResultSpec.create(Cardinality.NONE, (OMObject) null));
		case SINGLE:
			return cacheExpression(result.result.onClass, property,
					ResultSpec.create(Cardinality.SINGLE, getOMObject(result.result.constraint)));
		case MULTI:
			for (IClass clazz : sort(((IResource) manager.find((IReference) subject)).getDirectClasses().toList())) {
				Pair<Object, IReference> key = new Pair<>(clazz, property);
				CacheResult<ResultSpec<OMObject>> cacheResult = expressionCache.get(key);
				if (cacheResult != null) {
					return cacheResult.value;
				}
				Constraint constraint = getInheritedConstraint(clazz, property);
				if (constraint != null) {
					return cacheExpression(clazz, property,
							ResultSpec.create(Cardinality.SINGLE, getOMObject(constraint)));
				} else {
					// cache but do not return
					cacheExpression(clazz, property, ResultSpec.create(Cardinality.NONE, (OMObject) null));
				}
			}
			// TODO should we return something here?
		default:
			throw new RuntimeException("Not matching all cases here");
		}

	}

	private ResultSpec<OMObject> getSubjectIndependentExpressionSpec(IReference property) {
		// check properties that have only one constraint referring to it
		Pair<Object, IReference> key = new Pair<>(null, property);
		CacheResult<ResultSpec<OMObject>> result = expressionCache.get(key);
		if (result != null) {
			return result.value;
		}

		// try to get the only one existing constraint for the only property
		ResultSpec<ConstraintSpec> resultSpec = getDirectConstraint(null, property);
		switch (resultSpec.cardinality) {
		case NONE:
			return cacheExpression(null, property, ResultSpec.create(Cardinality.NONE, (OMObject) null));
		case SINGLE:
			return cacheExpression(null, property,
					ResultSpec.create(Cardinality.SINGLE, getOMObject(resultSpec.result.constraint)));
		case MULTI:
			return cacheExpression(null, property, ResultSpec.create(Cardinality.MULTI, (OMObject) null));
		default:
			throw new RuntimeException("Not matching all cases here");
		}
	}

	protected List<IClass> sort(List<IClass> classes) {
		Collections.sort(classes, IResource.RANK_COMPARATOR);
		return classes;
	}
}

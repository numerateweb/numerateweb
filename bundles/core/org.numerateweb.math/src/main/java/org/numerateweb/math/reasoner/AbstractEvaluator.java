package org.numerateweb.math.reasoner;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.numerateweb.math.model.OMObject;
import org.numerateweb.math.model.OMObjectBuilder;
import org.numerateweb.math.ns.Namespaces;
import org.numerateweb.math.rdf.NWMathParser;
import org.numerateweb.math.rules.Constraint;
import org.numerateweb.math.rules.NWRULES;
import org.numerateweb.math.util.SparqlUtils;

import com.google.inject.TypeLiteral;

import net.enilink.commons.iterator.IExtendedIterator;
import net.enilink.commons.util.Pair;
import net.enilink.komma.core.Bindings;
import net.enilink.komma.core.IBindings;
import net.enilink.komma.core.IEntityManager;
import net.enilink.komma.core.IQuery;
import net.enilink.komma.core.IReference;
import net.enilink.komma.core.IValue;
import net.enilink.komma.em.concepts.IClass;
import net.enilink.komma.em.concepts.IResource;
import net.enilink.komma.em.util.ISparqlConstants;
import net.enilink.komma.model.ModelUtil;

public abstract class AbstractEvaluator<E> {

	protected static class ConstraintSpec {
		protected final Constraint constraint;
		protected final IReference onClass;

		ConstraintSpec(IReference onClass, Constraint constraint) {
			this.onClass = onClass;
			this.constraint = constraint;
		}
	}

	public ICache<Pair<IReference, IReference>, ResultSpec<OMObject>> expressionCache;

	private IEntityManager manager;
	public ICache<OMObject, E> parsedExpressionCache;
	private ThreadLocal<Set<Pair<IReference, IReference>>> path = new ThreadLocal<Set<Pair<IReference, IReference>>>() {
		protected Set<Pair<IReference, IReference>> initialValue() {
			return new HashSet<Pair<IReference, IReference>>();
		};
	};

	private ThreadLocal<IQuery<IValue>> propertyValuesQuery = new ThreadLocal<IQuery<IValue>>();
	public ICache<Pair<IReference, IReference>, E> valueCache;

	protected AbstractEvaluator(IEntityManager manager, CacheManager cacheManager) {
		this.manager = manager;

		this.valueCache = cacheManager.get(new TypeLiteral<ICache<Pair<IReference, IReference>, E>>() {
		});
		this.expressionCache = cacheManager
				.get(new TypeLiteral<ICache<Pair<IReference, IReference>, ResultSpec<OMObject>>>() {
				});
		this.parsedExpressionCache = cacheManager.get(new TypeLiteral<ICache<OMObject, E>>() {
		});
	}

	private ResultSpec<OMObject> cache(IReference clazz, IReference property, ResultSpec<OMObject> result) {
		expressionCache.put(new Pair<IReference, IReference>(clazz, property), result);
		return result;
	}

	/**
	 * Return an expression object for the error message. The result could be
	 * for example an expression that represents "not a number".
	 * 
	 * @return An expression object for the given error
	 */
	protected abstract E errorToExpression(String message);

	/**
	 * Evaluate a parsed expression.
	 * 
	 * @param subject
	 *            The subject resource
	 * @param expression
	 *            The expression to evaluate
	 * @return Result of the evaluation
	 */
	protected abstract E eval(IReference subject, E expression);

	public OMObject evaluate(IReference subject, IReference property, boolean usePersistentValue) {
		E result = null;
		Pair<IReference, IReference> key = new Pair<IReference, IReference>(subject, property);
		CacheResult<E> cacheResult = valueCache.get(key);
		if (cacheResult != null) {
			result = cacheResult.value;
		} else {
			OMObject omobj = getConstraintExpression(subject, property);
			if (omobj != null) {
				result = evaluateExpression(subject, property, omobj);
			} else if (usePersistentValue) {
				result = getPropertyValue(subject, property);
			}
			valueCache.put(key, result);
		}
		return unparse(result);
	}

	public E evaluateExpression(IReference subject, IReference property, OMObject omobj) {
		if (omobj == null) {
			return null;
		}

		Pair<IReference, IReference> key = new Pair<IReference, IReference>(subject, property);
		// TODO is this correct? How to detect cycles?
		if (path.get().contains(key)) {
			return errorToExpression("Detected cycle in evaluation");
		} else {
			E parsedExpression = parse(subject, omobj);
			if (parsedExpression == null) {
				return null;
			}

			path.get().add(key);
			E result = eval(subject, parsedExpression);
			path.get().remove(key);
			return result;
		}
	}

	public OMObject getConstraintExpression(IReference subject, IReference property) {
		ResultSpec<OMObject> tmp = getExpressionSpec(subject, property);
		switch (tmp.cardinality) {
		case NONE:
			return null;
		case SINGLE:
			return tmp.result;
		case MULTI:
			throw new RuntimeException("More than one constraint on same class and property");
		default:
			throw new RuntimeException("Not matching all cases here");
		}
	}

	public ResultSpec<ConstraintSpec> getDirectConstraint(IReference subject, IReference property) {
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

	public ResultSpec<OMObject> getExpressionSpec(IReference subject, IReference property) {
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

	public IEntityManager getManager() {
		return manager;
	}

	public OMObject getOMObject(Constraint constraint) {
		Namespaces namespaces = new Namespaces(manager);
		return new NWMathParser(namespaces).parse(constraint.getExpression(), new OMObjectBuilder());
	}

	protected E getPropertyValue(IReference subject, IReference property) {
		IExtendedIterator<IValue> iterator = getPropertyValues(subject, property);
		if (!iterator.hasNext()) {
			/*
			 * This should return null, because actually no statement matching
			 * the pattern (s, p, null) was found
			 */
			return null;
		}

		IValue first = iterator.next();
		if (iterator.hasNext()) {
			String message = String.format("%s has more than one value for property %s", ModelUtil.getLabel(subject),
					ModelUtil.getLabel(property));
			return errorToExpression(message);
		} else {
			Object value = manager.toInstance(first);
			return javaValueToExpression(value);
		}
	}

	protected IExtendedIterator<IValue> getPropertyValues(IReference subject, IReference property) {
		if (propertyValuesQuery.get() == null) {
			IQuery<IValue> query = manager.createQuery("SELECT DISTINCT ?obj WHERE { ?subj ?pred ?obj }")
					.bindResultType(IValue.class);
			propertyValuesQuery.set(query);
		}
		return propertyValuesQuery.get().setParameter("subj", subject).setParameter("pred", property).evaluate();
	}

	private ResultSpec<OMObject> getSubjectDependentExpressionSpec(IReference subject, IReference property) {
		ResultSpec<ConstraintSpec> result = getDirectConstraint(subject, property);

		switch (result.cardinality) {
		case NONE:
			return cache(null, property, ResultSpec.create(Cardinality.NONE, (OMObject) null));
		case SINGLE:
			return cache(result.result.onClass, property,
					ResultSpec.create(Cardinality.SINGLE, getOMObject(result.result.constraint)));
		case MULTI:
			for (IClass clazz : sort(((IResource) manager.find(subject)).getDirectClasses().toList())) {
				Pair<IReference, IReference> key = new Pair<IReference, IReference>(clazz, property);
				CacheResult<ResultSpec<OMObject>> cacheResult = expressionCache.get(key);
				if (cacheResult != null) {
					return cacheResult.value;
				}
				Constraint constraint = getInheritedConstraint(clazz, property);
				if (constraint != null) {
					return cache(clazz, property, ResultSpec.create(Cardinality.SINGLE, getOMObject(constraint)));
				} else {
					// cache but do not return
					cache(clazz, property, ResultSpec.create(Cardinality.NONE, (OMObject) null));
				}
			}
			// TODO should we return something here?
		default:
			throw new RuntimeException("Not matching all cases here");
		}

	}

	private ResultSpec<OMObject> getSubjectIndependentExpressionSpec(IReference property) {
		// check properties that have only one constraint referring to it
		Pair<IReference, IReference> key = new Pair<>(null, property);
		CacheResult<ResultSpec<OMObject>> result = expressionCache.get(key);
		if (result != null) {
			return result.value;
		}

		// try to get the only one existing constraint for the only property
		ResultSpec<ConstraintSpec> resultSpec = getDirectConstraint(null, property);
		switch (resultSpec.cardinality) {
		case NONE:
			return cache(null, property, ResultSpec.create(Cardinality.NONE, (OMObject) null));
		case SINGLE:
			return cache(null, property,
					ResultSpec.create(Cardinality.SINGLE, getOMObject(resultSpec.result.constraint)));
		case MULTI:
			return cache(null, property, ResultSpec.create(Cardinality.MULTI, (OMObject) null));
		default:
			throw new RuntimeException("Not matching all cases here");
		}
	}

	/**
	 * Converts a Java value (double, long, int, ...) to an expression object.
	 * 
	 * @param value
	 *            The Java value
	 * @return An expression object
	 */
	protected abstract E javaValueToExpression(Object value);

	private E parse(IReference subject, OMObject omobj) {
		if (omobj == null) {
			return null;
		} else {
			CacheResult<E> result = parsedExpressionCache.get(omobj);
			if (result != null) {
				return result.value;
			} else {
				boolean doCache = true;

				E parsedExpression = parse(omobj);
				if (doCache) {
					parsedExpressionCache.put(omobj, parsedExpression);
				}
				return parsedExpression;
			}
		}
	}

	/**
	 * Transform given OM object to a specific expression.
	 * 
	 * @param omobj
	 *            The OM object
	 * @return An expression that is specific for this evaluator.
	 */
	protected abstract E parse(OMObject omobj);

	/**
	 * Transform given expression into an OM object.
	 * 
	 * @param expr
	 *            The expression
	 * @return An OM object for the given expression.
	 */
	protected abstract OMObject unparse(E expr);

	protected List<IClass> sort(List<IClass> classes) {
		Collections.sort(classes, IResource.RANK_COMPARATOR);
		return classes;
	}
}

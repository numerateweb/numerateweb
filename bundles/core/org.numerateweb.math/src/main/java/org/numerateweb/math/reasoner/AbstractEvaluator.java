package org.numerateweb.math.reasoner;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.numerateweb.math.model.OMObject;

import com.google.inject.TypeLiteral;

import net.enilink.commons.iterator.IExtendedIterator;
import net.enilink.commons.util.Pair;
import net.enilink.komma.core.IReference;
import net.enilink.komma.model.ModelUtil;

public abstract class AbstractEvaluator<E> {

	public ICache<OMObject, E> parsedExpressionCache;
	private ThreadLocal<Set<Pair<Object, IReference>>> path = new ThreadLocal<Set<Pair<Object, IReference>>>() {
		protected Set<Pair<Object, IReference>> initialValue() {
			return new HashSet<>();
		};
	};

	public ICache<Pair<Object, IReference>, E> valueCache;

	protected final IModelAccess modelAccess;

	@SuppressWarnings("unchecked")
	protected AbstractEvaluator(IModelAccess modelAccess, CacheManager cacheManager) {
		this.modelAccess = modelAccess;

		this.valueCache = (ICache<Pair<Object, IReference>, E>) cacheManager
				.get(new TypeLiteral<ICache<Pair<Object, IReference>, Object>>() {
				});

		this.parsedExpressionCache = (ICache<OMObject, E>) cacheManager
				.get(new TypeLiteral<ICache<OMObject, Object>>() {
				});
	}

	/**
	 * Return an expression object for the error message. The result could be for
	 * example an expression that represents "not a number".
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
	protected abstract E eval(Object subject, E expression);

	public OMObject evaluate(Object subject, IReference property, boolean usePersistentValue) {
		E result = null;
		Pair<Object, IReference> key = new Pair<>(subject, property);
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

	public E evaluateExpression(Object subject, IReference property, OMObject omobj) {
		if (omobj == null) {
			return null;
		}

		Pair<Object, IReference> key = new Pair<>(subject, property);
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

	public OMObject getConstraintExpression(Object subject, IReference property) {
		ResultSpec<OMObject> tmp = modelAccess.getExpressionSpec(subject, property);
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

	protected E getPropertyValue(Object subject, IReference property) {
		IExtendedIterator<?> iterator = modelAccess.getPropertyValues(subject, property, Optional.empty());
		if (!iterator.hasNext()) {
			/*
			 * This should return null, because actually no statement matching the pattern
			 * (s, p, null) was found
			 */
			return null;
		}

		Object first = iterator.next();
		if (iterator.hasNext()) {
			String message = String.format("%s has more than one value for property %s", ModelUtil.getLabel(subject),
					ModelUtil.getLabel(property));
			return errorToExpression(message);
		} else {
			return javaValueToExpression(first);
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

	private E parse(Object subject, OMObject omobj) {
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
}

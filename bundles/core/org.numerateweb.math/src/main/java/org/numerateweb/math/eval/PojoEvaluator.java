package org.numerateweb.math.eval;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Function;

import org.numerateweb.math.model.OMObject;
import org.numerateweb.math.reasoner.CacheManager;
import org.numerateweb.math.reasoner.DependencyGraph;
import org.numerateweb.math.reasoner.PojoModelAccess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.enilink.commons.util.Pair;
import net.enilink.komma.core.IReference;

public class PojoEvaluator extends SimpleEvaluator {

	protected final static Logger logger = LoggerFactory.getLogger(PojoEvaluator.class);

	protected DependencyGraph<Object> dependencyGraph;
	protected Map<Class<?>, Function<Object, Collection<String>>> ignoreLookup = new HashMap<>();

	public PojoEvaluator(PojoModelAccess modelAccess, CacheManager cacheManager) {
		super(modelAccess, cacheManager);
		dependencyGraph = new DependencyGraph<>();
	}

	public void registerIgnoreLookup(Class<?> clazz, Function<Object, Collection<String>> getter) {
		ignoreLookup.put(clazz, getter);
	}

	@Override
	protected void recordDependency(Pair<Object, IReference> from, Pair<Object, IReference> to) {
		logger.trace("adding dependency {} -> {}", from, to);
		dependencyGraph.addDependency(from, to);
	}

	@Override
	public Result evaluate(Object subject, IReference property, Optional<IReference> restriction) {
		// check if already in cache
		boolean cached = (null != valueCache.get(new Pair<Object, IReference>(subject, property)));
		// check if the property should be calculated for this subject
		if (ignoreLookup.containsKey(subject.getClass())
				&& ignoreLookup.get(subject.getClass()).apply(subject).contains(property.getURI().localPart())) {
			if (cached) {
				// remove from cache, re-evaluate
				invalidate(subject, property, true);
			}
			// return the plain property value
			return result(getPropertyValue(subject, property));
		}
		// WARNING: side effect, adds dependencies
		Result result = super.evaluate(subject, property, restriction);
		if (getConstraintExpression(subject, property) == null) {
			// value from model, no need to update field value
			return result;
		}
		if (cached) {
			// value was in cache, no need to update field value
			// WARNING: do NOT short-circuit, see side effect above!
			return result;
		}
		// update field value with expression result(s)
		try {
			List<Object> results = result.toList();
			Object singleResult = (result.isSingle()) ? results.get(0) : null;
			if (singleResult instanceof Exception || (singleResult instanceof OMObject && ((OMObject) singleResult).getType() == OMObject.Type.OME)) {
				logger.warn("evaluation of ({}, {}) failed: {}", subject, property, singleResult);
				return result(singleResult);
			} else {
				logger.trace("setting ({}, {}) to value={}", subject, property, results);
				((PojoModelAccess) modelAccess).setPropertyValue(subject, property, results);
				return result(result.isSingle() ? singleResult : results);
			}
		} catch (NoSuchElementException nse) {
			return result(null);
		}
	}

	public void invalidate(Object subject, IReference property) {
		invalidate(subject, property, false);
	}

	public void invalidate(Object subject, IReference property, boolean reevaluate) {
		Collection<Pair<Object, IReference>> invalidatedRoots = new ArrayList<>();
		dependencyGraph.invalidate(new Pair<>(subject, property), (obj, isRoot) -> {
			@SuppressWarnings("unchecked")
			Pair<Object, IReference> pair = (Pair<Object, IReference>) obj;
			logger.trace("CB: invalidating {}", pair);
			valueCache.remove(pair);
			if (isRoot) {
				invalidatedRoots.add(pair);
			}
			return null;
		});
		if (reevaluate) {
			for (Pair<Object, IReference> pair : invalidatedRoots) {
				logger.trace("re-evaluating {}", pair);
				try {
					evaluate(pair.getFirst(), pair.getSecond(), Optional.empty());
				} catch (Exception e) {
					logger.error(e.getMessage());
				}
			}
		}
		invalidatedRoots.clear();
	}
}

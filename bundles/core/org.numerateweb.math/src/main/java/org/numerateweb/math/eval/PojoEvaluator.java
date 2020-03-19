package org.numerateweb.math.eval;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.numerateweb.math.model.OMObject;
import org.numerateweb.math.reasoner.CacheManager;
import org.numerateweb.math.reasoner.DependencyGraph;
import org.numerateweb.math.reasoner.PojoModelAccess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.enilink.commons.util.Pair;
import net.enilink.komma.core.IReference;
import net.enilink.komma.core.URI;

public class PojoEvaluator extends SimpleEvaluator {

	protected final static Logger logger = LoggerFactory.getLogger(PojoEvaluator.class);

	protected DependencyGraph<Object> dependencyGraph;
	protected Map<Class<?>, Function<Object, Collection<String>>> ignoreLookup = new HashMap<>();
	protected Map<Pair<Object, IReference>, List<Object>> propertiesToManagedInstances = new HashMap<>();

	public PojoEvaluator(PojoModelAccess modelAccess, CacheManager cacheManager) {
		super(modelAccess, cacheManager);
		dependencyGraph = new DependencyGraph<>();
	}
	
	@Override
	public Object createInstance(URI uri, IReference clazz, Map<URI, Object> args) {
		Pair<Object, IReference> property = path.get().peekLast();
		Object instance = modelAccess.createInstance(property.getFirst(), property.getSecond().getURI(), uri, clazz, args);
		List<Object> instances = propertiesToManagedInstances.computeIfAbsent(property, k -> new ArrayList<>());
		instances.add(instance);
		return instance;
	}
	
	public void registerIgnoreLookup(Class<?> clazz, Function<Object, Collection<String>> getter) {
		ignoreLookup.put(clazz, getter);
	}

	@Override
	protected void recordDependency(Pair<Object, IReference> from, Pair<Object, IReference> to) {
		logger.trace("adding dependency {} -> {}", from, to);
		dependencyGraph.addDependency(from, to);
	}

	/**
	 * Evaluate in the root context, saves prior context path information (if any)
	 * and restores it after the evaluation.
	 */
	public Result evaluateRoot(Object subject, IReference property, Optional<IReference> restriction) {
		Path<Pair<Object, IReference>> outerPath = path.get();
		if (null == outerPath.peekLast()) {
			return evaluate(subject, property, restriction);
		}
		try {
			path.set(new Path<Pair<Object, IReference>>());
			return evaluate(subject, property, restriction);
		} finally {
			path.set(outerPath);
		}
	}

	@Override
	public Result evaluate(Object subject, IReference property, Optional<IReference> restriction) {
		// check if already in cache
		boolean cached = (null != valueCache.get(new Pair<Object, IReference>(subject, property)));
		// check if the property should be calculated for this subject
		if (ignoreLookup.containsKey(subject.getClass())
				&& ignoreLookup.get(subject.getClass()).apply(subject).contains(property.getURI().localPart())) {
			Object value = getPropertyValue(subject, property);
			logger.trace("ignoring evaluation for ({}.{}), existing value = {}", subject, property, value);
			// property should be ignored for this subject, add current value to cache
			valueCache.put(new Pair<Object, IReference>(subject, property), value);
			cached = true;
		}
		// WARNING: side effect, adds dependencies; DO NOT skip when cached!
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
		Set<Object> invalidatedInstances = new HashSet<>();
		dependencyGraph.invalidate(new Pair<>(subject, property), (obj, isRoot) -> {
			@SuppressWarnings("unchecked")
			Pair<Object, IReference> pair = (Pair<Object, IReference>) obj;
			logger.trace("CB: invalidating {}", pair);
			valueCache.remove(pair);
			if (isRoot) {
				invalidatedRoots.add(pair);
			}
			// remove potentially created instances
			propertiesToManagedInstances.computeIfPresent(pair, (k, list) -> {
				if (list != null) {
					invalidatedInstances.addAll(list);
				}
				return null;
			});
			return null;
		});
		// remove computations for invalidated instances from dependency graph
		Collection<Pair<Object, IReference>> rootsForReevalution = invalidatedRoots.stream().filter(root -> {
			if (invalidatedInstances.contains(root.getFirst())) {
				dependencyGraph.remove(root);
				// do not consider this node for reevaluation
				return false;
			} else {
				return true;
			}
		}).collect(Collectors.toList());
		
		if (reevaluate) {
			for (Pair<Object, IReference> pair : rootsForReevalution) {
				logger.trace("re-evaluating {}", pair);
				try {
					evaluateRoot(pair.getFirst(), pair.getSecond(), Optional.empty());
				} catch (Exception e) {
					logger.error(e.getMessage());
				}
			}
		}
	}
}

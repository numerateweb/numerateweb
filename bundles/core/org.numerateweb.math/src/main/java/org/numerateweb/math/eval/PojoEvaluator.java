package org.numerateweb.math.eval;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

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

	public PojoEvaluator(PojoModelAccess modelAccess, CacheManager cacheManager) {
		super(modelAccess, cacheManager);
		dependencyGraph = new DependencyGraph<>();
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
			Object singleResult = (results.size() == 1) ? results.get(0) : null;
			if (singleResult instanceof Exception || (singleResult instanceof OMObject && ((OMObject) singleResult).getType() == OMObject.Type.OME)) {
				logger.warn("evaluation of ({},{}) failed: {}", subject, property, singleResult);
			} else {
				logger.warn("setting ({},{}) to value={}", subject, property, results);
				((PojoModelAccess) modelAccess).setPropertyValue(subject, property, results);
			}
		} catch (NoSuchElementException nse) {
		} catch (IllegalArgumentException iae) {
			logger.error(iae.getMessage());
		}

		// need to evaluate again, result iterator has been consumed
		return super.evaluate(subject, property, restriction);
	}

	public void invalidate(Object subject, IReference property) {
		invalidate(subject, property, false);
	}

	public void invalidate(Object subject, IReference property, boolean reevaluate) {
		Collection<Pair<Object, IReference>> invalidatedPairs = new ArrayList<>();
		dependencyGraph.invalidate(new Pair<>(subject, property), (obj) -> {
			@SuppressWarnings("unchecked")
			Pair<Object, IReference> pair = (Pair<Object, IReference>) obj;
			logger.trace("CB: invalidating {}", pair);
			valueCache.remove(pair);
			invalidatedPairs.add(pair);
			return null;
		});
		if (reevaluate) {
			for (Pair<Object, IReference> pair : invalidatedPairs) {
				logger.trace("re-evaluating {}", pair);
				evaluate(pair.getFirst(), pair.getSecond(), Optional.empty());
			}
		}
	}
}

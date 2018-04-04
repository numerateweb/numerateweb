package org.numerateweb.math.eval;

import java.util.Optional;

import org.numerateweb.math.model.OMObject;

import net.enilink.commons.iterator.IExtendedIterator;
import net.enilink.komma.core.IReference;

/**
 * Evaluator interface for mathematical expressions.
 */
public interface IEvaluator {
	interface Result extends IExtendedIterator<Object> {
		OMObject asOpenMath();
	}

	/**
	 * Evaluates the value of the given <code>property</code> of the object
	 * <code>subject</code>.
	 * 
	 * @param subject
	 *            The object that has a certain property
	 * @param property
	 *            A property if the given subject
	 * @param restriction
	 *            An optional restriction on the property values
	 * @return Iterator of the property values
	 */
	Result evaluate(Object subject, IReference property, Optional<IReference> restriction);

	/**
	 * Return all instances of type <code>clazz</code> contained within the model.
	 * 
	 * @param clazz
	 *            The class whose instances should be retrieved
	 * @return Instances of the given type
	 */
	IExtendedIterator<?> getInstances(IReference clazz);
}

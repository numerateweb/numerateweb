package org.numerateweb.math.eval;

import java.util.Map;
import java.util.Optional;

import org.numerateweb.math.model.OMObject;

import net.enilink.commons.iterator.IExtendedIterator;
import net.enilink.komma.core.IReference;
import net.enilink.komma.core.URI;

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

	/**
	 * Return a new instance of with the given <code>uri</code> and type
	 * <code>clazz</code>, use the supplied args to construct/initialize the
	 * instance.
	 * 
	 * @param uri
	 *            The uri for the instance that should be created
	 * @param clazz
	 *            The class of the instance that should be created
	 * @param arg
	 *            A map of intialization property URIs and values
	 * @return The requested instance.
	 */
	Object createInstance(URI uri, IReference clazz, Map<URI, Object> args);
}

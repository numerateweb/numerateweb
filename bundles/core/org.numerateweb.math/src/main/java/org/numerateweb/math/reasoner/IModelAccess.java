package org.numerateweb.math.reasoner;

import java.util.Optional;

import org.numerateweb.math.model.OMObject;

import net.enilink.commons.iterator.IExtendedIterator;
import net.enilink.komma.core.IReference;

public interface IModelAccess {
	/**
	 * Return all instances of type <code>clazz</code> contained within the model.
	 * 
	 * @param clazz
	 *            The class whose instances should be retrieved
	 * @return Instances of the given type
	 */
	IExtendedIterator<?> getInstances(IReference clazz);

	/**
	 * Return an {@link OMObject} that represents the constraint on the given
	 * <code>property</code> of <code>subject</code>.
	 * 
	 * @param subject
	 *            The subject to whose property a constraint applies
	 * @param property
	 *            The property
	 * @return a math object that represents the constraint expression
	 */
	ResultSpec<OMObject> getExpressionSpec(Object subject, IReference property);

	/**
	 * Returns the value of the given <code>property</code> of the object
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
	IExtendedIterator<?> getPropertyValues(Object subject, IReference property, Optional<IReference> restriction);
}

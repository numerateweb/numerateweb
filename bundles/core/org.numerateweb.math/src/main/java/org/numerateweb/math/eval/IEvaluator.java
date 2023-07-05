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
		boolean isSingle();

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

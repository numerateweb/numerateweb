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
package org.numerateweb.math.model.factory;

import java.math.BigInteger;
import java.util.List;

import org.numerateweb.math.ns.INamespaces;

import net.enilink.komma.core.IReference;
import net.enilink.komma.core.URI;

/**
 * Factory interface for the creation of mathematical objects.
 * 
 * @param <T>
 *            Type of the resulting objects.
 */
public interface ValueFactory<T> {
	T createOMElement(T arg);

	T createOMS(URI symbol);

	T createOMV(String variableName);

	T createOMI(BigInteger value);

	T createOMB(String base64Binary);

	T createOMSTR(String value);

	T createOMF(double value);

	T createOMA(List<T> args);

	T createOMBIND(T binder, List<T> variables, T body);

	T createOME(T symbol, List<T> args);

	T createOMATTR(List<T> attributeValuePairs, T target);

	T createOMR(IReference reference);

	T createOMFOREIGN(String encoding, String content);

	T createRDFClass(IReference reference, INamespaces ns);
}
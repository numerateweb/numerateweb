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
package org.numerateweb.math.model;

import java.math.BigInteger;

import net.enilink.komma.core.IReference;
import net.enilink.komma.core.URI;

import org.numerateweb.math.ns.INamespaces;

/**
 * Builder interface for the creation of 'literal' mathematical objects.
 * 
 * @param <T>
 *            Mathematical object type of the resulting elements.
 */
public interface LiteralBuilder<T> {
	LiteralBuilder<T> id(URI id);
	
	T b(String base64Binary);

	T f(double value);

	T foreign(String encoding, String content);

	T i(BigInteger value);

	T rdfClass(IReference reference, INamespaces ns);

	T ref(IReference reference);

	T s(URI symbol);

	T str(String value);

	T var(String variableName);
	
}

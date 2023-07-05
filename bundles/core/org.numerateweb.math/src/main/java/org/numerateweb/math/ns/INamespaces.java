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
package org.numerateweb.math.ns;

import net.enilink.komma.core.URI;

public interface INamespaces {
	/**
	 * Gets the namespace that is associated with the specified prefix, if any.
	 * 
	 * @param prefix
	 *            A namespace prefix.
	 * @return The namespace name that is associated with the specified prefix,
	 *         or <tt>null</tt> if there is no such namespace.
	 */
	URI getNamespace(String prefix);
	
	/**
	 * Gets the prefix that is associated with the specified namespace uri, if
	 * any.
	 * 
	 * @param namespace
	 *            A namespace uri.
	 * @return The prefix that is associated with the specified namespace uri,
	 *         or <tt>null</tt> if there is no such prefix.
	 */
	String getPrefix(URI namespace);
	
	static INamespaces empty() {
		return new INamespaces() {
			@Override
			public String getPrefix(URI namespace) {
				return null;
			}
			
			@Override
			public URI getNamespace(String prefix) {
				return null;
			}
		};
	}
}

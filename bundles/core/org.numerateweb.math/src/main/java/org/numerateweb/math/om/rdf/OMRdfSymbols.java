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
package org.numerateweb.math.om.rdf;

import net.enilink.komma.core.URI;
import net.enilink.komma.core.URIs;

public interface OMRdfSymbols {
	public static final URI NAMESPACE = URIs
			.createURI("http://www.openmath.org/cd/rdf#");

	public static final URI PREFIXES = NAMESPACE.appendLocalPart("prefixes");
	public static final URI PREFIX = NAMESPACE.appendLocalPart("prefix");

	public static final URI RESOURCE = NAMESPACE.appendLocalPart("resource");
	public static final URI RESOURCESET = NAMESPACE
			.appendLocalPart("resourceset");
	public static final URI VALUE = NAMESPACE.appendLocalPart("value");
	public static final URI VALUESET = NAMESPACE.appendLocalPart("valueset");

	public static final URI LITERAL_TYPE = NAMESPACE
			.appendLocalPart("literal_type");
	public static final URI LITERAL_LANG = NAMESPACE
			.appendLocalPart("literal_lang");
}

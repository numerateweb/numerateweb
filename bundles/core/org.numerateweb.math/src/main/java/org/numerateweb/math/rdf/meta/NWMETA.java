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
package org.numerateweb.math.rdf.meta;

import net.enilink.komma.core.URIs;
import net.enilink.komma.core.URI;

public interface NWMETA {
	public static final String NAMESPACE = "http://numerateweb.org/vocab/math/meta#";
	public static final URI NAMESPACE_URI = URIs.createURI(NAMESPACE);

	public static final URI TYPE_APPLICATIONSYMBOL = NAMESPACE_URI
			.appendLocalPart("ApplicationSymbol");

	public static final URI TYPE_ATTRIBUTIONSYMBOL = NAMESPACE_URI
			.appendLocalPart("AttributionSymbol");

	public static final URI TYPE_BINDERSYMBOL = NAMESPACE_URI
			.appendLocalPart("BinderSymbol");

	public static final URI TYPE_CONSTANTSYMBOL = NAMESPACE_URI
			.appendLocalPart("ConstantSymbol");

	public static final URI TYPE_ERRORSYMBOL = NAMESPACE_URI
			.appendLocalPart("ErrorSymbol");

	public static final URI TYPE_LIBRARY = NAMESPACE_URI
			.appendLocalPart("Library");

	public static final URI TYPE_SEMANTICATTRIBUTIONSYMBOL = NAMESPACE_URI
			.appendLocalPart("SemanticAttributionSymbol");

	public static final URI PROPERTY_COMMENTEDPROPERTY = NAMESPACE_URI
			.appendLocalPart("commentedProperty");

	public static final URI PROPERTY_DESCRIPTION = NAMESPACE_URI
			.appendLocalPart("description");

	public static final URI PROPERTY_EXAMPLE = NAMESPACE_URI
			.appendLocalPart("example");

	public static final URI PROPERTY_FORMALPROPERTY = NAMESPACE_URI
			.appendLocalPart("formalProperty");

}

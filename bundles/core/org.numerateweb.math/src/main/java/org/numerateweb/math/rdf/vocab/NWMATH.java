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
package org.numerateweb.math.rdf.vocab;

import net.enilink.komma.core.URIs;
import net.enilink.komma.core.URI;

public interface NWMATH {
	String NAMESPACE = "http://numerateweb.org/vocab/math#";
	URI NAMESPACE_URI = URIs.createURI(NAMESPACE);

	URI TYPE_VARIABLE = NAMESPACE_URI.appendLocalPart("Variable");

	URI TYPE_ATTRIBUTIONPAIR = NAMESPACE_URI.appendLocalPart("AttributionPair");

	URI TYPE_OBJECT = NAMESPACE_URI.appendLocalPart("Object");

	URI TYPE_APPLICATION = NAMESPACE_URI.appendLocalPart("Application");

	URI TYPE_SYMBOL = NAMESPACE_URI.appendLocalPart("Symbol");

	URI TYPE_COMPOUND = NAMESPACE_URI.appendLocalPart("Compound");

	URI TYPE_ATTRIBUTION = NAMESPACE_URI.appendLocalPart("Attribution");

	URI TYPE_BINDING = NAMESPACE_URI.appendLocalPart("Binding");

	URI TYPE_LITERAL = NAMESPACE_URI.appendLocalPart("Literal");

	URI TYPE_ERROR = NAMESPACE_URI.appendLocalPart("Error");

	URI TYPE_FOREIGN = NAMESPACE_URI.appendLocalPart("Foreign");

	URI TYPE_REFERENCE = NAMESPACE_URI.appendLocalPart("Reference");

	URI TYPE_VARIABLELIST = NAMESPACE_URI.appendLocalPart("VariableList");

	URI TYPE_ATTRIBUTIONLIST = NAMESPACE_URI.appendLocalPart("AttributionList");

	URI TYPE_OBJECTLIST = NAMESPACE_URI.appendLocalPart("ObjectList");

	URI PROPERTY_SYMBOL = NAMESPACE_URI.appendLocalPart("symbol");

	URI PROPERTY_ARGUMENTS = NAMESPACE_URI.appendLocalPart("arguments");

	URI PROPERTY_ENCODING = NAMESPACE_URI.appendLocalPart("encoding");

	URI PROPERTY_TARGET = NAMESPACE_URI.appendLocalPart("target");

	URI PROPERTY_NAME = NAMESPACE_URI.appendLocalPart("name");

	URI PROPERTY_OPERATOR = NAMESPACE_URI.appendLocalPart("operator");

	URI PROPERTY_VARIABLES = NAMESPACE_URI.appendLocalPart("variables");

	URI PROPERTY_VALUE = NAMESPACE_URI.appendLocalPart("value");

	URI PROPERTY_BODY = NAMESPACE_URI.appendLocalPart("body");

	URI PROPERTY_BINDER = NAMESPACE_URI.appendLocalPart("binder");

	URI PROPERTY_ATTRIBUTEVALUE = NAMESPACE_URI.appendLocalPart("attributeValue");

	URI PROPERTY_ATTRIBUTEKEY = NAMESPACE_URI.appendLocalPart("attributeKey");

}

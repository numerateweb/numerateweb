package org.numerateweb.math.concepts;

import net.enilink.komma.core.URIs;
import net.enilink.komma.core.URI;

public interface NWMATH {
	public static final String NAMESPACE = "http://numerateweb.org/vocab/math#";
	public static final URI NAMESPACE_URI = URIs.createURI(NAMESPACE);

	public static final URI TYPE_VARIABLE = NAMESPACE_URI.appendLocalPart("Variable");

	public static final URI TYPE_ATTRIBUTIONPAIR = NAMESPACE_URI.appendLocalPart("AttributionPair");

	public static final URI TYPE_OBJECT = NAMESPACE_URI.appendLocalPart("Object");

	public static final URI TYPE_APPLICATION = NAMESPACE_URI.appendLocalPart("Application");

	public static final URI TYPE_SYMBOL = NAMESPACE_URI.appendLocalPart("Symbol");

	public static final URI TYPE_COMPOUND = NAMESPACE_URI.appendLocalPart("Compound");

	public static final URI TYPE_ATTRIBUTION = NAMESPACE_URI.appendLocalPart("Attribution");

	public static final URI TYPE_BINDING = NAMESPACE_URI.appendLocalPart("Binding");

	public static final URI TYPE_LITERAL = NAMESPACE_URI.appendLocalPart("Literal");

	public static final URI TYPE_ERROR = NAMESPACE_URI.appendLocalPart("Error");

	public static final URI TYPE_FOREIGN = NAMESPACE_URI.appendLocalPart("Foreign");

	public static final URI TYPE_VARIABLELIST = NAMESPACE_URI.appendLocalPart("VariableList");

	public static final URI TYPE_ATTRIBUTIONLIST = NAMESPACE_URI.appendLocalPart("AttributionList");

	public static final URI TYPE_OBJECTLIST = NAMESPACE_URI.appendLocalPart("ObjectList");

	public static final URI PROPERTY_SYMBOL = NAMESPACE_URI.appendLocalPart("symbol");

	public static final URI PROPERTY_ARGUMENTS = NAMESPACE_URI.appendLocalPart("arguments");

	public static final URI PROPERTY_TARGET = NAMESPACE_URI.appendLocalPart("target");
	
	public static final URI PROPERTY_NAME = NAMESPACE_URI.appendLocalPart("name");
	
	public static final URI PROPERTY_OPERATOR = NAMESPACE_URI.appendLocalPart("operator");

	public static final URI PROPERTY_VARIABLES = NAMESPACE_URI.appendLocalPart("variables");

	public static final URI PROPERTY_BODY = NAMESPACE_URI.appendLocalPart("body");

	public static final URI PROPERTY_BINDER = NAMESPACE_URI.appendLocalPart("binder");

	public static final URI PROPERTY_ATTRIBUTEVALUE = NAMESPACE_URI.appendLocalPart("attributeValue");

	public static final URI PROPERTY_ATTRIBUTEKEY = NAMESPACE_URI.appendLocalPart("attributeKey");

}

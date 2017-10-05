package org.numerateweb.math.rdf.rules;

import net.enilink.komma.core.URIs;
import net.enilink.komma.core.URI;

public interface NWRULES {
	public static final String NAMESPACE = "http://numerateweb.org/vocab/math/rules#";
	public static final URI NAMESPACE_URI = URIs.createURI(NAMESPACE);

	public static final URI TYPE_CONSTRAINT = NAMESPACE_URI.appendLocalPart("Constraint");

	public static final URI PROPERTY_EXPRESSION = NAMESPACE_URI.appendLocalPart("expression");
	
	public static final URI PROPERTY_ONPROPERTY = NAMESPACE_URI.appendLocalPart("onProperty");
	
	public static final URI PROPERTY_CONSTRAINT = NAMESPACE_URI.appendLocalPart("constraint");

}

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

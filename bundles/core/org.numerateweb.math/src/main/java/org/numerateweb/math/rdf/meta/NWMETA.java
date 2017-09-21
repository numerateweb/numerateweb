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

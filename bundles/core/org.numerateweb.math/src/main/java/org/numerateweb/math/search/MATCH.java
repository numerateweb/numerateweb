package org.numerateweb.math.search;

import static net.enilink.komma.core.URIs.createURI;
import net.enilink.komma.core.URI;

public abstract class MATCH {
	public static final String CD_NAME = "match";
	public static final URI CD_URI = createURI("http://www.openmath.org/cd/"
			+ CD_NAME);

	public static final URI NOT = CD_URI.appendLocalPart("not");
	public static final URI ANY = CD_URI.appendLocalPart("any");
	public static final URI ALL = CD_URI.appendLocalPart("all");
	public static final URI ROOT = CD_URI.appendLocalPart("root");
	public static final URI DESCENDANT = CD_URI.appendLocalPart("descendant");
	public static final URI SELF_OR_DESCENDANT = CD_URI
			.appendLocalPart("selfOrDescendant");

	public static final URI variable(String name) {
		return createURI("match:var:" + name);
	}
}
package org.numerateweb.math.search;

import static net.enilink.komma.core.URIs.createURI;
import net.enilink.komma.core.URI;

public abstract class PATTERNS {
	public static final String CD_NAME = "patterns";
	public static final URI CD_URI = createURI("http://www.openmath.org/cd/" + CD_NAME);

	public static final URI NONE_OF = CD_URI.appendLocalPart("none_of");
	public static final URI ANY_OF = CD_URI.appendLocalPart("any_of");
	public static final URI ALL_OF = CD_URI.appendLocalPart("all_of");
	public static final URI ARGUMENT = CD_URI.appendLocalPart("argument");
	public static final URI ROOT = CD_URI.appendLocalPart("root");
	public static final URI DESCENDANT = CD_URI.appendLocalPart("descendant");
	public static final URI SELF_OR_DESCENDANT = CD_URI.appendLocalPart("self_or_descendant");
	public static final URI ANY = CD_URI.appendLocalPart("any");
}
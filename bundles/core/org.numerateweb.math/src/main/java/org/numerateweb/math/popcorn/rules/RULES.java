package org.numerateweb.math.popcorn.rules;

import net.enilink.komma.core.URI;
import net.enilink.komma.core.URIs;

public interface RULES {
	final URI NS = URIs.createURI("http://www.openmath.org/cd/rules#");

	final URI CONSTRAINT = NS.appendLocalPart("Constraint");
}
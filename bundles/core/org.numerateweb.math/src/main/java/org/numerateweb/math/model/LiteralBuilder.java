package org.numerateweb.math.model;

import java.math.BigInteger;

import net.enilink.komma.core.IReference;
import net.enilink.komma.core.URI;

import org.numerateweb.math.ns.INamespaces;

/**
 * Builder interface for the creation of 'literal' mathematical objects.
 * 
 * @param <T>
 *            Mathematical object type of the resulting elements.
 */
public interface LiteralBuilder<T> {
	LiteralBuilder<T> id(URI id);
	
	T b(String base64Binary);

	T f(double value);

	T foreign(String encoding, String content);

	T i(BigInteger value);

	T rdfClass(IReference reference, INamespaces ns);

	T ref(IReference reference);

	T s(URI symbol);

	T str(String value);

	T var(String variableName);
	
}

package org.numerateweb.math.model.factory;

import java.math.BigInteger;
import java.util.List;

import org.numerateweb.math.ns.INamespaces;

import net.enilink.komma.core.IReference;
import net.enilink.komma.core.URI;

/**
 * Factory interface for the creation of mathematical objects.
 * 
 * @param <T>
 *            Type of the resulting objects.
 */
public interface ValueFactory<T> {
	T createOMElement(T arg);

	T createOMS(URI symbol);

	T createOMV(String variableName);

	T createOMI(BigInteger value);

	T createOMB(String base64Binary);

	T createOMSTR(String value);

	T createOMF(double value);

	T createOMA(List<T> args);

	T createOMBIND(T binder, List<T> variables, T body);

	T createOME(T symbol, List<T> args);

	T createOMATTR(List<T> attributeValuePairs, T target);

	T createOMR(IReference reference);

	T createOMFOREIGN(String encoding, String content);

	T createRDFClass(IReference reference, INamespaces ns);
}
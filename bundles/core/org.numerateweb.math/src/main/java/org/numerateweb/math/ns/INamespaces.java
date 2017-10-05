package org.numerateweb.math.ns;

import net.enilink.komma.core.URI;

public interface INamespaces {
	/**
	 * Gets the namespace that is associated with the specified prefix, if any.
	 * 
	 * @param prefix
	 *            A namespace prefix.
	 * @return The namespace name that is associated with the specified prefix,
	 *         or <tt>null</tt> if there is no such namespace.
	 */
	URI getNamespace(String prefix);
	
	/**
	 * Gets the prefix that is associated with the specified namespace uri, if
	 * any.
	 * 
	 * @param namespace
	 *            A namespace uri.
	 * @return The prefix that is associated with the specified namespace uri,
	 *         or <tt>null</tt> if there is no such prefix.
	 */
	String getPrefix(URI namespace);
	
	static INamespaces empty() {
		return new INamespaces() {
			@Override
			public String getPrefix(URI namespace) {
				return null;
			}
			
			@Override
			public URI getNamespace(String prefix) {
				return null;
			}
		};
	}
}

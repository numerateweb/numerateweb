package org.numerateweb.math.ns;

import net.enilink.komma.core.IEntityManager;
import net.enilink.komma.core.URI;

public class Namespaces implements INamespaces {
	protected IEntityManager em;

	public Namespaces(IEntityManager em) {
		this.em = em;
	}

	@Override
	public String getPrefix(URI namespace) {
		return em.getPrefix(namespace);
	}

	@Override
	public URI getNamespace(String prefix) {
		return em.getNamespace(prefix == null ? "" : prefix);
	}
}

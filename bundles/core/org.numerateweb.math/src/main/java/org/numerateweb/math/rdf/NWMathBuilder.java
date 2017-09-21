package org.numerateweb.math.rdf;

import org.numerateweb.math.ns.INamespaces;

import net.enilink.komma.core.IEntityManager;
import net.enilink.komma.core.IReference;

public class NWMathBuilder extends NWMathBuilderBase<IReference> {
	public NWMathBuilder(IEntityManager em, INamespaces ns) {
		super(em, ns);
	}

	public NWMathBuilder(Context context) {
		super(context);
	}

	@Override
	public IReference build(IReference obj) {
		return obj;
	}
}

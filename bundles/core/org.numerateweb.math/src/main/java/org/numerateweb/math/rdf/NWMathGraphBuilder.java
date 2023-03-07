package org.numerateweb.math.rdf;

import net.enilink.komma.core.IGraph;
import net.enilink.komma.core.IReference;
import org.numerateweb.math.ns.INamespaces;

public class NWMathGraphBuilder extends NWMathGraphBuilderBase<IReference> {
	public NWMathGraphBuilder(IGraph graph, INamespaces ns) {
		super(graph, ns);
	}

	public NWMathGraphBuilder(Context context) {
		super(context);
	}

	@Override
	public IReference build(IReference obj) {
		return obj;
	}
}
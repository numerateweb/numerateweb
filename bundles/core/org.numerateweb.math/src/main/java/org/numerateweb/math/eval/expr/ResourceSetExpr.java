package org.numerateweb.math.eval.expr;

import java.util.Optional;

import org.numerateweb.math.eval.Expressions;

import net.enilink.komma.core.IReference;

public class ResourceSetExpr implements Expr, EvalWithRestriction {
	/**
	 * The RDF class.
	 */
	protected final IReference rdfClass;

	protected Optional<IReference> restriction = Optional.empty();

	public ResourceSetExpr(IReference rdfClass) {
		this.rdfClass = rdfClass;
	}

	@Override
	public Object eval() {
		return evalWithRestriction(Optional.empty());
	}

	@Override
	public Object evalWithRestriction(Optional<IReference> restriction) {
		return Expressions.getEvaluator().getInstances(rdfClass);
	}
}
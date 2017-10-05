package org.numerateweb.math.eval.expr;

import java.util.Optional;

import org.numerateweb.math.eval.Expressions;

import net.enilink.komma.core.IReference;

public class ValueSetExpr implements Expr, EvalWithRestriction {
	/**
	 * The RDF property.
	 */
	protected final IReference property;

	/**
	 * The RDF subject for the given property.
	 */
	protected final Optional<Expr> subjectExpr;

	public ValueSetExpr(IReference property, Optional<Expr> subjectExpr) {
		this.property = property;
		this.subjectExpr = subjectExpr;
	}

	protected Object query(IReference subject, IReference property, Optional<IReference> restriction) {
		return Expressions.queryValues(subject, property, restriction);
	}

	@Override
	public Object eval() {
		return evalWithRestriction(Optional.empty());
	}

	@Override
	public Object evalWithRestriction(Optional<IReference> restriction) {
		IReference subject = subjectExpr.map(e -> Expressions.toReference((IReference) e.eval()))
				.orElse(Expressions.getResource());
		return query(subject, property, restriction);
	}
}
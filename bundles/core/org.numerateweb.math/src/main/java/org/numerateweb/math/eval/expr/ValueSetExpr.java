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

	protected Object query(Object subject, IReference property, Optional<IReference> restriction) {
		// get values and map to constant expressions
		return Expressions.getEvaluator().evaluate(subject, property, restriction);
	}

	@Override
	public Object eval() {
		return evalWithRestriction(Optional.empty());
	}

	@Override
	public Object evalWithRestriction(Optional<IReference> restriction) {
		Object subject = subjectExpr.map(e -> e.eval()).orElse(Expressions.getSubject());
		return query(subject, property, restriction);
	}
}
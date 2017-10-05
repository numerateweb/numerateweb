package org.numerateweb.math.eval;

import org.numerateweb.math.eval.expr.Expr;

/**
 * Builder for simple expressions that can be evaluated numerically.
 */
public class ExprBuilder extends ExprBuilderBase<Expr> {
	@Override
	protected Expr build(Expr expr) {
		return expr;
	}
}
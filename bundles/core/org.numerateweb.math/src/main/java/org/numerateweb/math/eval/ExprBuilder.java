package org.numerateweb.math.eval;

import org.numerateweb.math.eval.expr.Expr;

public class ExprBuilder extends ExprBuilderBase<Expr> {
	@Override
	protected Expr build(Expr expr) {
		return expr;
	}
}
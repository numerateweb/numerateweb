package org.numerateweb.math.eval.expr;

import java.util.List;

import org.numerateweb.math.model.OMObject;

/**
 * Represents an if-then-else expression.
 */
public class IfElseExpr implements Expr {
	/**
	 * Expressions for the three blocks that are evaluated to compute the condition
	 * and branch results.
	 */
	protected final Expr[] args;

	public IfElseExpr(List<Expr> args) {
		this(args.toArray(new Expr[args.size()]));
	}

	public IfElseExpr(Expr... args) {
		if (args.length < 2) {
			throw new IllegalArgumentException("Expected at least two arguments for IF expression.");
		}
		this.args = args;
	}

	@Override
	public Object eval() {
		Object cond = null;
		try {
			cond = args[0].eval();
		} catch (Exception e) {
			System.err.println("Evaluation of IF condition failed: " + e.getMessage());
			cond = false;
		}
		if (Boolean.TRUE.equals(cond) || OMObject.LOGIC1_TRUE.equals(cond)) {
			return args[1].eval();
		}
		if (Boolean.FALSE.equals(cond) || OMObject.LOGIC1_FALSE.equals(cond)) {
			// TODO check if null is OK for applying a missing else block
			return args.length > 2 ? args[2].eval() : null;
		}
		throw new IllegalArgumentException("Illegal condition for IF expression: " + args[0]);
	}
}

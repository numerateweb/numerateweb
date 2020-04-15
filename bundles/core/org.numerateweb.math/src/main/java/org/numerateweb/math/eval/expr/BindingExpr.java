package org.numerateweb.math.eval.expr;

import java.util.List;

/**
 * Represents an OpenMath binding like fns1.lambda.
 */
public class BindingExpr implements Expr {
	/**
	 * The bound variables.
	 */
	public List<Expr> variables;
	/**
	 * The binder symbol (e.g. fns1.lambda).
	 */
	public Expr binder;
	/**
	 * The body of the binding.
	 */
	public Expr body;

	public BindingExpr(Expr binder, List<Expr> variables, Expr body) {
		this.binder = binder;
		this.variables = variables;
		this.body = body;
	}

	@Override
	public String toString() {
		return variables.toString() + "->" + body.toString();
	}

	@Override
	public Object eval() {
		// the value of this expression is the binding expression itself
		return this;
	}
}
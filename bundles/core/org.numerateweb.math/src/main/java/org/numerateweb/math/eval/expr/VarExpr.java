package org.numerateweb.math.eval.expr;

import org.numerateweb.math.eval.Expressions;

/**
 * Represents the value of a named variable.
 */
public class VarExpr implements Expr {
	/**
	 * The variable's name.
	 */
	protected final String name;

	public VarExpr(String name) {
		this.name = name;
	}

	public String name() {
		return name;
	}

	@Override
	public Object eval() {
		return Expressions.getVar(name);
	}
}
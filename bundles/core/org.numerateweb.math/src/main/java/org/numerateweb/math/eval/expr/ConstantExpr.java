package org.numerateweb.math.eval.expr;

/**
 * Represents a constant value.
 */
public class ConstantExpr implements Expr {
	/**
	 * The constant value.
	 */
	protected final Object value;

	public ConstantExpr(Object value) {
		this.value = value;
	}

	@Override
	public Object eval() {
		return value;
	}
}
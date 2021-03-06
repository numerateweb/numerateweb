package org.numerateweb.math.eval.expr;

import java.util.List;
import java.util.function.Function;

/**
 * Represents a call of a built-in function on one or more arguments.
 */
public class BuiltinCallExpr implements Expr {
	/**
	 * The function that is used to compute a value.
	 */
	protected final Function<Object, Object> f;
	/**
	 * Expressions for the function's arguments that are evaluated to compute
	 * the actual arguments of the function.
	 */
	protected final Expr[] args;

	public BuiltinCallExpr(Function<Object, Object> f, List<Expr> args) {
		this(f, args.toArray(new Expr[args.size()]));
	}

	public BuiltinCallExpr(Function<Object, Object> f, Expr... args) {
		this.f = f;
		this.args = args;
	}

	@Override
	public Object eval() {
		Object[] values = new Object[args.length];
		for (int i = 0; i < args.length; i++) {
			Expr arg = args[i];
			values[i] = arg.eval();
		}
		try {
			return f.apply(values);
		} catch (NumberFormatException e) {
			return Double.NaN;
		}
	}
}
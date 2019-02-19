package org.numerateweb.math.eval.expr;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.numerateweb.math.eval.Expressions;
import org.numerateweb.math.eval.Helpers;

/**
 * Represents a map operator over a collection of elements.
 */
public class MapOperatorExpr implements Expr {
	/**
	 * The binding expression (only fns1.lambda is currently supported).
	 */
	protected final BindingExpr binding;
	/**
	 * The name of the first variable in the binding.
	 */
	protected final Optional<String> varName;
	/**
	 * The interval or set expression.
	 */
	protected final Expr interval;

	public MapOperatorExpr(List<Expr> args) {
		this(args.toArray(new Expr[args.size()]));
	}

	public MapOperatorExpr(Expr... args) {
		if (args[0] instanceof BindingExpr) {
			BindingExpr binding = (BindingExpr) args[0];
			if (!(binding.binder instanceof SymbolExpr
					&& Expressions.LAMBDA_SYMBOL.equals(((SymbolExpr) binding.binder).uri()))) {
				throw new IllegalArgumentException("Only fns1#lambda is allowed as binder.");
			}
			this.interval = args[1];
			this.binding = binding;
			this.varName = binding.variables.stream().findFirst().map(e -> ((VarExpr) e).name());
		} else {
			throw new IllegalArgumentException("Expected binding");
		}
	}

	@Override
	public Object eval() {
		Supplier<Object> func = () -> {
			// simply evaluate the body here
			return binding.body.eval();
		};

		return Helpers.valueToStream(interval.eval()).map(v -> {
			// set var before evaluation or none
			return varName.map(name -> Expressions.withVar(name, v, func)).orElseGet(func);
		}).collect(Collectors.toList());
	}
}

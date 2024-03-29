/*
 * Copyright (c) 2023 Numerate Web contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.numerateweb.math.eval.expr;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import org.numerateweb.math.eval.Expressions;

import net.enilink.commons.util.Pair;

/**
 * Represents an application of a lambda function like $func(2, 3) or ($x -> 2 * $x)(2, 3)
 */
public class CallExpr implements Expr {
	/**
	 * The operator
	 */
	protected final Expr operator;

	/**
	 * The arguments
	 */
	protected final List<Expr> args;

	public CallExpr(Expr operator, List<Expr> args) {
		this.operator = operator;
		this.args = args;
	}

	@Override
	public Object eval() {
		final Object bindingCandidate = operator.eval();

		if (!(bindingCandidate instanceof BindingExpr && ((BindingExpr) bindingCandidate).binder instanceof SymbolExpr
				&& Expressions.LAMBDA_SYMBOL.equals(((SymbolExpr) ((BindingExpr) bindingCandidate).binder).uri()))) {
			throw new IllegalArgumentException("Only fns1#lambda is allowed as binder.");
		}

		final BindingExpr binding = (BindingExpr) bindingCandidate;

		List<Expr> vars = binding.variables;
		if (vars.size() > args.size()) {
			throw new IllegalArgumentException("More arguments required.");
		}

		List<Pair<String, Object>> bindings = new ArrayList<>(vars.size());
		for (int i = 0; i < vars.size(); i++) {
			bindings.add(new Pair<>(((VarExpr) vars.get(i)).name(), args.get(i).eval()));
		}

		Supplier<Object> func = () -> {
			// simply evaluate the body here
			return binding.body.eval();
		};

		return Expressions.withVars(bindings, func);
	}
}

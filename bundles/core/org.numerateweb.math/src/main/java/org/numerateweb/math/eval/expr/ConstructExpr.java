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

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.numerateweb.math.eval.Expressions;
import org.numerateweb.math.eval.Helpers;

import net.enilink.komma.core.IReference;
import net.enilink.komma.core.URI;
import net.enilink.komma.core.URIs;

/**
 * Represents a construct expression to create new model elements from OpenMath
 * constraints.
 * <p>
 * Usage example (2 ways to create a list of 3 new Item instances i=0, 1, 2):
 * <pre>
 *  :list1 := ctor1:generate(0..2, $i->("item" + $i){Class -> Item, i -> $i})
 *  :list2 := map($i->ctor1:new(("item" + $i){Class -> Item, i -> $i}), 0..2)
 * </pre>
 */
public class ConstructExpr implements Expr {
	/**
	 * The interval or set expression.
	 */
	protected final Expr interval;
	/**
	 * The constructor expression (URI generator attributed with arguments), either
	 * as given as sole argument or inside a lambda-expression.
	 */
	protected final AttributedExpr ctor;
	/**
	 * The name of the first variable in the binding.
	 */
	protected final Optional<String> varName;

	public ConstructExpr(List<Expr> args) {
		this(args.toArray(new Expr[args.size()]));
	}

	public ConstructExpr(Expr... args) {
		if (args.length < 1) {
			throw new IllegalArgumentException("Expected interval and binder or attributed expression.");
		}
		if (args.length == 1) {
			this.interval = null;
			if (!(args[0] instanceof AttributedExpr)) {
				throw new IllegalArgumentException("Expected an attributed expression.");
			}
			this.ctor = (AttributedExpr) args[0];
			this.varName = Optional.empty();
		} else if (args.length == 2 && args[1] instanceof BindingExpr) {
			BindingExpr binding = (BindingExpr) args[1];
			if (!(binding.binder instanceof SymbolExpr
					&& Expressions.LAMBDA_SYMBOL.equals(((SymbolExpr) binding.binder).uri()))) {
				throw new IllegalArgumentException("Only fns1#lambda is allowed as binder.");
			}
			this.interval = args[0];
			if (!(binding.body instanceof AttributedExpr)) {
				throw new IllegalArgumentException("Expected an attributed expression.");
			}
			this.ctor = (AttributedExpr) binding.body;
			this.varName = binding.variables.stream().findFirst().map(e -> ((VarExpr) e).name());
		} else {
			throw new IllegalArgumentException("Expected binding.");
		}
	}

	@Override
	public Object eval() {
		Supplier<Object> func = () -> {
			// eval the target expression for the new entity's URI
			URI uri = URIs.createURI(ctor.target.eval().toString());
			IReference clazz = ((SymbolExpr) ctor.attributes.get(URIs.createURI("symbol:Class"))).uri;
			Map<URI, Object> args = ctor.attributes.entrySet().stream() //
					.filter(e -> !"symbol:Class".equals(e.getKey().toString()))
					.collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue().eval()));
			return Expressions.getEvaluator().createInstance(uri, clazz, args);
		};
		if (null == interval) {
			return func.get();
		}

		return Helpers.valueToStream(interval.eval()).map(v -> {
			// set var before evaluation or none
			return varName.map(name -> Expressions.withVar(name, v, func)).orElseGet(func);
		}).collect(Collectors.toList());
	}
}

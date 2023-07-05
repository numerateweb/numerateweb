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
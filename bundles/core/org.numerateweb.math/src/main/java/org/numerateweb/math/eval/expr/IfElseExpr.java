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

import org.numerateweb.math.model.OMObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents an if-then-else expression.
 */
public class IfElseExpr implements Expr {
	private static final Logger log = LoggerFactory.getLogger(IfElseExpr.class);

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
			log.debug("Evaluation of IF condition failed", e);
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

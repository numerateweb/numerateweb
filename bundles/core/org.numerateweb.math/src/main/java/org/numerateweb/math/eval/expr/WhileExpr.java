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

/**
 * Represents a while expression.
 */
public class WhileExpr implements Expr {
	/**
	 * The while condition
	 */
	protected final Expr condition;
	/**
	 * The while body
	 */
	protected final Expr body;

	protected static final int MAX_ITERATIONS = 10000;

	public WhileExpr(List<Expr> args) {
		if (args.size() != 2) {
			throw new IllegalArgumentException("Expected at least two arguments for IF expression.");
		}
		this.condition = args.get(0);
		this.body = args.get(1);
	}

	@Override
	public Object eval() {
		int i = 0;
		boolean condBoolean;
		do {
			Object condValue = condition.eval();
			condBoolean = Boolean.TRUE.equals(condValue) || OMObject.LOGIC1_TRUE.equals(condValue);
			if (condBoolean) {
				body.eval();
			}
		} while (condBoolean && ++i < MAX_ITERATIONS);
		
		// TODO what is a good return value here
		return Double.NaN;
	}
}

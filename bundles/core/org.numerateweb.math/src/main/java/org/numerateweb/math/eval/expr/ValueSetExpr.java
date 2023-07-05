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

import java.util.Optional;

import org.numerateweb.math.eval.Expressions;

import net.enilink.komma.core.IReference;

public class ValueSetExpr implements Expr, EvalWithRestriction {
	/**
	 * The RDF property.
	 */
	protected final IReference property;

	/**
	 * The RDF subject for the given property.
	 */
	protected final Optional<Expr> subjectExpr;

	public ValueSetExpr(IReference property, Optional<Expr> subjectExpr) {
		this.property = property;
		this.subjectExpr = subjectExpr;
	}

	protected Object query(Object subject, IReference property, Optional<IReference> restriction) {
		// get values and map to constant expressions
		return Expressions.getEvaluator().evaluate(subject, property, restriction);
	}

	@Override
	public Object eval() {
		return evalWithRestriction(Optional.empty());
	}

	@Override
	public Object evalWithRestriction(Optional<IReference> restriction) {
		Object subject = subjectExpr.map(e -> e.eval()).orElse(Expressions.getSubject());
		return query(subject, property, restriction);
	}
}
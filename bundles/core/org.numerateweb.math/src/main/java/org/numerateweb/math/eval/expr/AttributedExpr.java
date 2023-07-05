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

import java.util.Map;

import net.enilink.komma.core.URI;

/**
 * Represents an attributed OpenMath expression.
 */
public class AttributedExpr implements Expr {
	/**
	 * The target expression.
	 */
	protected final Expr target;
	/**
	 * The attribute map.
	 */
	protected final Map<URI, Expr> attributes;

	public AttributedExpr(Expr target, Map<URI, Expr> attributes) {
		this.target = target;
		this.attributes = attributes;
	}

	@Override
	public String toString() {
		return target.toString() + attributes.toString();
	}

	public Expr target() {
		return target;
	}

	@Override
	public Object eval() {
		throw new UnsupportedOperationException();
	}
}

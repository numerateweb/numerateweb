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
package org.numerateweb.math.popcorn;

import net.enilink.komma.core.URI;

/**
 * Represents a Popcorn expression with associated priority information.
 */
public class PopcornExpr {
	public final boolean isInfixSymbol;
	public final int priority;
	public final URI symbol;
	public final String text;

	public PopcornExpr(String text) {
		this(text, 0);
	}

	public PopcornExpr(String text, int priority) {
		this(null, text, priority, false);
	}

	public PopcornExpr(URI symbol, String text, int priority,
			boolean isInfixSymbol) {
		this.symbol = symbol;
		this.text = text;
		this.priority = priority;
		this.isInfixSymbol = isInfixSymbol;
	}

	public PopcornExpr priority(int priority) {
		return new PopcornExpr(symbol, text, priority, isInfixSymbol);
	}
	
	public PopcornExpr symbol(URI symbol) {
		return new PopcornExpr(symbol, text, priority, isInfixSymbol);
	}
	
	public PopcornExpr text(String text) {
		return new PopcornExpr(symbol, text, priority, isInfixSymbol);
	}

	@Override
	public String toString() {
		return text;
	}
}

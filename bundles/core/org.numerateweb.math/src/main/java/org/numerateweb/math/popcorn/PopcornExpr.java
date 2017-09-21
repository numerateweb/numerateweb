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

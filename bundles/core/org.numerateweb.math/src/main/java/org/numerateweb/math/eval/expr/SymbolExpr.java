package org.numerateweb.math.eval.expr;

import net.enilink.komma.core.URI;

/**
 * Represents an OpenMath symbol.
 */
public class SymbolExpr implements Expr {
	/**
	 * The symbol's URI.
	 */
	protected final URI uri;

	public SymbolExpr(URI uri) {
		this.uri = uri;
	}

	@Override
	public String toString() {
		return uri.toString();
	}

	public URI uri() {
		return uri;
	}

	@Override
	public Object eval() {
		throw new UnsupportedOperationException("Unable to evaluate symbol '" + uri + "'");
	}
}
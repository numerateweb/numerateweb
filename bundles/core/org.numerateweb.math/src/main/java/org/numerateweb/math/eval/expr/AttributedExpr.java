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

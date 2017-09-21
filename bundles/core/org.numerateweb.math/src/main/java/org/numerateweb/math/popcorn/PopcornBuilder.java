package org.numerateweb.math.popcorn;

import org.numerateweb.math.ns.INamespaces;

public class PopcornBuilder extends PopcornBuilderBase<PopcornExpr> {
	public PopcornBuilder(INamespaces ns) {
		super(ns);
	}

	@Override
	protected PopcornExpr build(PopcornExpr expr) {
		return expr;
	}
}

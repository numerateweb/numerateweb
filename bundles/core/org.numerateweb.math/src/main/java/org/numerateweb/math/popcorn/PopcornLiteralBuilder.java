package org.numerateweb.math.popcorn;

import java.math.BigInteger;

import org.numerateweb.math.model.BuilderUtils;
import org.numerateweb.math.model.LiteralBuilder;
import org.numerateweb.math.ns.INamespaces;

import net.enilink.komma.core.IReference;
import net.enilink.komma.core.Literals;
import net.enilink.komma.core.URI;

public class PopcornLiteralBuilder implements LiteralBuilder<PopcornExpr> {
	protected INamespaces ns;

	public PopcornLiteralBuilder(INamespaces ns) {
		this.ns = ns;
	}

	@Override
	public PopcornExpr b(String base64Binary) {
		return new PopcornExpr("\\%" + base64Binary + "\\%");
	}

	@Override
	public PopcornExpr f(double value) {
		return new PopcornExpr(String.valueOf(value));
	}

	@Override
	public PopcornExpr foreign(String encoding, String content) {
		return new PopcornExpr("`" + encoding + content + "`");
	}

	@Override
	public PopcornExpr i(BigInteger value) {
		return new PopcornExpr(String.valueOf(value));
	}

	@Override
	public LiteralBuilder<PopcornExpr> id(URI id) {
		// ignore ids
		return null;
	}

	@Override
	public PopcornExpr rdfClass(IReference reference, INamespaces ns) {
		return str(BuilderUtils.classAsString(reference, ns));
	}

	@Override
	public PopcornExpr ref(IReference reference) {
		return new PopcornExpr(BuilderUtils.toPNameOrUriString(reference, ns));

		// TODO correctly handle references to RDF resources and other
		// mathematical objects
		// return new PopcornExpr("#"
		// + BuilderUtils.toPNameOrUriString(reference, ns));
	}

	@Override
	public PopcornExpr s(URI symbol) {
		return PopcornSymbols.toPopcorn(ns, symbol);
	}

	@Override
	public PopcornExpr str(String value) {
		if (value != null) {
			value = Literals.escapeTurtle(value);
		}
		return new PopcornExpr("\"" + value + "\"");
	}

	@Override
	public PopcornExpr var(String variableName) {
		return new PopcornExpr("$" + variableName);
	}

}

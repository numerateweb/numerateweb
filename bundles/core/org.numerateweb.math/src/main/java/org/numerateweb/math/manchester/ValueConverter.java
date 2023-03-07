package org.numerateweb.math.manchester;

import java.util.HashMap;
import java.util.Map;

import net.enilink.komma.core.*;
import net.enilink.komma.literals.LiteralConverter;
import net.enilink.vocab.xmlschema.XMLSCHEMA;
import org.numerateweb.math.ns.INamespaces;

import net.enilink.komma.parser.sparql.tree.BNode;
import net.enilink.komma.parser.sparql.tree.BooleanLiteral;
import net.enilink.komma.parser.sparql.tree.DoubleLiteral;
import net.enilink.komma.parser.sparql.tree.GenericLiteral;
import net.enilink.komma.parser.sparql.tree.IntegerLiteral;
import net.enilink.komma.parser.sparql.tree.IriRef;
import net.enilink.komma.parser.sparql.tree.Literal;
import net.enilink.komma.parser.sparql.tree.QName;
import net.enilink.komma.parser.sparql.tree.visitor.TreeWalker;

/**
 * Convert a value into the representation that is used by KOMMA.
 */
public class ValueConverter {
	private Map<BNode, IReference> bNodes = new HashMap<BNode, IReference>();
	private INamespaces ns;

	public ValueConverter(INamespaces ns) {
		this.ns = ns;
	}

	protected URI toURI(Object value) {
		if (value instanceof IriRef) {
			return URIs.createURI(((IriRef) value).getIri());
		} else if (value instanceof QName) {
			String prefix = ((QName) value).getPrefix();
			String localPart = ((QName) value).getLocalPart();
			URI ns;
			if (prefix == null || prefix.trim().length() == 0) {
				prefix = "";
			}
			ns = this.ns.getNamespace(prefix);
			if (ns != null) {
				return ns.appendLocalPart(localPart);
			}
			throw new IllegalArgumentException("Unknown prefix \"" + prefix
					+ "\"");
		} else if (value != null) {
			return ns.getNamespace("").appendLocalPart(value.toString());
		}
		return null;
	}

	public IValue toValue(Object value) {
		if (value instanceof BNode) {
			IReference reference = bNodes.get(value);
			if (reference == null) {
				bNodes.put((BNode) value, reference = new BlankNode());
			}
			return reference;
		} else if (value instanceof IriRef || value instanceof QName) {
			return toURI(value);
		} else if (value instanceof Literal) {
			final IValue[] result = new IValue[1];
			((Literal) value).accept(new TreeWalker<Void>() {
				@Override
				public Boolean integerLiteral(IntegerLiteral numericLiteral,
				                              Void data) {
					result[0] = new net.enilink.komma.core.Literal(Integer.toString(numericLiteral.getValue()),
							XMLSCHEMA.TYPE_INTEGER);
					return false;
				}

				@Override
				public Boolean booleanLiteral(BooleanLiteral booleanLiteral,
				                              Void data) {
					result[0] = new net.enilink.komma.core.Literal(Boolean.toString(booleanLiteral.getValue()),
							XMLSCHEMA.TYPE_BOOLEAN);
					return false;
				}

				@Override
				public Boolean doubleLiteral(DoubleLiteral doubleLiteral,
				                             Void data) {
					result[0] = new net.enilink.komma.core.Literal(Double.toString(doubleLiteral.getValue()),
							XMLSCHEMA.TYPE_DECIMAL);
					return false;
				}

				@Override
				public Boolean genericLiteral(GenericLiteral genericLiteral,
				                              Void data) {
					String lang = genericLiteral.getLanguage();
					result[0] = lang == null ? new net.enilink.komma.core.Literal(genericLiteral.getLabel(),
							(URI) toValue(genericLiteral.getDatatype())) :
							new net.enilink.komma.core.Literal(genericLiteral.getLabel(), lang);
					return false;
				}
			}, null);
			return result[0];
		}
		return (IReference) value;
	}
}

package org.numerateweb.math.manchester;

import java.util.HashMap;
import java.util.Map;

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
import net.enilink.komma.core.IEntityManager;
import net.enilink.komma.core.IReference;
import net.enilink.komma.core.IValue;
import net.enilink.komma.core.URI;
import net.enilink.komma.core.URIs;

/**
 * Convert a value into the representation that is used by KOMMA.
 * 
 */
public class ValueConverter {
	private IEntityManager em;
	private Map<BNode, IReference> bNodes = new HashMap<BNode, IReference>();
	private INamespaces ns;

	public ValueConverter(IEntityManager manager, INamespaces ns) {
		this.em = manager;
		this.ns = ns;
	}

	public IEntityManager getEntityManager() {
		return em;
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
				bNodes.put((BNode) value, reference = em.create());
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
					result[0] = em.toValue(numericLiteral.getValue());
					return false;
				}

				@Override
				public Boolean booleanLiteral(BooleanLiteral booleanLiteral,
						Void data) {
					result[0] = em.toValue(booleanLiteral.getValue());
					return false;
				}

				@Override
				public Boolean doubleLiteral(DoubleLiteral doubleLiteral,
						Void data) {
					result[0] = em.toValue(doubleLiteral.getValue());
					return false;
				}

				@Override
				public Boolean genericLiteral(GenericLiteral genericLiteral,
						Void data) {
					result[0] = em.createLiteral(genericLiteral.getLabel(),
							(URI) toValue(genericLiteral.getDatatype()),
							genericLiteral.getLanguage());
					return false;
				}
			}, null);
			return (IValue) result[0];
		}
		return (IReference) value;
	}
}

package org.numerateweb.math.popcorn.rules;

import static org.numerateweb.math.model.OMObject.OMA;
import static org.numerateweb.math.model.OMObject.OMR;
import static org.numerateweb.math.model.OMObject.OMS;

import java.util.ArrayList;
import java.util.List;

import org.numerateweb.math.model.OMObject;
import org.numerateweb.math.ns.INamespaces;
import org.numerateweb.math.popcorn.PopcornParser;
import org.parboiled.Parboiled;
import org.parboiled.Rule;
import org.parboiled.support.Var;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import net.enilink.komma.core.URI;
import net.enilink.komma.core.URIs;
import net.enilink.komma.parser.BaseRdfParser;
import net.enilink.komma.parser.sparql.tree.IriRef;
import net.enilink.komma.parser.sparql.tree.QName;

public class MathRulesParser extends BaseRdfParser {
	static class RulesNamespaces implements INamespaces {
		final INamespaces base;
		BiMap<String, URI> mappings = HashBiMap.create();

		public RulesNamespaces(INamespaces base) {
			this.base = base;
		}

		public void set(String prefix, URI namespace) {
			mappings.put(prefix, namespace);
		}

		@Override
		public URI getNamespace(String prefix) {
			return mappings.get(prefix);
		}

		@Override
		public String getPrefix(URI namespace) {
			return mappings.inverse().get(namespace);
		}
	}

	public final PopcornParser popcornParser;
	public final RulesNamespaces ns;

	public MathRulesParser() {
		this(INamespaces.empty());
	}

	public MathRulesParser(INamespaces ns) {
		this.ns = new RulesNamespaces(ns);
		this.popcornParser = Parboiled.createParser(PopcornParser.class, this.ns);
	}

	/**
	 * Updates the prefix mappings.
	 * 
	 * @param prefix
	 *            A prefix
	 * @param namespace
	 *            A corresponding namespace URI
	 * @return always true to continue parsing in any case
	 */
	public boolean setNamespace(String prefix, IriRef namespace) {
		this.ns.set(prefix, URIs.createURI(namespace.getIri()));
		return true;
	}

	public Rule PrefixDeclaration() {
		return sequence("Prefix:", PNAME_NS(), WS(), IRI_REF(), setNamespace((String) pop(1), (IriRef) pop()), WS());
	}

	// allow local names without colons
	public Rule IriRef() {
		return sequence(firstOf(IRI_REF(), PrefixedName(), //
				sequence(PN_LOCAL(), push(new QName("", (String) pop())))), WS());
	}

	public URI toURI(Object value) {
		if (value instanceof URI) {
			return (URI) value;
		} else if (value instanceof QName) {
			String prefix = ((QName) value).getPrefix();
			String localPart = ((QName) value).getLocalPart();

			URI namespace = ns != null ? ns.getNamespace(prefix) : null;
			if (namespace != null) {
				return namespace.appendLocalPart(localPart);
			} else {
				// fallback if namespace was not found
				return URIs.createURI(prefix + ":" + localPart);
			}
		} else {
			return URIs.createURI(value.toString());
		}
	}

	public Rule Constraint(Var<URI> classIri) {
		return sequence(IriRef(), ":=", popcornParser.Expr(), //
				// convert constraint to OpenMath object
				push(OMA(OMS(RULES.CONSTRAINT), OMR(classIri.get()), OMR(toURI(pop(1))), (OMObject) pop())) //
		);
	}

	public boolean pushConstraints() {
		List<OMObject> args = new ArrayList<>();
		args.add(OMS("http://www.openmath.org/cd/set1#set"));
		args.addAll(popList(OMObject.class));
		return push(OMA(args));
	}

	public Rule Constraints(Var<URI> classIri) {
		return sequence(Constraint(classIri), zeroOrMore(",", Constraint(classIri)));
	}

	public Rule ClassFrame() {
		Var<URI> classIri = new Var<>();
		return sequence("Class:", IriRef(), classIri.set(toURI(pop())),
				zeroOrMore(sequence("Constraints:", Constraints(classIri))));
	}

	public Rule Document() {
		return sequence(push(LIST_BEGIN), zeroOrMore(firstOf(PrefixDeclaration(), ClassFrame())), EOI,
				pushConstraints());
	}

	// for testing purposes
	public Rule Constraint() {
		Var<URI> classIri = new Var<>(URIs.createURI("example:Class"));
		return Constraint(classIri);
	}

	public Rule Constraints() {
		Var<URI> classIri = new Var<>(URIs.createURI("example:Class"));
		return Constraints(classIri);
	}
}
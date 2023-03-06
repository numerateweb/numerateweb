package org.numerateweb.math.rdf;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import net.enilink.komma.core.ILiteral;
import net.enilink.komma.core.IReference;
import net.enilink.komma.core.URI;
import net.enilink.komma.core.URIs;
import net.enilink.vocab.xmlschema.XMLSCHEMA;

import org.numerateweb.math.model.Builder;
import org.numerateweb.math.model.Builder.BindingBuilder;
import org.numerateweb.math.model.Builder.SeqBuilder;
import org.numerateweb.math.model.Builder.VariablesBuilder;
import org.numerateweb.math.ns.INamespaces;
import org.numerateweb.math.om.rdf.OMRdfSymbols;
import org.numerateweb.math.rdf.vocab.Application;
import org.numerateweb.math.rdf.vocab.Attribution;
import org.numerateweb.math.rdf.vocab.AttributionPair;
import org.numerateweb.math.rdf.vocab.Binding;
import org.numerateweb.math.rdf.vocab.Error;
import org.numerateweb.math.rdf.vocab.Foreign;
import org.numerateweb.math.rdf.vocab.Literal;
import org.numerateweb.math.rdf.vocab.Reference;
import org.numerateweb.math.rdf.vocab.Symbol;
import org.numerateweb.math.rdf.vocab.Variable;

public class NWMathParser {
	static class NamespaceBinding {
		String prefix;
		URI uri;
		NamespaceBinding previous;

		NamespaceBinding(String prefix, URI uri, NamespaceBinding previous) {
			this.prefix = prefix;
			this.uri = uri;
			this.previous = previous;
		}

		String getPrefix(String uri) {
			if (this.uri.toString().equals(uri)) {
				return prefix;
			}
			return previous != null ? previous.getPrefix(uri) : null;
		}

		URI getNamespace(String prefix) {
			if (this.prefix.equals(prefix)) {
				return uri;
			}
			return previous != null ? previous.getNamespace(prefix) : null;
		}
	}

	protected NamespaceBinding embeddedNamespaces;
	protected INamespaces predefinedNamespaces;

	protected INamespaces allNamespaces = new INamespaces() {
		@Override
		public String getPrefix(URI namespace) {
			String prefix = null;
			if (embeddedNamespaces != null) {
				prefix = embeddedNamespaces.getPrefix(namespace.toString());
			}
			if (prefix == null) {
				prefix = predefinedNamespaces.getPrefix(namespace);
			}
			return prefix;
		}

		@Override
		public URI getNamespace(String prefix) {
			URI namespace = null;
			if (embeddedNamespaces != null) {
				namespace = embeddedNamespaces.getNamespace(prefix);
			}
			if (namespace == null) {
				namespace = predefinedNamespaces.getNamespace(prefix);
			}
			return namespace;
		}
	};

	protected boolean resolveURIs = true;

	public NWMathParser(INamespaces predefinedNamespaces) {
		this.predefinedNamespaces = predefinedNamespaces;
	}

	public <T> T parse(IReference mathobj, Builder<T> builder) {
		try {
			return doParse(mathobj, builder);
		} catch (Exception e) {
			return builder.error(URIs.createURI("http://www.openmath.org/cd/moreerrors#unexpected"))
					.str("Reading math object failed.").str(e.getMessage()).end();
		}
	}

	/**
	 * Controls if URI references are resolved or not.
	 * 
	 * @param resolveURIs
	 *            <code>true</code> if parse should dive into named resource, else
	 *            <code>false</code>.
	 * @return The parser instance
	 */
	public NWMathParser resolveURIs(boolean resolveURIs) {
		this.resolveURIs = resolveURIs;
		return this;
	}

	protected <T> T doParse(IReference mathobj, Builder<T> builder) {
		URI uri = mathobj.getURI();

		// resolve reference objects
		if ((uri == null || resolveURIs) && mathobj instanceof Reference && ((Reference) mathobj).getTarget() != null) {
			Set<IReference> seen = new HashSet<>();
			seen.add(mathobj);
			IReference refTo = ((Reference) mathobj).getTarget();
			while (refTo != null && !seen.contains(refTo)) {
				seen.add(refTo);
				if (refTo instanceof Reference) {
					refTo = ((Reference) mathobj).getTarget();
				}
				if (refTo != null) {
					mathobj = refTo;
				}
			}
		}

		boolean isSymbol = mathobj instanceof Symbol;
		if (uri == null || resolveURIs || isSymbol) {
			if (isSymbol) {
				return createOMS((Symbol) mathobj, builder);
			} else if (mathobj instanceof Variable) {
				return createOMV((Variable) mathobj, builder);
			} else if (mathobj instanceof Literal) {
				Literal l = (Literal) mathobj;
				ILiteral value = l.getValue();
				if (value != null) {
					URI type = value.getDatatype();
					if (XMLSCHEMA.TYPE_INTEGER.equals(type) || XMLSCHEMA.TYPE_INT.equals(type)
							|| XMLSCHEMA.TYPE_LONG.equals(type)) {
						return createOMI(l, builder);
					} else if (XMLSCHEMA.TYPE_FLOAT.equals(type) || XMLSCHEMA.TYPE_DOUBLE.equals(type)
							|| XMLSCHEMA.TYPE_DECIMAL.equals(type)) {
						return createOMF(l, builder);
					} else if (XMLSCHEMA.TYPE_BASE64BINARY.equals(type)) {
						return createOMB(l, builder);
					}
				}
				return createOMSTR(l, builder);
			} else if (mathobj instanceof Application) {
				return createOMA((Application) mathobj, builder);
			} else if (mathobj instanceof Binding) {
				return createOMBIND((Binding) mathobj, builder);
			} else if (mathobj instanceof Error) {
				return createOME((Error) mathobj, builder);
			} else if (mathobj instanceof Attribution) {
				return createOMATTR((Attribution) mathobj, builder);
			} else if (mathobj instanceof Foreign) {
				return createOMFOREIGN((Foreign) mathobj, builder);
			}
		}
		if (uri != null && uri.toString().startsWith("http://www.openmath.org/cd/")) {
			// this is likely a mathematical symbol
			return builder.s(uri);
		}
		return builder.ref(mathobj);
	}

	@SuppressWarnings("unchecked")
	protected <T> T doParse(Iterable<? extends IReference> objs, Builder<T> builder) {
		T result = null;
		if (objs != null) {
			for (IReference obj : objs) {
				result = doParse(obj, builder);
			}
		}
		if (result == null) {
			result = (T) builder;
		}
		return result;
	}

	public <T> T createOMS(Symbol s, Builder<T> builder) {
		return builder.s(s.getURI());
	}

	public <T> T createOMV(Variable v, Builder<T> builder) {
		return builder.var(v.getName());
	}

	public <T> T createOMI(Literal l, Builder<T> builder) {
		return builder.i(new BigInteger(l.getValue().getLabel()));
	}

	public <T> T createOMB(Literal l, Builder<T> builder) {
		return builder.b(l.getValue().getLabel());
	}

	public <T> T createOMSTR(Literal l, Builder<T> builder) {
		ILiteral value = l.getValue();
		return builder.str(value == null ? "" : value.getLabel());
	}

	public <T> T createOMF(Literal l, Builder<T> builder) {
		return builder.f(Double.parseDouble(l.getValue().getLabel()));
	}

	public <T> T createOMA(Application application, Builder<T> builder) {
		IReference operator = application.getOperator();
		SeqBuilder<T> appBuilder = doParse(operator, builder.apply());
		if (OMRdfSymbols.RESOURCESET.equals(operator)) {
			Iterator<IReference> it = application.getArguments().iterator();
			if (it.hasNext()) {
				Object arg = it.next();
				if (arg instanceof Literal) {
					// arg is already a Manchester OWL expression
					createOMSTR((Literal) arg, appBuilder);
				} else {
					// the factory knows how to handle RDF references
					appBuilder.rdfClass((IReference) arg, allNamespaces);
				}
			}
		} else if (OMRdfSymbols.RESOURCE.equals(operator)) {
			Iterator<IReference> it = application.getArguments().iterator();
			if (it.hasNext()) {
				createResource(it.next(), appBuilder);
			}
		} else if (OMRdfSymbols.VALUE.equals(operator) || OMRdfSymbols.VALUESET.equals(operator)) {
			Iterator<IReference> it = application.getArguments().iterator();
			if (it.hasNext()) {
				createResource(it.next(), appBuilder);
			}
			while (it.hasNext()) {
				doParse(it.next(), appBuilder);
			}
		} else {
			doParse(application.getArguments(), appBuilder);
		}
		return appBuilder.end();
	}

	protected <T> T createResource(Object value, Builder<T> builder) {
		if (value instanceof Literal) {
			// value is already an IRI reference or a prefixed name
			return createOMSTR((Literal) value, builder);
		} else {
			return builder.ref((IReference) value);
		}
	}

	public <T> T createOMBIND(Binding binding, Builder<T> builder) {
		BindingBuilder<T> bindingBuilder = builder.bind();
		VariablesBuilder<?> varBuilder = doParse(binding.getBinder(), bindingBuilder.binder()).variables();
		List<Variable> variables = binding.getVariables();
		if (variables != null) {
			for (Variable var : variables) {
				varBuilder.var(var.getName());
			}
		}
		varBuilder.end();
		return doParse(binding.getBody(), bindingBuilder.body()).end();
	}

	public <T> T createOME(Error error, Builder<T> builder) {
		return doParse(error.getArguments(), builder.error(error.getSymbol().getURI())).end();
	}

	public <T> T createOMATTR(Attribution attribution, Builder<T> builder) {
		NamespaceBinding oldNamespaces = embeddedNamespaces;
		try {
			for (AttributionPair pair : attribution.getArguments()) {
				builder = doParse(pair.getAttributeValue(), builder.attr(pair.getAttributeKey().getURI()));
				if (OMRdfSymbols.PREFIXES.equals(pair.getAttributeKey().getURI())) {
					embeddedNamespaces = extractBindings(pair.getAttributeValue(), oldNamespaces);
				}
			}
			return doParse(attribution.getTarget(), builder);
		} finally {
			embeddedNamespaces = oldNamespaces;
		}
	}

	private NamespaceBinding extractBindings(Object value, NamespaceBinding namespaces) {
		if (value instanceof Application) {
			Application application = (Application) value;
			List<?> arguments = application.getArguments();
			if (OMRdfSymbols.PREFIX.equals(application.getOperator())) {
				if (arguments.size() == 2) {
					return new NamespaceBinding(((Literal) arguments.get(0)).toString(),
							URIs.createURI(((Literal) arguments.get(1)).toString()), namespaces);
				}
			} else {
				for (Object arg : arguments) {
					namespaces = extractBindings(arg, namespaces);
				}
			}
		}
		return namespaces;
	}

	public <T> T createOMFOREIGN(Foreign foreign, Builder<T> builder) {
		return builder.foreign(foreign.getEncoding(), foreign.getValue());
	}
}

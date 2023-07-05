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
package org.numerateweb.math.rdf;

import net.enilink.komma.core.*;
import net.enilink.vocab.rdf.RDF;
import net.enilink.vocab.xmlschema.XMLSCHEMA;
import org.numerateweb.math.model.Builder;
import org.numerateweb.math.model.Builder.BindingBuilder;
import org.numerateweb.math.model.Builder.SeqBuilder;
import org.numerateweb.math.model.Builder.VariablesBuilder;
import org.numerateweb.math.ns.INamespaces;
import org.numerateweb.math.om.rdf.OMRdfSymbols;
import org.numerateweb.math.rdf.vocab.NWMATH;

import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

public class NWMathGraphParser {
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

	protected final IGraph graph;
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

	public NWMathGraphParser(IGraph graph, INamespaces predefinedNamespaces) {
		this.graph = graph;
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
	public NWMathGraphParser resolveURIs(boolean resolveURIs) {
		this.resolveURIs = resolveURIs;
		return this;
	}

	protected boolean isInstanceOf(IReference obj, URI type) {
		return graph.contains(obj, RDF.PROPERTY_TYPE, type);
	}

	protected <T> T doParse(IReference mathobj, Builder<T> builder) {
		URI uri = mathobj.getURI();

		// resolve reference objects
		IReference target;
		if ((uri == null || resolveURIs)
				&& isInstanceOf(mathobj, NWMATH.TYPE_REFERENCE)
				&& (target = graph.filter(mathobj, NWMATH.PROPERTY_TARGET, null).objectReference()) != null) {
			Set<IReference> seen = new HashSet<>();
			seen.add(mathobj);
			while (target != null && !seen.contains(target)) {
				seen.add(target);
				if (isInstanceOf(target, NWMATH.TYPE_REFERENCE)) {
					target = graph.filter(mathobj, NWMATH.PROPERTY_TARGET, null).objectReference();
				}
				if (target != null) {
					mathobj = target;
				}
			}
		}

		boolean isSymbol = isInstanceOf(mathobj, NWMATH.TYPE_SYMBOL);
		if (uri == null || resolveURIs || isSymbol) {
			if (isSymbol) {
				return createOMS(mathobj, builder);
			} else if (isInstanceOf(mathobj, NWMATH.TYPE_VARIABLE)) {
				return createOMV(mathobj, builder);
			} else if (isInstanceOf(mathobj, NWMATH.TYPE_LITERAL)) {
				ILiteral value = graph.filter(mathobj, NWMATH.PROPERTY_VALUE, null).objectLiteral();
				if (value != null) {
					URI type = value.getDatatype();
					if (XMLSCHEMA.TYPE_INTEGER.equals(type) || XMLSCHEMA.TYPE_INT.equals(type)
							|| XMLSCHEMA.TYPE_LONG.equals(type)) {
						return createOMI(value, builder);
					} else if (XMLSCHEMA.TYPE_FLOAT.equals(type) || XMLSCHEMA.TYPE_DOUBLE.equals(type)
							|| XMLSCHEMA.TYPE_DECIMAL.equals(type)) {
						return createOMF(value, builder);
					} else if (XMLSCHEMA.TYPE_BASE64BINARY.equals(type)) {
						return createOMB(value, builder);
					}
				}
				return createOMSTR(value, builder);
			} else if (isInstanceOf(mathobj, NWMATH.TYPE_APPLICATION)) {
				return createOMA(mathobj, builder);
			} else if (isInstanceOf(mathobj, NWMATH.TYPE_BINDING)) {
				return createOMBIND(mathobj, builder);
			} else if (isInstanceOf(mathobj, NWMATH.TYPE_ERROR)) {
				return createOME(mathobj, builder);
			} else if (isInstanceOf(mathobj, NWMATH.TYPE_ATTRIBUTION)) {
				return createOMATTR(mathobj, builder);
			} else if (isInstanceOf(mathobj, NWMATH.TYPE_FOREIGN)) {
				return createOMFOREIGN(mathobj, builder);
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

	public <T> T createOMS(IReference s, Builder<T> builder) {
		return builder.s(s.getURI());
	}

	public <T> T createOMV(IReference v, Builder<T> builder) {
		return builder.var(graph.filter(v, NWMATH.PROPERTY_NAME, null).objectString());
	}

	public <T> T createOMI(ILiteral l, Builder<T> builder) {
		return builder.i(new BigInteger(l.getLabel()));
	}

	public <T> T createOMB(ILiteral l, Builder<T> builder) {
		return builder.b(l.getLabel());
	}

	public <T> T createOMSTR(ILiteral l, Builder<T> builder) {
		return builder.str(l == null ? "" : l.getLabel());
	}

	public <T> T createOMF(ILiteral l, Builder<T> builder) {
		return builder.f(Double.parseDouble(l.getLabel()));
	}

	public <T> T createOMA(IReference application, Builder<T> builder) {
		IReference operator = graph.filter(application, NWMATH.PROPERTY_OPERATOR, null).objectReference();
		SeqBuilder<T> appBuilder = doParse(operator, builder.apply());
		if (OMRdfSymbols.RESOURCESET.equals(operator)) {
			Iterator<IReference> it = parseArguments(application).iterator();
			if (it.hasNext()) {
				IReference arg = it.next();
				if (isInstanceOf(arg, NWMATH.TYPE_LITERAL)) {
					// arg is already a Manchester OWL expression
					createOMSTR(graph.filter(arg, NWMATH.PROPERTY_VALUE, null).objectLiteral(), appBuilder);
				} else {
					// the factory knows how to handle RDF references
					appBuilder.rdfClass(arg, allNamespaces);
				}
			}
		} else if (OMRdfSymbols.RESOURCE.equals(operator)) {
			Iterator<IReference> it = parseArguments(application).iterator();
			if (it.hasNext()) {
				createResource(it.next(), appBuilder);
			}
		} else if (OMRdfSymbols.VALUE.equals(operator) || OMRdfSymbols.VALUESET.equals(operator)) {
			Iterator<IReference> it = parseArguments(application).iterator();
			if (it.hasNext()) {
				createResource(it.next(), appBuilder);
			}
			while (it.hasNext()) {
				doParse(it.next(), appBuilder);
			}
		} else {
			doParse(parseArguments(application), appBuilder);
		}
		return appBuilder.end();
	}

	protected <T> T createResource(Object value, Builder<T> builder) {
		if (isInstanceOf((IReference) value, NWMATH.TYPE_LITERAL)) {
			// value is already an IRI reference or a prefixed name
			return createOMSTR(graph.filter((IReference) value, NWMATH.PROPERTY_VALUE, null)
					.objectLiteral(), builder);
		} else {
			return builder.ref((IReference) value);
		}
	}

	public <T> T createOMBIND(IReference binding, Builder<T> builder) {
		BindingBuilder<T> bindingBuilder = builder.bind();
		VariablesBuilder<?> varBuilder = doParse(graph.filter(binding, NWMATH.PROPERTY_BINDER, null)
				.objectReference(), bindingBuilder.binder()).variables();
		List<Object> variables = parseRdfList(graph.filter(binding, NWMATH.PROPERTY_VARIABLES, null)
				.objectReference());
		if (variables != null) {
			for (Object var : variables) {
				if (var instanceof IReference) {
					varBuilder.var(graph.filter((IReference) var, NWMATH.PROPERTY_NAME, null).objectString());
				} else {
					// TODO throw an exception, this is a parse error
				}
			}
		}
		varBuilder.end();
		return doParse(graph.filter(binding, NWMATH.PROPERTY_BODY, null).objectReference(),
				bindingBuilder.body()).end();
	}

	public <T> T createOME(IReference error, Builder<T> builder) {
		return doParse(parseArguments(error), builder.error(graph.filter(error, NWMATH.PROPERTY_SYMBOL, null)
				.objectReference().getURI())).end();
	}

	public <T> T createOMATTR(IReference attribution, Builder<T> builder) {
		NamespaceBinding oldNamespaces = embeddedNamespaces;
		try {
			for (IReference pair : parseArguments(attribution)) {
				IReference attributeKey = graph.filter(pair, NWMATH.PROPERTY_ATTRIBUTEKEY, null)
						.objectReference();
				IReference attributeValue = graph.filter(pair, NWMATH.PROPERTY_ATTRIBUTEVALUE, null)
						.objectReference();
				builder = doParse(attributeValue, builder.attr(attributeKey.getURI()));
				if (OMRdfSymbols.PREFIXES.equals(attributeKey.getURI())) {
					embeddedNamespaces = extractBindings(attributeValue, oldNamespaces);
				}
			}
			return doParse(graph.filter(attribution, NWMATH.PROPERTY_TARGET, null).objectReference(), builder);
		} finally {
			embeddedNamespaces = oldNamespaces;
		}
	}

	private NamespaceBinding extractBindings(Object value, NamespaceBinding namespaces) {
		if (value instanceof IReference && isInstanceOf((IReference) value, NWMATH.TYPE_APPLICATION)) {
			IReference application = (IReference) value;
			List<IReference> arguments = parseArguments(application);
			if (graph.contains(application, NWMATH.PROPERTY_OPERATOR, OMRdfSymbols.PREFIX)) {
				if (arguments.size() == 2) {
					String prefix = graph.filter(arguments.get(0), NWMATH.PROPERTY_VALUE, null).objectString();
					String uri = graph.filter(arguments.get(1), NWMATH.PROPERTY_VALUE, null).objectString();
					return new NamespaceBinding(prefix, URIs.createURI(uri), namespaces);
				}
			} else {
				for (Object arg : arguments) {
					namespaces = extractBindings(arg, namespaces);
				}
			}
		}
		return namespaces;
	}

	public <T> T createOMFOREIGN(IReference foreign, Builder<T> builder) {
		return builder.foreign(graph.filter(foreign, NWMATH.PROPERTY_ENCODING, null).objectString(),
				graph.filter(foreign, NWMATH.PROPERTY_VALUE, null).objectString());
	}

	protected List<IReference> parseArguments(IReference mathobj) {
		return parseRdfList(graph.filter(mathobj, NWMATH.PROPERTY_ARGUMENTS, null)
				.objectReference()).stream()
				// TODO maybe raise exception if a literal is found within the arguments list
				.filter(arg -> arg instanceof IReference)
				.map(arg -> (IReference) arg).collect(Collectors.toList());
	}

	protected List<Object> parseRdfList(IReference list) {
		List<Object> items = new ArrayList<>();
		Set<IReference> seen = new HashSet<>();
		while (list != null && seen.add(list) && !RDF.NIL.equals(list)) {
			IReference rest = null;
			IValue first = null;
			for (IStatement stmt : graph.match(list, null, null, true)) {
				if (RDF.PROPERTY_FIRST.equals(stmt.getPredicate())) {
					first = (IValue) stmt.getObject();
				} else if (RDF.PROPERTY_REST.equals(stmt.getPredicate())) {
					if (stmt.getObject() instanceof IReference) {
						rest = (IReference) stmt.getObject();
					} else {
						// invalid list data
						break;
					}
				}
			}
			if (first != null) {
				// convert RDF value to Java
				items.add(first);
				list = rest;
			}
		}
		return items;
	}
}

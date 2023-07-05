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
import net.enilink.komma.parser.manchester.ManchesterSyntaxParser;
import net.enilink.vocab.rdf.RDF;
import net.enilink.vocab.xmlschema.XMLSCHEMA;
import org.numerateweb.math.manchester.StatementConverterActions;
import org.numerateweb.math.manchester.ValueConverter;
import org.numerateweb.math.model.Builder;
import org.numerateweb.math.model.DelegatingSeqBuilder;
import org.numerateweb.math.ns.INamespaces;
import org.numerateweb.math.om.rdf.OMRdfSymbols;
import org.numerateweb.math.rdf.vocab.NWMATH;
import org.parboiled.Parboiled;
import org.parboiled.parserunners.BasicParseRunner;
import org.parboiled.support.ParsingResult;

import java.math.BigInteger;
import java.util.*;

public class NWMathGraphBuilderBase<T> implements Builder<T> {
	private static Map<URI, BuilderFactory> builderFactories = new HashMap<>();

	static {
		builderFactories.put(OMRdfSymbols.RESOURCE, new BuilderFactory() {
			@Override
			public <E> SeqBuilder<E> create(final NWMathGraphBuilderBase<E> parent, final URI symbol) {
				return new NWMathSeqBuilder<>(parent.context) {
					IReference resourceRef;

					@Override
					public SeqBuilder<E> build(IReference obj) {
						resourceRef = obj;
						return super.build(obj);
					}

					@Override
					public SeqBuilder<E> str(String value) {
						return build(toUri(context, value));
					}

					@Override
					public E end() {
						IReference application = createApplication(symbol);
						getGraph().add(application, NWMATH.PROPERTY_ARGUMENTS, createRdfList(Arrays.asList(resourceRef)));
						return parent.build(application);
					}
				};
			}
		});

		BuilderFactory valueAndValuesetBuilderFactory = new BuilderFactory() {
			@Override
			public <E> SeqBuilder<E> create(final NWMathGraphBuilderBase<E> parent, final URI symbol) {
				return new NWMathSeqBuilder<>(parent.context) {
					List<IReference> args = new ArrayList<>();

					@Override
					public SeqBuilder<E> build(IReference obj) {
						args.add(obj);
						return super.build(obj);
					}

					@Override
					public SeqBuilder<E> str(String value) {
						if (args.isEmpty()) {
							build(toUri(context, value));
						}
						return this;
					}

					@Override
					public E end() {
						IReference application = createApplication(symbol);
						getGraph().add(application, NWMATH.PROPERTY_ARGUMENTS, createRdfList(args));
						return parent.build(application);
					}
				};
			}
		};

		builderFactories.put(OMRdfSymbols.VALUE, valueAndValuesetBuilderFactory);
		builderFactories.put(OMRdfSymbols.VALUESET, valueAndValuesetBuilderFactory);

		builderFactories.put(OMRdfSymbols.RESOURCESET, new BuilderFactory() {
			@Override
			public <E> SeqBuilder<E> create(final NWMathGraphBuilderBase<E> parent, final URI symbol) {
				return new NWMathSeqBuilder<>(parent.context) {
					String classDescription;

					@Override
					public SeqBuilder<E> str(String value) {
						classDescription = value;
						return this;
					}

					@Override
					public E end() {
						ValueConverter converter = new ValueConverter(new INamespaces() {
							@Override
							public String getPrefix(URI namespace) {
								// not supported
								throw new UnsupportedOperationException();
							}

							@Override
							public URI getNamespace(String prefix) {
								return context.getURI(prefix);
							}
						});
						StatementConverterActions actions = new StatementConverterActions(converter);

						ManchesterSyntaxParser manchesterParser = Parboiled.createParser(ManchesterSyntaxParser.class,
								actions);
						ParsingResult<Object> result = new BasicParseRunner<>(manchesterParser.Description())
								.run(classDescription);

						IReference application = createApplication(symbol);
						getGraph().add(application, NWMATH.PROPERTY_ARGUMENTS,
								createRdfList(Arrays.asList(converter.toValue(result.resultValue))));
						getGraph().addAll(actions.getResult());
						return parent.build(application);
					}
				};
			}
		});
	}

	protected Context context;
	protected URI nextId;

	public NWMathGraphBuilderBase(IGraph graph, INamespaces ns) {
		this(new Context(graph, ns));
	}

	public NWMathGraphBuilderBase(Context context) {
		this.context = context;
	}

	protected URI toUri(Context context, String uriOrPrefixedName) {
		if (uriOrPrefixedName.startsWith("<")) {
			return URIs.createURI(uriOrPrefixedName.substring(1, uriOrPrefixedName.length() - 1));
		}
		int colonIndex = uriOrPrefixedName.indexOf(':');
		String prefix, localPart;
		if (colonIndex >= 0) {
			prefix = uriOrPrefixedName.substring(0, colonIndex);
			localPart = uriOrPrefixedName.substring(colonIndex + 1, uriOrPrefixedName.length());
		} else {
			prefix = "";
			localPart = uriOrPrefixedName;
		}

		URI nsUri = context.getURI(prefix);

		if (nsUri != null) {
			return nsUri.appendLocalPart(localPart);
		} else {
			throw new IllegalArgumentException("Unknown prefix: \"" + prefix + "\"");
		}

	}

	public Context getContext() {
		return context;
	}

	protected IGraph getGraph() {
		return context.graph;
	}

	/**
	 * Creates named or anonymous object instances.
	 *
	 * @param type
	 * @return
	 */
	protected IReference create(URI type, boolean useId) {
		IReference reference;
		if (nextId != null) {
			reference = nextId;
			nextId = null;
		} else {
			reference = new BlankNode();
		}
		getGraph().add(reference, RDF.PROPERTY_TYPE, type);
		return reference;
	}

	protected IReference createApplication(URI symbol) {
		IReference application = new BlankNode();
		getGraph().add(application, RDF.PROPERTY_TYPE, NWMATH.TYPE_APPLICATION);
		getGraph().add(application, NWMATH.PROPERTY_OPERATOR, symbol);
		return application;
	}

	public T build(IReference obj) {
		return null;
	}

	@Override
	public T s(URI symbol) {
		return build(symbol);
	}

	@Override
	public T var(String variableName) {
		IReference variable = create(NWMATH.TYPE_VARIABLE, true);
		getGraph().add(variable, NWMATH.PROPERTY_NAME, new Literal(variableName, XMLSCHEMA.TYPE_STRING));
		return build(variable);
	}

	@Override
	public T i(BigInteger value) {
		IReference literal = create(NWMATH.TYPE_LITERAL, true);
		getGraph().add(literal, NWMATH.PROPERTY_VALUE,
				new net.enilink.komma.core.Literal(value.toString(), XMLSCHEMA.TYPE_INTEGER));
		return build(literal);
	}

	@Override
	public Builder<T> id(URI id) {
		nextId = id;
		return this;
	}

	@Override
	public T b(String base64Binary) {
		IReference literal = create(NWMATH.TYPE_LITERAL, true);
		getGraph().add(literal, NWMATH.PROPERTY_VALUE,
				new net.enilink.komma.core.Literal(base64Binary, XMLSCHEMA.TYPE_BASE64BINARY));
		return build(literal);
	}

	@Override
	public T str(String value) {
		IReference literal = create(NWMATH.TYPE_LITERAL, true);
		getGraph().add(literal, NWMATH.PROPERTY_VALUE,
				new net.enilink.komma.core.Literal(value, XMLSCHEMA.TYPE_STRING));
		return build(literal);
	}

	@Override
	public T f(double value) {
		IReference literal = create(NWMATH.TYPE_LITERAL, true);
		getGraph().add(literal, NWMATH.PROPERTY_VALUE,
				new net.enilink.komma.core.Literal(Double.toString(value), XMLSCHEMA.TYPE_DOUBLE));
		return build(literal);
	}

	protected BuilderFactory getBuilderFactory(URI symbol) {
		return builderFactories.get(symbol);
	}

	@Override
	public SeqBuilder<T> apply() {
		final NWMathGraphBuilderBase<T> parent = this;
		final DelegatingSeqBuilder<T> delegatingBuilder = new DelegatingSeqBuilder<>();
		delegatingBuilder.delegate(new NWMathSeqBuilder<>(context) {
			IReference application = null;
			List<IReference> args = null;

			@Override
			public SeqBuilder<T> s(URI symbol) {
				if (application == null) {
					BuilderFactory factory = NWMathGraphBuilderBase.this.getBuilderFactory(symbol);
					if (factory != null) {
						delegatingBuilder.delegate(factory.create(parent, symbol));
						return delegatingBuilder;
					}
				}
				return super.s(symbol);
			}

			@Override
			public SeqBuilder<T> build(IReference expr) {
				if (application == null) {
					application = create(NWMATH.TYPE_APPLICATION, true);
					getGraph().add(application, NWMATH.PROPERTY_OPERATOR, expr);
				} else {
					if (args == null) {
						args = new ArrayList<>();
					}
					args.add(expr);
				}
				return this;
			}

			@Override
			public T end() {
				getGraph().add(application, NWMATH.PROPERTY_ARGUMENTS, createRdfList(args));
				return parent.build(application);
			}
		});
		return delegatingBuilder;
	}

	@Override
	public BindingBuilder<T> bind() {
		return new BindingBuilder<>() {
			final BindingBuilder<T> self = this;
			IReference binder, body;
			List<IReference> variables = new ArrayList<>();

			@Override
			public Builder<BindingBuilder<T>> binder() {
				return new NWMathGraphBuilderBase<>(context) {
					@Override
					public BindingBuilder<T> build(IReference obj) {
						binder = obj;
						return self;
					}
				};
			}

			@Override
			public Builder<BindingBuilder<T>> body() {
				return new NWMathGraphBuilderBase<>(context) {
					@Override
					public BindingBuilder<T> build(IReference obj) {
						body = obj;
						return self;
					}
				};
			}

			@Override
			public T end() {
				IReference binding = create(NWMATH.TYPE_BINDING, true);
				getGraph().add(binding, NWMATH.PROPERTY_BINDER, binder);
				getGraph().add(binding, NWMATH.PROPERTY_VARIABLES, createRdfList(variables));
				getGraph().add(binding, NWMATH.PROPERTY_BODY, body);
				return build(binding);
			}

			@Override
			public VariablesBuilder<BindingBuilder<T>> variables() {
				return new NWMathVarBuilder<>(context) {
					@Override
					public VariablesBuilder<BindingBuilder<T>> build(IReference obj) {
						variables.add(obj);
						return this;
					}

					@Override
					public BindingBuilder<T> end() {
						return self;
					}
				};
			}
		};
	}

	@Override
	public SeqBuilder<T> error(final URI symbol) {
		final NWMathGraphBuilderBase<T> parent = this;
		final IReference error = create(NWMATH.TYPE_ERROR, true);
		getGraph().add(error, NWMATH.PROPERTY_SYMBOL, symbol);
		return new NWMathSeqBuilder<>(context) {
			List<IReference> args = null;

			@Override
			public SeqBuilder<T> build(IReference obj) {
				if (args == null) {
					args = new ArrayList<>();
				}
				args.add(obj);
				return this;
			}

			@Override
			public T end() {
				getGraph().add(error, NWMATH.PROPERTY_ARGUMENTS, createRdfList(args));
				return parent.build(error);
			}
		};
	}

	@Override
	public Builder<Builder<T>> attr(final URI symbol) {
		final NWMathGraphBuilderBase<T> parent = this;
		if (OMRdfSymbols.PREFIXES.equals(symbol)) {
			final List<Namespace> namespaces = new ArrayList<>();
			return new NWMathGraphBuilderBase<>(context) {
				@Override
				public SeqBuilder<Builder<T>> apply() {
					return new NamespaceBuilder<>(context, namespaces) {
						boolean isSet = false;

						public SeqBuilder<Builder<T>> s(URI symbol) {
							if ("http://www.openmath.org/cd/set1#set".equals(symbol.toString())) {
								// support set1.set for multiple prefix
								// declarations
								isSet = true;
								return this;
							}
							return super.s(symbol);
						}

						@Override
						public SeqBuilder<SeqBuilder<Builder<T>>> apply() {
							final SeqBuilder<Builder<T>> parent = this;
							return new NamespaceBuilder<>(context, namespaces) {
								@Override
								SeqBuilder<Builder<T>> builder() {
									return parent;
								}
							};
						}

						@Override
						public Builder<T> end() {
							if (isSet) {
								return builder();
							}
							return super.end();
						}

						@Override
						Builder<T> builder() {
							return namespaces.isEmpty() ? parent
									: new NWMathGraphBuilderBase<T>(context.childContext(namespaces)) {
								@Override
								public T build(IReference target) {
									return parent.build(target);
								}
							};
						}
					};
				}
			};
		}

		final List<IReference> pairs = new ArrayList<>();
		return new NWMathGraphBuilderBase<Builder<T>>(context) {
			Builder<Builder<T>> self = this;
			URI currentKey = symbol;

			protected void addPair(IReference obj) {
				// store key-value pair
				IReference pair = create(NWMATH.TYPE_ATTRIBUTIONPAIR, false);
				getGraph().add(currentKey, RDF.PROPERTY_TYPE, NWMATH.TYPE_SYMBOL);
				getGraph().add(pair, NWMATH.PROPERTY_ATTRIBUTEKEY, currentKey);
				getGraph().add(pair, NWMATH.PROPERTY_ATTRIBUTEVALUE, obj);
				pairs.add(pair);
			}

			@Override
			public Builder<T> build(IReference obj) {
				addPair(obj);
				return new NWMathGraphBuilderBase<>(context) {
					@Override
					public Builder<Builder<T>> attr(URI symbol) {
						currentKey = symbol;
						return self;
					}

					@Override
					public T build(IReference target) {
						if (pairs.isEmpty()) {
							return parent.build(target);
						}
						IReference attribution = create(NWMATH.TYPE_ATTRIBUTION, false);
						getGraph().add(attribution, NWMATH.PROPERTY_TARGET, target);
						getGraph().add(attribution, NWMATH.PROPERTY_ARGUMENTS, createRdfList(pairs));
						return parent.build(attribution);
					}
				};
			}
		};
	}

	@Override
	public T foreign(String encoding, String content) {
		IReference foreign = create(NWMATH.TYPE_FOREIGN, true);
		getGraph().add(foreign, NWMATH.PROPERTY_ENCODING, new Literal(encoding, XMLSCHEMA.TYPE_STRING));
		getGraph().add(foreign, NWMATH.PROPERTY_VALUE, new Literal(content, XMLSCHEMA.TYPE_STRING));
		return build(foreign);
	}

	@Override
	public T ref(IReference reference) {
		// create a reference object if the new object should get a URI
		if (nextId != null) {
			IReference refObj = create(NWMATH.TYPE_REFERENCE, true);
			getGraph().add(refObj, NWMATH.PROPERTY_TARGET, reference);
			reference = refObj;
		}
		return build(reference);
	}

	@Override
	public T rdfClass(IReference reference, INamespaces ns) {
		return build(reference);
	}

	protected IReference createRdfList(List<?> values) {
		if (values.isEmpty()) {
			return RDF.NIL;
		} else {
			IReference result = null;
			IReference prev = null;
			for (Object value : values) {
				IReference list = new BlankNode();
				if (result == null) {
					result = list;
				}
				if (prev != null) {
					getGraph().add(new Statement(prev, RDF.PROPERTY_REST, list));
				}
				getGraph().add(new Statement(list, RDF.PROPERTY_TYPE, RDF.TYPE_LIST));
				getGraph().add(new Statement(list, RDF.PROPERTY_FIRST, value));
				prev = list;
			}
			getGraph().add(new Statement(prev, RDF.PROPERTY_REST, RDF.NIL));
			return result;
		}
	}

	protected interface BuilderFactory {
		<T> SeqBuilder<T> create(NWMathGraphBuilderBase<T> parent, URI symbol);
	}

	protected abstract static class NWMathVarBuilder<B> extends NWMathGraphBuilderBase<VariablesBuilder<B>>
			implements VariablesBuilder<B> {
		protected NWMathVarBuilder(Context context) {
			super(context);
		}

		@Override
		public Builder<VariablesBuilder<B>> attrVar(final URI symbol) {
			final NWMathVarBuilder<B> parent = this;
			final List<IReference> pairs = new ArrayList<>();
			return new NWMathGraphBuilderBase<VariablesBuilder<B>>(context) {
				Builder<VariablesBuilder<B>> self = this;
				URI currentKey = symbol;

				protected void addPair(IReference obj) {
					// store key-value pair
					IReference pair = create(NWMATH.TYPE_ATTRIBUTIONPAIR, false);
					getGraph().add(currentKey, RDF.PROPERTY_TYPE, NWMATH.TYPE_SYMBOL);
					getGraph().add(pair, NWMATH.PROPERTY_ATTRIBUTEKEY, currentKey);
					getGraph().add(pair, NWMATH.PROPERTY_ATTRIBUTEVALUE, obj);
					pairs.add(pair);
				}

				@Override
				public VariablesBuilder<B> build(IReference obj) {
					addPair(obj);
					return new NWMathVarBuilder<>(context) {
						@Override
						public Builder<VariablesBuilder<B>> attrVar(URI symbol) {
							currentKey = symbol;
							return self;
						}

						@Override
						public VariablesBuilder<B> build(IReference var) {
							if (pairs.isEmpty()) {
								return parent.build(var);
							}
							IReference attribution = create(NWMATH.TYPE_ATTRIBUTION, false);
							getGraph().add(attribution, NWMATH.PROPERTY_TARGET, var);
							getGraph().add(attribution, NWMATH.PROPERTY_ARGUMENTS, createRdfList(pairs));
							return parent.build(attribution);
						}

						@Override
						public B end() {
							return parent.end();
						}
					};
				}
			};
		}
	}

	protected abstract static class NWMathSeqBuilder<B> extends NWMathGraphBuilderBase<SeqBuilder<B>>
			implements SeqBuilder<B> {
		protected NWMathSeqBuilder(Context context) {
			super(context);
		}
	}

	abstract static class NamespaceBuilder<B> extends NWMathSeqBuilder<B> {
		List<Namespace> namespaces;
		List<String> args = new ArrayList<>();

		NamespaceBuilder(Context context, List<Namespace> namespaces) {
			super(context);
			this.namespaces = namespaces;
		}

		@Override
		public SeqBuilder<B> s(URI symbol) {
			if (!OMRdfSymbols.PREFIX.equals(symbol)) {
				throw new IllegalArgumentException(
						"Symbol '" + symbol + "' is not allowed as value for '" + OMRdfSymbols.PREFIXES + "'");
			}
			return this;
		}

		@Override
		public SeqBuilder<B> str(String value) {
			args.add(value);
			return this;
		}

		@Override
		public B end() {
			if (args.size() != 2) {
				throw new IllegalArgumentException("The prefix operator requires two arguments.");
			}
			namespaces.add(new Namespace(args.get(0), URIs.createURI(args.get(1))));
			return builder();
		}

		abstract B builder();
	}

	protected static class Context {
		final IGraph graph;
		final INamespaces baseNamespaces;
		final Context parent;
		final List<Namespace> namespaces;

		Context(IGraph graph, INamespaces baseNamespaces) {
			this(graph, baseNamespaces, null, null);
		}

		Context(IGraph graph, INamespaces baseNamespaces, List<Namespace> namespaces, Context parent) {
			this.graph = graph;
			this.baseNamespaces = baseNamespaces;
			this.namespaces = namespaces;
			this.parent = parent;
		}

		public Context childContext(List<Namespace> namespaces) {
			return new Context(graph, baseNamespaces, namespaces, this);
		}

		public URI getURI(String prefix) {
			if (namespaces != null) {
				for (Namespace ns : namespaces) {
					if (ns.getPrefix().equals(prefix)) {
						return ns.getURI();
					}
				}
			}
			if (parent != null) {
				return parent.getURI(prefix);
			}
			if (baseNamespaces != null) {
				return baseNamespaces.getNamespace(prefix);
			}
			return null;
		}
	}
}
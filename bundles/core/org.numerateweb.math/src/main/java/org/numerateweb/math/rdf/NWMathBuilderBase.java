package org.numerateweb.math.rdf;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.enilink.komma.core.IEntityManager;
import net.enilink.komma.core.IReference;
import net.enilink.komma.core.Namespace;
import net.enilink.komma.core.URI;
import net.enilink.komma.core.URIs;
import net.enilink.komma.parser.manchester.ManchesterSyntaxParser;
import net.enilink.vocab.xmlschema.XMLSCHEMA;

import org.numerateweb.math.concepts.Application;
import org.numerateweb.math.concepts.Attribution;
import org.numerateweb.math.concepts.AttributionPair;
import org.numerateweb.math.concepts.Binding;
import org.numerateweb.math.concepts.Error;
import org.numerateweb.math.concepts.Foreign;
import org.numerateweb.math.concepts.Literal;
import org.numerateweb.math.concepts.Object;
import org.numerateweb.math.concepts.Reference;
import org.numerateweb.math.concepts.Symbol;
import org.numerateweb.math.concepts.Variable;
import org.numerateweb.math.helper.StatementConverterActions;
import org.numerateweb.math.helper.ValueConverter;
import org.numerateweb.math.model.Builder;
import org.numerateweb.math.model.DelegatingSeqBuilder;
import org.numerateweb.math.ns.INamespaces;
import org.numerateweb.math.om.rdf.OMRdfSymbols;
import org.parboiled.Parboiled;
import org.parboiled.parserunners.BasicParseRunner;
import org.parboiled.support.ParsingResult;

public class NWMathBuilderBase<T> implements Builder<T> {
	protected Context context;

	protected abstract static class NWMathVarBuilder<B> extends
			NWMathBuilderBase<VariablesBuilder<B>> implements
			VariablesBuilder<B> {
		protected NWMathVarBuilder(Context context) {
			super(context);
		}

		@Override
		public Builder<VariablesBuilder<B>> attrVar(final URI symbol) {
			final NWMathVarBuilder<B> parent = this;
			final List<AttributionPair> pairs = new ArrayList<AttributionPair>();
			return new NWMathBuilderBase<VariablesBuilder<B>>(context) {
				Builder<VariablesBuilder<B>> self = this;
				URI currentKey = symbol;

				protected void addPair(IReference obj) {
					// store key-value pair
					AttributionPair pair = getEntityManager().create(
							AttributionPair.class);
					pair.setAttributeKey(getEntityManager().createNamed(
							currentKey, Symbol.class));
					pair.setAttributeValue(obj);
					pairs.add(pair);
				}

				@Override
				public VariablesBuilder<B> build(IReference obj) {
					addPair(obj);
					return new NWMathVarBuilder<B>(context) {
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
							Attribution attribution = getEntityManager()
									.create(Attribution.class);
							attribution.setTarget(var);
							attribution.setArguments(pairs);
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

	protected abstract static class NWMathSeqBuilder<B> extends
			NWMathBuilderBase<SeqBuilder<B>> implements SeqBuilder<B> {
		protected NWMathSeqBuilder(Context context) {
			super(context);
		}
	}

	abstract static class NamespaceBuilder<B> extends NWMathSeqBuilder<B> {
		List<Namespace> namespaces;
		List<String> args = new ArrayList<String>();

		NamespaceBuilder(Context context, List<Namespace> namespaces) {
			super(context);
			this.namespaces = namespaces;
		}

		@Override
		public SeqBuilder<B> s(URI symbol) {
			if (!OMRdfSymbols.PREFIX.equals(symbol)) {
				throw new IllegalArgumentException("Symbol '" + symbol
						+ "' is not allowed as value for '"
						+ OMRdfSymbols.PREFIXES + "'");
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
				throw new IllegalArgumentException(
						"The prefix operator requires two arguments.");
			}
			namespaces.add(new Namespace(args.get(0), URIs.createURI(args
					.get(1))));
			return builder();
		}

		abstract B builder();
	}

	protected static class Context {
		final IEntityManager em;
		final INamespaces baseNamespaces;
		final Context parent;
		final List<Namespace> namespaces;

		Context(IEntityManager em, INamespaces baseNamespaces) {
			this(em, baseNamespaces, null, null);
		}

		Context(IEntityManager em, INamespaces baseNamespaces,
				List<Namespace> namespaces, Context parent) {
			this.em = em;
			this.baseNamespaces = baseNamespaces;
			this.namespaces = namespaces;
			this.parent = parent;
		}

		public Context childContext(List<Namespace> namespaces) {
			return new Context(em, baseNamespaces, namespaces, this);
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

	protected interface BuilderFactory {
		<T> SeqBuilder<T> create(NWMathBuilderBase<T> parent, URI symbol);
	}

	private static Map<URI, BuilderFactory> builderFactories = new HashMap<URI, BuilderFactory>();
	static {
		builderFactories.put(OMRdfSymbols.RESOURCE, new BuilderFactory() {
			@Override
			public <E> SeqBuilder<E> create(final NWMathBuilderBase<E> parent,
					final URI symbol) {
				return new NWMathSeqBuilder<E>(parent.context) {
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
						Application application = createApplication(symbol);
						application.setArguments(Arrays.asList(resourceRef));
						return parent.build(application);
					}
				};
			}
		});

		BuilderFactory valueAndValuesetBuilderFactory = new BuilderFactory() {
			@Override
			public <E> SeqBuilder<E> create(final NWMathBuilderBase<E> parent,
					final URI symbol) {
				return new NWMathSeqBuilder<E>(parent.context) {
					List<IReference> args = new ArrayList<IReference>();

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
						Application application = createApplication(symbol);
						application.setArguments(args);
						return parent.build(application);
					}
				};
			}
		};

		builderFactories
				.put(OMRdfSymbols.VALUE, valueAndValuesetBuilderFactory);
		builderFactories.put(OMRdfSymbols.VALUESET,
				valueAndValuesetBuilderFactory);

		builderFactories.put(OMRdfSymbols.RESOURCESET, new BuilderFactory() {
			@Override
			public <E> SeqBuilder<E> create(final NWMathBuilderBase<E> parent,
					final URI symbol) {
				return new NWMathSeqBuilder<E>(parent.context) {
					String classDescription;

					@Override
					public SeqBuilder<E> str(String value) {
						classDescription = value;
						return this;
					}

					@Override
					public E end() {
						ValueConverter converter = new ValueConverter(
								getEntityManager(), new INamespaces() {
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
						StatementConverterActions actions = new StatementConverterActions(
								converter);

						ManchesterSyntaxParser manchesterParser = Parboiled
								.createParser(ManchesterSyntaxParser.class,
										actions);
						ParsingResult<Object> result = new BasicParseRunner<Object>(
								manchesterParser.Description())
								.run(classDescription);

						Application application = createApplication(symbol);
						application.setArguments(Arrays
								.asList((IReference) converter
										.toValue(result.resultValue)));
						getEntityManager().add(actions.getResult());
						return parent.build(application);
					}
				};
			}
		});
	}

	protected URI toUri(Context context, String uriOrPrefixedName) {
		if (uriOrPrefixedName.startsWith("<")) {
			return URIs.createURI(uriOrPrefixedName.substring(1,
					uriOrPrefixedName.length() - 1));
		}
		int colonIndex = uriOrPrefixedName.indexOf(':');
		String prefix, localPart;
		if (colonIndex >= 0) {
			prefix = uriOrPrefixedName.substring(0, colonIndex);
			localPart = uriOrPrefixedName.substring(colonIndex + 1,
					uriOrPrefixedName.length());
		} else {
			prefix = "";
			localPart = uriOrPrefixedName;
		}

		URI nsUri = context.getURI(prefix);

		if (nsUri != null) {
			return nsUri.appendLocalPart(localPart);
		} else {
			throw new IllegalArgumentException("Unknown prefix: \"" + prefix
					+ "\"");
		}

	}

	public NWMathBuilderBase(IEntityManager em, INamespaces ns) {
		this(new Context(em, ns));
	}

	public NWMathBuilderBase(Context context) {
		this.context = context;
	}

	public Context getContext() {
		return context;
	}

	protected IEntityManager getEntityManager() {
		return context.em;
	}

	protected URI nextId;

	/**
	 * Creates named or anonymous object instances.
	 * 
	 * @param type
	 * @return
	 */
	protected <E> E create(Class<E> type) {
		if (nextId != null) {
			URI id = nextId;
			nextId = null;
			return getEntityManager().createNamed(id, type);
		} else {
			return getEntityManager().create(type);
		}
	}

	protected Application createApplication(URI symbol) {
		Application application = create(Application.class);
		application.setOperator(getEntityManager().find(symbol, Symbol.class));
		return application;
	}

	public T build(IReference obj) {
		return null;
	}

	@Override
	public T s(URI symbol) {
		return build(getEntityManager().find(symbol));
	}

	@Override
	public T var(String variableName) {
		Variable variable = create(Variable.class);
		variable.setName(variableName);
		return build(variable);
	}

	@Override
	public T i(BigInteger value) {
		Literal literal = create(Literal.class);
		literal.setValue(getEntityManager().createLiteral(value,
				XMLSCHEMA.TYPE_INTEGER));
		return build(literal);
	}

	@Override
	public Builder<T> id(URI id) {
		nextId = id;
		return this;
	}

	@Override
	public T b(String base64Binary) {
		Literal literal = create(Literal.class);
		literal.setValue(getEntityManager().createLiteral(base64Binary,
				XMLSCHEMA.TYPE_BASE64BINARY));
		return build(literal);
	}

	@Override
	public T str(String value) {
		Literal literal = create(Literal.class);
		literal.setValue(getEntityManager().createLiteral(value,
				XMLSCHEMA.TYPE_STRING));
		return build(literal);
	}

	@Override
	public T f(double value) {
		Literal literal = create(Literal.class);
		literal.setValue(getEntityManager().createLiteral(value,
				XMLSCHEMA.TYPE_DOUBLE));
		return build(literal);
	}

	protected BuilderFactory getBuilderFactory(URI symbol) {
		return builderFactories.get(symbol);
	}

	@Override
	public SeqBuilder<T> apply() {
		final NWMathBuilderBase<T> parent = this;
		final DelegatingSeqBuilder<T> delegatingBuilder = new DelegatingSeqBuilder<T>();
		delegatingBuilder.delegate(new NWMathSeqBuilder<T>(context) {
			Application application = null;
			List<IReference> args = null;

			@Override
			public SeqBuilder<T> s(URI symbol) {
				if (application == null) {
					BuilderFactory factory = NWMathBuilderBase.this
							.getBuilderFactory(symbol);
					if (factory != null) {
						delegatingBuilder.delegate(factory.create(parent,
								symbol));
						return delegatingBuilder;
					}
				}
				return super.s(symbol);
			}

			@Override
			public SeqBuilder<T> build(IReference expr) {
				if (application == null) {
					application = create(Application.class);
					application.setOperator(expr);
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
				application.setArguments(args);
				return parent.build(application);
			}
		});
		return delegatingBuilder;
	}

	@Override
	public BindingBuilder<T> bind() {
		return new BindingBuilder<T>() {
			final BindingBuilder<T> self = this;
			IReference binder, body;
			List<Variable> variables = new ArrayList<Variable>();

			@Override
			public Builder<BindingBuilder<T>> binder() {
				return new NWMathBuilderBase<BindingBuilder<T>>(context) {
					@Override
					public BindingBuilder<T> build(IReference obj) {
						binder = obj;
						return self;
					}
				};
			}

			@Override
			public Builder<BindingBuilder<T>> body() {
				return new NWMathBuilderBase<BindingBuilder<T>>(context) {
					@Override
					public BindingBuilder<T> build(IReference obj) {
						body = obj;
						return self;
					}
				};
			}

			@Override
			public T end() {
				Binding binding = create(Binding.class);
				binding.setBinder(binder);
				binding.setVariables(variables);
				binding.setBody(body);
				return build(binding);
			}

			@Override
			public VariablesBuilder<BindingBuilder<T>> variables() {
				return new NWMathVarBuilder<BindingBuilder<T>>(context) {
					@Override
					public VariablesBuilder<BindingBuilder<T>> build(
							IReference obj) {
						if (obj instanceof Variable) {
							variables.add((Variable) obj);
						}
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
		final NWMathBuilderBase<T> parent = this;
		final Error error = create(Error.class);
		error.setSymbol(getEntityManager().find(symbol, Symbol.class));
		return new NWMathSeqBuilder<T>(context) {
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
				error.setArguments(args);
				return parent.build(error);
			}
		};
	}

	@Override
	public Builder<Builder<T>> attr(final URI symbol) {
		final NWMathBuilderBase<T> parent = this;
		if (OMRdfSymbols.PREFIXES.equals(symbol)) {
			final List<Namespace> namespaces = new ArrayList<>();
			return new NWMathBuilderBase<Builder<T>>(context) {
				@Override
				public SeqBuilder<Builder<T>> apply() {
					return new NamespaceBuilder<Builder<T>>(context, namespaces) {
						boolean isSet = false;

						public SeqBuilder<Builder<T>> s(URI symbol) {
							if ("http://www.openmath.org/cd/set1#set"
									.equals(symbol.toString())) {
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
							return new NamespaceBuilder<SeqBuilder<Builder<T>>>(
									context, namespaces) {
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
									: new NWMathBuilderBase<T>(
											context.childContext(namespaces)) {
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

		final List<AttributionPair> pairs = new ArrayList<AttributionPair>();
		return new NWMathBuilderBase<Builder<T>>(context) {
			Builder<Builder<T>> self = this;
			URI currentKey = symbol;

			protected void addPair(IReference obj) {
				// store key-value pair
				AttributionPair pair = getEntityManager().create(
						AttributionPair.class);
				pair.setAttributeKey(getEntityManager().createNamed(currentKey,
						Symbol.class));
				pair.setAttributeValue(obj);
				pairs.add(pair);
			}

			@Override
			public Builder<T> build(IReference obj) {
				addPair(obj);
				return new NWMathBuilderBase<T>(context) {
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
						Attribution attribution = create(Attribution.class);
						attribution.setTarget(target);
						attribution.setArguments(pairs);
						return parent.build(attribution);
					}
				};
			}
		};
	}

	@Override
	public T foreign(String encoding, String content) {
		Foreign foreign = create(Foreign.class);
		foreign.setEncoding(encoding);
		foreign.setValue(content);
		return build(foreign);
	}

	@Override
	public T ref(IReference reference) {
		// create a reference object if the new object should get a URI
		if (nextId != null) {
			Reference refObj = create(Reference.class);
			refObj.setTarget(reference);
			reference = refObj;
		}
		return build(getEntityManager().find(reference));
	}

	@Override
	public T rdfClass(IReference reference, INamespaces ns) {
		return build(getEntityManager().find(reference));
	}

}
package org.numerateweb.math.popcorn;

import static org.numerateweb.math.popcorn.PopcornSymbols.symbol;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.numerateweb.math.model.Builder;
import org.numerateweb.math.model.LiteralBuilder;
import org.numerateweb.math.ns.INamespaces;

import net.enilink.komma.core.IReference;
import net.enilink.komma.core.URI;

public abstract class PopcornBuilderBase<T> implements Builder<T> {
	protected static final URI LAMBDA = symbol("fns1", "lambda");

	abstract static class PopcornVarBuilder<B> extends
			PopcornBuilderBase<VariablesBuilder<B>> implements
			VariablesBuilder<B> {
		PopcornVarBuilder(LiteralBuilder<PopcornExpr> literalBuilder) {
			super(literalBuilder);
		}

		@Override
		public Builder<VariablesBuilder<B>> attrVar(final URI symbol) {
			final PopcornVarBuilder<B> parent = this;
			final StringBuilder attributes = new StringBuilder();
			return new PopcornBuilderBase<VariablesBuilder<B>>(literalBuilder) {
				Builder<VariablesBuilder<B>> self = this;

				@Override
				protected VariablesBuilder<B> build(PopcornExpr expr) {
					if (attributes.length() > 0) {
						attributes.append(", ");
					}
					// create key-value pair
					PopcornExpr key = literalBuilder.s(symbol);
					attributes.append(key.toString()).append(" -> ")
							.append(expr);
					return new PopcornVarBuilder<B>(literalBuilder) {
						@Override
						public Builder<VariablesBuilder<B>> attrVar(URI symbol) {
							return self;
						}

						@Override
						protected VariablesBuilder<B> build(PopcornExpr var) {
							return parent.build(new PopcornExpr(var + "{"
									+ attributes + "}"));
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

	abstract static class PopcornSeqBuilder<B> extends
			PopcornBuilderBase<SeqBuilder<B>> implements SeqBuilder<B> {
		List<PopcornExpr> objects = new ArrayList<PopcornExpr>();

		PopcornSeqBuilder(LiteralBuilder<PopcornExpr> literalBuilder) {
			super(literalBuilder);
		}
	}

	protected LiteralBuilder<PopcornExpr> literalBuilder;

	interface Converter {
		PopcornExpr convert(List<PopcornExpr> args);
	}

	static Map<URI, Converter> symbolConverters = new HashMap<>();

	static {
		symbolConverters.put(symbol("list1", "list"), new Converter() {
			@Override
			public PopcornExpr convert(List<PopcornExpr> args) {
				return new PopcornExpr("[" + list(args.subList(1, args.size()))
						+ "]");
			}
		});

		symbolConverters.put(symbol("set1", "set"), new Converter() {
			@Override
			public PopcornExpr convert(List<PopcornExpr> args) {
				return new PopcornExpr("{" + list(args.subList(1, args.size()))
						+ "}");
			}
		});
		symbolConverters.put(symbol("prog1", "if"), new Converter() {
			@Override
			public PopcornExpr convert(List<PopcornExpr> args) {
				Iterator<PopcornExpr> it = args.iterator();
				it.next();
				return new PopcornExpr("if " + it.next() + " then " + it.next()
						+ " else " + it.next() + " end");
			}
		});
		symbolConverters.put(symbol("prog1", "while"), new Converter() {
			@Override
			public PopcornExpr convert(List<PopcornExpr> args) {
				Iterator<PopcornExpr> it = args.iterator();
				it.next();
				return new PopcornExpr("while " + it.next() + " do "
						+ it.next() + " end");
			}
		});

		// RDF support
		symbolConverters.put(symbol("rdf", "value"), new Converter() {
			@Override
			public PopcornExpr convert(List<PopcornExpr> args) {
				Iterator<PopcornExpr> it = args.iterator();
				it.next();
				return new PopcornExpr("@" + stripQuots(it.next().toString())
						+ (it.hasNext() ? "(" + it.next() + ")" : ""));
			}
		});
		symbolConverters.put(symbol("rdf", "valueset"), new Converter() {
			@Override
			public PopcornExpr convert(List<PopcornExpr> args) {
				Iterator<PopcornExpr> it = args.iterator();
				it.next();
				return new PopcornExpr("@@" + stripQuots(it.next().toString())
						+ (it.hasNext() ? "(" + it.next() + ")" : ""));
			}
		});
		symbolConverters.put(symbol("rdf", "resource"), new Converter() {
			@Override
			public PopcornExpr convert(List<PopcornExpr> args) {
				Iterator<PopcornExpr> it = args.iterator();
				it.next();
				String resourceName = stripQuots(it.next().toString());
				return new PopcornExpr("@(" + resourceName + ")");
			}
		});
		symbolConverters.put(symbol("rdf", "resourceset"), new Converter() {
			@Override
			public PopcornExpr convert(List<PopcornExpr> args) {
				Iterator<PopcornExpr> it = args.iterator();
				it.next();
				return new PopcornExpr("@@[" + stripQuots(it.next().toString())
						+ "]");
			}
		});
		symbolConverters.put(symbol("set1", "intersect"), new Converter() {
			@Override
			public PopcornExpr convert(List<PopcornExpr> args) {
				// (set1.intersect, arg1, arg2)
				if (args.size() == 3) {
					PopcornExpr arg1 = args.get(1);
					PopcornExpr arg2 = args.get(2);
					if (arg1.toString().matches("^@@[^\\[].*")
							&& arg2.toString().startsWith("@@[")) {
						return new PopcornExpr(arg1
								+ arg2.toString().substring(2));
					}
				}
				return null;
			}
		});
	}

	public PopcornBuilderBase(INamespaces ns) {
		this.literalBuilder = newLiteralBuilder(ns);
	}

	protected LiteralBuilder<PopcornExpr> newLiteralBuilder(INamespaces ns) {
		return new PopcornLiteralBuilder(ns);
	}

	public PopcornBuilderBase(LiteralBuilder<PopcornExpr> literalBuilder) {
		this.literalBuilder = literalBuilder;
	}

	abstract protected T build(PopcornExpr expr);

	protected static String stripQuots(String omstr) {
		if (omstr.startsWith("\"") && omstr.endsWith("\"")) {
			return omstr.substring(1, omstr.length() - 1);
		}
		return omstr;
	}

	protected static String list(List<?> list) {
		return list(list, ", ");
	}

	protected static String list(List<?> list, String separator) {
		StringBuilder sb = new StringBuilder();
		Iterator<?> it = list.iterator();
		while (it.hasNext()) {
			sb.append(it.next().toString());
			if (it.hasNext()) {
				sb.append(separator);
			}
		}
		return sb.toString();
	}

	@Override
	public T s(URI symbol) {
		return build(literalBuilder.s(symbol));
	}

	@Override
	public T var(String variableName) {
		return build(literalBuilder.var(variableName));
	}

	@Override
	public T i(BigInteger value) {
		return build(literalBuilder.i(value));
	}

	@Override
	public Builder<T> id(URI id) {
		// ignore ids
		return this;
	}

	@Override
	public T b(String base64Binary) {
		return build(literalBuilder.b(base64Binary));
	}

	@Override
	public T str(String value) {
		return build(literalBuilder.str(value));
	}

	@Override
	public T f(double value) {
		return build(literalBuilder.f(value));
	}

	@Override
	public SeqBuilder<T> apply() {
		final PopcornBuilderBase<T> parent = this;
		return new PopcornSeqBuilder<T>(literalBuilder) {
			@Override
			protected SeqBuilder<T> build(PopcornExpr expr) {
				objects.add(expr);
				return this;
			}

			boolean sameSymbol(PopcornExpr e1, PopcornExpr e2) {
				if (e1.symbol == null) {
					return e2.symbol == null;
				}
				return e1.symbol.equals(e2.symbol);
			}

			@Override
			public T end() {
				PopcornExpr head = objects.get(0);

				Converter converter = symbolConverters.get(head.symbol);
				if (converter != null) {
					PopcornExpr result = converter.convert(objects);
					// converter may return null, if it cannot handle this
					// specific application
					if (result != null) {
						return parent.build(result);
					}
				}

				List<PopcornExpr> argsTail = objects.subList(1, objects.size());
				if (head.isInfixSymbol) {
					StringBuilder sb = new StringBuilder();
					Iterator<PopcornExpr> it = argsTail.iterator();
					while (it.hasNext()) {
						PopcornExpr arg = it.next();
						boolean parens = arg.priority > head.priority
								|| sameSymbol(arg, head)
								&& arg.priority == head.priority;
						if (parens) {
							sb.append("(");
						}
						sb.append(arg.toString());
						if (parens) {
							sb.append(")");
						}
						if (it.hasNext()) {
							sb.append(" ").append(head).append(" ");
						}
					}
					return parent.build(new PopcornExpr(head.symbol, sb
							.toString(), head.priority, true));
				}
				return parent.build(new PopcornExpr(head + "(" + list(argsTail)
						+ ")"));
			}
		};
	}

	@Override
	public BindingBuilder<T> bind() {
		return new BindingBuilder<T>() {
			final BindingBuilder<T> self = this;
			PopcornExpr binder, body;
			List<PopcornExpr> variables = null;;

			@Override
			public Builder<BindingBuilder<T>> binder() {
				return new PopcornBuilderBase<BindingBuilder<T>>(literalBuilder) {
					@Override
					protected BindingBuilder<T> build(PopcornExpr expr) {
						binder = expr;
						return self;
					}
				};
			}

			@Override
			public VariablesBuilder<BindingBuilder<T>> variables() {
				return new PopcornVarBuilder<BindingBuilder<T>>(literalBuilder) {
					@Override
					protected VariablesBuilder<BindingBuilder<T>> build(
							PopcornExpr expr) {
						if (variables == null) {
							variables = new ArrayList<PopcornExpr>();
						}
						variables.add(expr);
						return this;
					}

					@Override
					public BindingBuilder<T> end() {
						return self;
					}
				};
			}

			@Override
			public Builder<BindingBuilder<T>> body() {
				final BindingBuilder<T> bindBuilder = this;
				return new PopcornBuilderBase<BindingBuilder<T>>(literalBuilder) {
					@Override
					protected BindingBuilder<T> build(PopcornExpr expr) {
						body = expr;
						return bindBuilder;
					}
				};
			}

			@Override
			public T end() {
				if (LAMBDA.equals(binder.symbol)) {
					// use compact lambda notations
					return build(new PopcornExpr(list(variables) + " -> "
							+ body));
				}
				return build(new PopcornExpr(binder + "[" + list(variables)
						+ " -> " + body + "]"));
			}
		};
	}

	@Override
	public SeqBuilder<T> error(URI symbol) {
		final PopcornBuilderBase<T> parent = this;
		return new PopcornSeqBuilder<T>(literalBuilder) {
			@Override
			protected SeqBuilder<T> build(PopcornExpr expr) {
				objects.add(expr);
				return this;
			}

			@Override
			public T end() {
				return parent.build(new PopcornExpr("/* Error: "
						+ objects.get(0).toString()
						+ (objects.size() == 1 ? "" : "\n"
								+ list(objects.subList(1, objects.size())))
						+ " */"));
			}
		}.s(symbol);
	}

	@Override
	public Builder<Builder<T>> attr(final URI symbol) {
		final PopcornBuilderBase<T> parent = this;
		final StringBuilder attributes = new StringBuilder();
		return new PopcornBuilderBase<Builder<T>>(literalBuilder) {
			Builder<Builder<T>> self = this;

			@Override
			protected Builder<T> build(PopcornExpr expr) {
				if (attributes.length() > 0) {
					attributes.append(", ");
				}
				// create key-value pair
				PopcornExpr key = literalBuilder.s(symbol);
				attributes.append(key.toString()).append(" -> ").append(expr);
				return new PopcornBuilderBase<T>(literalBuilder) {
					@Override
					public Builder<Builder<T>> attr(URI symbol) {
						return self;
					}

					@Override
					protected T build(PopcornExpr target) {
						return parent.build(new PopcornExpr(target + "{"
								+ attributes + "}"));
					}
				};
			}
		};
	}

	@Override
	public T ref(IReference reference) {
		return build(literalBuilder.ref(reference));
	}

	@Override
	public T foreign(String encoding, String content) {
		return build(literalBuilder.foreign(encoding, content));
	}

	@Override
	public T rdfClass(IReference reference, INamespaces ns) {
		return build(literalBuilder.rdfClass(reference, ns));
	}

}

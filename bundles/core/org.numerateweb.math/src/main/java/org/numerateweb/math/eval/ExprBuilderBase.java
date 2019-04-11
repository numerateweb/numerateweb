package org.numerateweb.math.eval;

import static org.numerateweb.math.eval.Helpers.valueToStream;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.numerateweb.math.eval.expr.AttributedExpr;
import org.numerateweb.math.eval.expr.BindingExpr;
import org.numerateweb.math.eval.expr.ConstantExpr;
import org.numerateweb.math.eval.expr.ConstructExpr;
import org.numerateweb.math.eval.expr.EvalWithRestriction;
import org.numerateweb.math.eval.expr.Expr;
import org.numerateweb.math.eval.expr.FunctionExpr;
import org.numerateweb.math.eval.expr.IfElseExpr;
import org.numerateweb.math.eval.expr.MapOperatorExpr;
import org.numerateweb.math.eval.expr.ResourceSetExpr;
import org.numerateweb.math.eval.expr.SetOperatorExpr;
import org.numerateweb.math.eval.expr.SymbolExpr;
import org.numerateweb.math.eval.expr.ValueExpr;
import org.numerateweb.math.eval.expr.ValueSetExpr;
import org.numerateweb.math.eval.expr.VarExpr;
import org.numerateweb.math.model.Builder;
import org.numerateweb.math.ns.INamespaces;

import net.enilink.commons.util.ValueUtils;
import net.enilink.komma.core.IReference;
import net.enilink.komma.core.URI;
import net.enilink.vocab.owl.OWL;

abstract class ExprBuilderBase<T> implements Builder<T> {
	abstract static class ExprVarBuilder<B> extends ExprBuilderBase<VariablesBuilder<B>>
			implements VariablesBuilder<B> {
		@Override
		public Builder<VariablesBuilder<B>> attrVar(URI symbol) {
			// attributions are simply ignored
			final ExprVarBuilder<B> parent = this;
			return new ExprBuilderBase<VariablesBuilder<B>>() {
				@Override
				protected VariablesBuilder<B> build(Expr var) {
					return parent.build(var);
				}
			};
		}
	}

	abstract static class ExprSeqBuilder<B> extends ExprBuilderBase<SeqBuilder<B>> implements SeqBuilder<B> {
		List<Expr> objects = new ArrayList<Expr>();
	}

	interface Converter {
		Expr convert(List<Expr> args);
	}

	static final Map<String, Converter> symbolConverters = new HashMap<>();
	static final String CDBASE = Expressions.CDBASE;

	static {
		symbolConverters.put(CDBASE + "/prog1#assignment", new Converter() {
			@Override
			public Expr convert(List<Expr> args) {
				if (args.get(0) instanceof VarExpr) {
					VarExpr varExpr = (VarExpr) args.get(0);
					Expr valueExpr = args.get(1);
					return new Expr() {
						@Override
						public Object eval() {
							// set variable value
							String varName = varExpr.name();
							return Expressions.setVar(varName, valueExpr.eval());
						}
					};
				} else {
					throw new RuntimeException("Expected variable as first argument instead of " + args.get(0));
				}
			}
		});

		symbolConverters.put(CDBASE + "/arith1#intersect", new Converter() {
			@Override
			public Expr convert(List<Expr> args) {
				if (args.size() == 2 && args.get(0) instanceof EvalWithRestriction) {
					return new Expr() {
						final Expr restrictableArg = args.get(0);
						final Expr restrictionArg = args.get(1);

						@Override
						public Object eval() {
							Object restriction = restrictionArg.eval();
							if (restriction instanceof IReference) {
								return ((EvalWithRestriction) restrictableArg)
										.evalWithRestriction(Optional.of((IReference) restriction));
							} else {
								// handle as simple intersection of two sets
								return Stream.of(restrictableArg.eval(), restriction)
										.flatMap(v -> valueToStream(v)).distinct()
										.collect(Collectors.toList());
							}
						}
					};
				} else {
					// use default function handler
					return null;
				}
			}
		});

		symbolConverters.put(CDBASE + "/rdf#resourceset", new Converter() {
			@Override
			public Expr convert(List<Expr> args) {
				switch (args.size()) {
				case 0:
					return new ResourceSetExpr(OWL.TYPE_THING);
				case 1:
					// statically evaluated beforehand
					return new ResourceSetExpr((IReference) args.get(0).eval());
				default:
					throw new IllegalArgumentException("Expected zero or one argument for rdf.resourceset instead of "
							+ args.size() + " arguments.");

				}
			}
		});

		symbolConverters.put(CDBASE + "/rdf#valueset", new Converter() {
			@Override
			public Expr convert(List<Expr> args) {
				switch (args.size()) {
				case 1:
					// property expression statically evaluated beforehand
					return new ValueSetExpr((IReference) args.get(0).eval(), Optional.empty());
				case 2:
					// property expression statically evaluated beforehand
					return new ValueSetExpr((IReference) args.get(0).eval(), Optional.of(args.get(1)));
				default:
					throw new IllegalArgumentException(
							"Expected one or two arguments for rdf.valueset instead of " + args.size() + " arguments.");

				}
			}
		});

		symbolConverters.put(CDBASE + "/rdf#value", new Converter() {
			@Override
			public Expr convert(List<Expr> args) {
				switch (args.size()) {
				case 1:
					// property expression statically evaluated beforehand
					return new ValueExpr((IReference) args.get(0).eval(), Optional.empty());
				case 2:
					// property expression statically evaluated beforehand
					return new ValueExpr((IReference) args.get(0).eval(), Optional.of(args.get(1)));
				default:
					throw new IllegalArgumentException(
							"Expected one or two arguments for rdf.value instead of " + args.size() + " arguments.");
				}
			}
		});

		symbolConverters.put(CDBASE + "/arith1#sum", new Converter() {
			@Override
			public Expr convert(List<Expr> args) {
				ValueUtils values = ValueUtils.getInstance();
				return new SetOperatorExpr((a, b) -> {
					return values.add(a, b);
				}, 0, args);
			}

		});

		symbolConverters.put(CDBASE + "/arith1#product", new Converter() {
			@Override
			public Expr convert(List<Expr> args) {
				ValueUtils values = ValueUtils.getInstance();
				return new SetOperatorExpr((a, b) -> {
					return values.multiply(a, b);
				}, 1, args);
			}
		});

		symbolConverters.put(CDBASE + "/list1#map", new Converter() {
			@Override
			public Expr convert(List<Expr> args) {
				return new MapOperatorExpr(args);
			}

		});

		symbolConverters.put(CDBASE + "/prog1#if", new Converter() {
			@Override
			public Expr convert(List<Expr> args) {
				return new IfElseExpr(args);
			}
		});

		symbolConverters.put(CDBASE + "/ctor1#generate", new Converter() {
			@Override
			public Expr convert(List<Expr> args) {
				return new ConstructExpr(args);
			}
		});

		symbolConverters.put(CDBASE + "/ctor1#new", new Converter() {
			@Override
			public Expr convert(List<Expr> args) {
				return new ConstructExpr(args);
			}
		});
	}

	abstract protected T build(Expr expr);

	@Override
	public SeqBuilder<T> apply() {
		final ExprBuilderBase<T> parent = this;
		return new ExprSeqBuilder<T>() {
			@Override
			protected SeqBuilder<T> build(Expr expr) {
				objects.add(expr);
				return this;
			}

			@Override
			public T end() {
				Expr head = objects.get(0);
				objects.remove(0);

				String symbolUri = head.toString();
				Converter converter = symbolConverters.get(symbolUri);
				if (converter != null) {
					Expr result = converter.convert(objects);
					// converter may return null, if it cannot handle this
					// specific application
					if (result != null) {
						return parent.build(result);
					}
				}

				Function<Object, Object> function = Expressions.functions.get(symbolUri);
				if (function != null) {
					Expr result = new FunctionExpr(function, objects);
					return parent.build(result);
				}

				throw new IllegalArgumentException("Unsupported symbol " + head);
			}
		};
	}

	@Override
	public T var(String variableName) {
		return build(new VarExpr(variableName));
	}

	@Override
	public T i(BigInteger value) {
		return build(new ConstantExpr(value));
	}

	@Override
	public Builder<T> id(URI id) {
		// ignore ids
		return this;
	}

	@Override
	public T f(double value) {
		return build(new ConstantExpr(value));
	}

	@Override
	public T str(String value) {
		return build(new ConstantExpr(value));
	}

	@Override
	public T s(URI symbol) {
		Expr constantExpr = Expressions.constants.get(symbol.toString());
		if (constantExpr != null) {
			return build(constantExpr);
		}
		return build(new SymbolExpr(symbol));
	}

	@Override
	public T b(String base64Binary) {
		throw new UnsupportedOperationException();
	}

	@Override
	public BindingBuilder<T> bind() {
		return new BindingBuilder<T>() {
			final BindingBuilder<T> self = this;
			Expr binder, body;
			List<Expr> variables = new ArrayList<Expr>();

			@Override
			public Builder<BindingBuilder<T>> binder() {
				return new ExprBuilderBase<BindingBuilder<T>>() {
					@Override
					protected BindingBuilder<T> build(Expr obj) {
						binder = obj;
						return self;
					}
				};
			}

			@Override
			public VariablesBuilder<BindingBuilder<T>> variables() {
				return new ExprVarBuilder<BindingBuilder<T>>() {
					@Override
					protected VariablesBuilder<BindingBuilder<T>> build(Expr obj) {
						variables.add(obj);
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
				return new ExprBuilderBase<BindingBuilder<T>>() {
					@Override
					protected BindingBuilder<T> build(Expr obj) {
						body = obj;
						return self;
					}
				};
			}

			@Override
			public T end() {
				return build(new BindingExpr(binder, variables, body));
			}
		};
	}

	@Override
	public SeqBuilder<T> error(URI symbol) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Builder<Builder<T>> attr(URI symbol) {
		final ExprBuilderBase<T> parent = this;
		final Map<URI, Expr> attributes = new HashMap<>();
		return new ExprBuilderBase<Builder<T>>() {
			Builder<Builder<T>> self = this;
			URI currentSymbol = symbol;

			@Override
			protected Builder<T> build(Expr expr) {
				attributes.put(currentSymbol, expr);

				return new ExprBuilderBase<T>() {
					@Override
					public Builder<Builder<T>> attr(URI symbol) {
						currentSymbol = symbol;
						return self;
					}

					@Override
					protected T build(Expr target) {
						return parent.build(new AttributedExpr(target, attributes));
					}
				};
			}
		};
	}

	@Override
	public T ref(IReference reference) {
		return build(new ConstantExpr(reference));
	}

	@Override
	public T foreign(String encoding, String content) {
		throw new UnsupportedOperationException();
	}

	@Override
	public T rdfClass(IReference reference, INamespaces ns) {
		return build(new ConstantExpr(reference));
	}
}
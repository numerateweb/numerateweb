package org.numerateweb.math.model.factory;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.numerateweb.math.model.Builder;
import org.numerateweb.math.model.OMObject.Type;
import org.numerateweb.math.ns.INamespaces;

import net.enilink.komma.core.IReference;
import net.enilink.komma.core.URI;

/**
 * Builder that delegates to a {@link ValueFactory}.
 * 
 * @param <T>
 *            Element type of this builder. This can be either a mathematical
 *            object type or another builder type.
 * @param <E>
 *            Element type produced by the given {@link ValueFactory}.
 */
public abstract class VFBuilder<T, E> implements Builder<T> {
	public abstract static class VFVarBuilder<B, E> extends
			VFBuilder<VariablesBuilder<B>, E> implements VariablesBuilder<B> {
		public VFVarBuilder(ValueFactory<E> vf) {
			super(vf);
		}

		@Override
		public Builder<VariablesBuilder<B>> attrVar(final URI symbol) {
			final VFVarBuilder<B, E> parent = this;
			final List<E> attributes = new ArrayList<E>();
			return new VFBuilder<VariablesBuilder<B>, E>(vf) {
				Builder<VariablesBuilder<B>> self = this;

				@Override
				protected VariablesBuilder<B> build(E obj) {
					// store key-value pair
					attributes.add(vf.createOMS(symbol));
					attributes.add(obj);
					return new VFVarBuilder<B, E>(vf) {
						@Override
						public Builder<VariablesBuilder<B>> attrVar(URI symbol) {
							return self;
						}

						@Override
						protected VariablesBuilder<B> build(E var) {
							return parent.build(vf
									.createOMATTR(attributes, var));
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

	public abstract static class VFSeqBuilder<B, E> extends
			VFBuilder<SeqBuilder<B>, E> implements SeqBuilder<B> {
		public VFSeqBuilder(ValueFactory<E> vf) {
			super(vf);
		}
	}

	protected ValueFactory<E> vf;

	public VFBuilder(ValueFactory<E> vf) {
		this.vf = vf;
	}

	abstract protected T build(E obj);

	@Override
	public T s(URI symbol) {
		return build(vf.createOMS(symbol));
	}

	@Override
	public T var(String variableName) {
		return build(vf.createOMV(variableName));
	}

	@Override
	public T i(BigInteger value) {
		return build(vf.createOMI(value));
	}

	@Override
	public Builder<T> id(URI id) {
		// ignore ids
		return this;
	}

	@Override
	public T b(String base64Binary) {
		return build(vf.createOMB(base64Binary));
	}

	@Override
	public T str(String value) {
		return build(vf.createOMSTR(value));
	}

	@Override
	public T f(double value) {
		return build(vf.createOMF(value));
	}

	protected SeqBuilder<T> argumentsBuilder(final Type type) {
		final VFBuilder<T, E> parent = this;
		return new VFSeqBuilder<T, E>(vf) {
			List<E> objects = new ArrayList<E>();

			@Override
			protected SeqBuilder<T> build(E obj) {
				objects.add(obj);
				return this;
			}

			@Override
			public T end() {
				E obj;
				switch (type) {
				case OMA:
					obj = vf.createOMA(objects);
					break;
				case OME:
					obj = vf.createOME(objects.get(0),
							objects.subList(1, objects.size()));
					break;
				default:
					throw new IllegalArgumentException();
				}
				return parent.build(obj);
			}
		};
	}

	@Override
	public SeqBuilder<T> apply() {
		return argumentsBuilder(Type.OMA);
	}

	@Override
	public BindingBuilder<T> bind() {
		return new BindingBuilder<T>() {
			final BindingBuilder<T> self = this;
			E binder, body;
			List<E> variables = new ArrayList<E>();

			@Override
			public Builder<BindingBuilder<T>> binder() {
				return new VFBuilder<BindingBuilder<T>, E>(vf) {
					@Override
					protected BindingBuilder<T> build(E obj) {
						binder = obj;
						return self;
					}
				};
			}

			@Override
			public VariablesBuilder<BindingBuilder<T>> variables() {
				return new VFVarBuilder<BindingBuilder<T>, E>(vf) {
					@Override
					protected VariablesBuilder<BindingBuilder<T>> build(E obj) {
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
				return new VFBuilder<BindingBuilder<T>, E>(vf) {
					@Override
					protected BindingBuilder<T> build(E obj) {
						body = obj;
						return self;
					}
				};
			}

			@Override
			public T end() {
				return build(vf.createOMBIND(binder, variables, body));
			}
		};
	}

	@Override
	public SeqBuilder<T> error(URI symbol) {
		return argumentsBuilder(Type.OME).s(symbol);
	}

	@Override
	public Builder<Builder<T>> attr(URI symbol) {
		final VFBuilder<T, E> parent = this;
		final List<E> attributes = new ArrayList<E>();
		attributes.add(vf.createOMS(symbol));
		return new VFBuilder<Builder<T>, E>(vf) {
			Builder<Builder<T>> self = this;

			@Override
			protected Builder<T> build(E obj) {
				attributes.add(obj);
				return new VFBuilder<T, E>(vf) {
					@Override
					public Builder<Builder<T>> attr(URI symbol) {
						attributes.add(vf.createOMS(symbol));
						return self;
					}

					@Override
					protected T build(E target) {
						return parent
								.build(vf.createOMATTR(attributes, target));
					}
				};
			}
		};
	}

	@Override
	public T ref(IReference reference) {
		return build(vf.createOMR(reference));
	}

	@Override
	public T foreign(String encoding, String content) {
		return build(vf.createOMFOREIGN(encoding, content));
	}

	@Override
	public T rdfClass(IReference reference, INamespaces ns) {
		return build(vf.createRDFClass(reference, ns));
	}
	
}

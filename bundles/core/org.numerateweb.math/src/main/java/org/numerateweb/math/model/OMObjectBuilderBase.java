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
package org.numerateweb.math.model;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.numerateweb.math.model.OMObject.Type;
import org.numerateweb.math.ns.INamespaces;

import net.enilink.komma.core.IReference;
import net.enilink.komma.core.URI;

public abstract class OMObjectBuilderBase<T> implements Builder<T> {
	public abstract static class OMObjectVarBuilder<B> extends
			OMObjectBuilderBase<VariablesBuilder<B>> implements
			VariablesBuilder<B> {
		@Override
		public Builder<VariablesBuilder<B>> attrVar(URI symbol) {
			final OMObjectVarBuilder<B> parent = this;
			final List<OMObject> attributes = new ArrayList<OMObject>();
			attributes.add(new OMObject(Type.OMS, symbol));
			return new OMObjectBuilderBase<VariablesBuilder<B>>() {
				Builder<VariablesBuilder<B>> self = this;

				@Override
				protected VariablesBuilder<B> build(OMObject obj) {
					attributes.add(obj);
					return new OMObjectVarBuilder<B>() {
						@Override
						public Builder<VariablesBuilder<B>> attrVar(URI symbol) {
							attributes.add(new OMObject(Type.OMS, symbol));
							return self;
						}

						@Override
						protected VariablesBuilder<B> build(OMObject var) {
							return parent.build(create(
									Type.OMATTR,
									new OMObject(Type.OMATP, attributes
											.toArray()), var));
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

	public abstract static class OMObjectSeqBuilder<B> extends
			OMObjectBuilderBase<SeqBuilder<B>> implements SeqBuilder<B> {
	}

	abstract protected T build(OMObject obj);

	protected URI nextId;

	protected OMObject create(Type type, Object... args) {
		URI id = nextId;
		nextId = null;
		return new OMObject(id, type, args);
	}

	@Override
	public T s(URI symbol) {
		return build(create(Type.OMS, symbol));
	}

	@Override
	public T var(String variableName) {
		return build(create(Type.OMV, variableName));
	}

	@Override
	public T i(BigInteger value) {
		return build(create(Type.OMI, value));
	}

	@Override
	public Builder<T> id(URI id) {
		nextId = id;
		return this;
	}

	@Override
	public T b(String base64Binary) {
		return build(create(Type.OMB, base64Binary));
	}

	@Override
	public T str(String value) {
		return build(create(Type.OMSTR, value));
	}

	@Override
	public T f(double value) {
		return build(create(Type.OMF, value));
	}

	protected SeqBuilder<T> argumentsBuilder(final Type type) {
		final OMObjectBuilderBase<T> parent = this;
		return new OMObjectSeqBuilder<T>() {
			List<OMObject> objects = new ArrayList<OMObject>();

			@Override
			protected SeqBuilder<T> build(OMObject obj) {
				objects.add(obj);
				return this;
			}

			@Override
			public T end() {
				return parent.build(create(type, objects.toArray()));
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
			OMObject binder, body;
			List<OMObject> variables = new ArrayList<OMObject>();

			@Override
			public Builder<BindingBuilder<T>> binder() {
				return new OMObjectBuilderBase<BindingBuilder<T>>() {
					@Override
					protected BindingBuilder<T> build(OMObject obj) {
						binder = obj;
						return self;
					}
				};
			}

			@Override
			public VariablesBuilder<BindingBuilder<T>> variables() {
				return new OMObjectVarBuilder<BindingBuilder<T>>() {
					@Override
					protected VariablesBuilder<BindingBuilder<T>> build(
							OMObject obj) {
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
				return new OMObjectBuilderBase<BindingBuilder<T>>() {
					@Override
					protected BindingBuilder<T> build(OMObject obj) {
						body = obj;
						return self;
					}
				};
			}

			@Override
			public T end() {
				return build(create(Type.OMBIND, binder, new OMObject(
						Type.OMBVAR, variables.toArray()), body));
			}
		};
	}

	@Override
	public SeqBuilder<T> error(URI symbol) {
		return argumentsBuilder(Type.OME).s(symbol);
	}

	@Override
	public Builder<Builder<T>> attr(URI symbol) {
		final OMObjectBuilderBase<T> parent = this;
		final List<OMObject> attributes = new ArrayList<OMObject>();
		attributes.add(new OMObject(Type.OMS, symbol));
		return new OMObjectBuilderBase<Builder<T>>() {
			Builder<Builder<T>> self = this;

			@Override
			protected Builder<T> build(OMObject obj) {
				attributes.add(obj);
				return new OMObjectBuilderBase<T>() {
					@Override
					public Builder<Builder<T>> attr(URI symbol) {
						attributes.add(new OMObject(Type.OMS, symbol));
						return self;
					}

					@Override
					protected T build(OMObject target) {
						return parent.build(create(Type.OMATTR, new OMObject(
								Type.OMATP, attributes.toArray()), target));
					}
				};
			}
		};
	}

	@Override
	public T ref(IReference reference) {
		return build(create(Type.OMR, reference));
	}

	@Override
	public T foreign(String encoding, String content) {
		return build(create(Type.OMFOREIGN, encoding, content));
	}

	@Override
	public T rdfClass(IReference reference, INamespaces ns) {
		return build(create(Type.RDF_CLASS, reference, ns));
	}

}
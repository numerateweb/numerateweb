package org.numerateweb.math.xml;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;

import net.enilink.komma.core.IReference;
import net.enilink.komma.core.URI;

import org.numerateweb.math.model.Builder;
import org.numerateweb.math.ns.INamespaces;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

abstract public class OMXmlBuilderBase<T> implements Builder<T> {
	abstract public static class OMXmlVarBuilder<B> extends
			OMXmlBuilderBase<VariablesBuilder<B>> implements
			VariablesBuilder<B> {
		public OMXmlVarBuilder(OMXmlLiteralBuilder literalBuilder) {
			super(literalBuilder);
		}

		@Override
		public Builder<VariablesBuilder<B>> attrVar(final URI symbol) {
			final OMXmlVarBuilder<B> parent = this;
			final Node omatp = literalBuilder.create(OM.OMATP);
			return new OMXmlBuilderBase<VariablesBuilder<B>>(literalBuilder) {
				Builder<VariablesBuilder<B>> self = this;

				@Override
				protected VariablesBuilder<B> build(Node value) {
					// store key-value pair
					omatp.appendChild(literalBuilder.s(symbol));
					omatp.appendChild(value);
					return new OMXmlVarBuilder<B>(literalBuilder) {
						@Override
						public Builder<VariablesBuilder<B>> attrVar(URI symbol) {
							return self;
						}

						@Override
						protected VariablesBuilder<B> build(Node var) {
							Element omattr = literalBuilder.create(OM.OMATTR);
							omattr.appendChild(omatp);
							omattr.appendChild(var);
							return parent.build(omattr);
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

	abstract public static class OMXmlSeqBuilder<B> extends
			OMXmlBuilderBase<SeqBuilder<B>> implements SeqBuilder<B> {
		public OMXmlSeqBuilder(OMXmlLiteralBuilder literalBuilder) {
			super(literalBuilder);
		}
	}

	protected OMXmlLiteralBuilder literalBuilder;

	public OMXmlBuilderBase(DocumentBuilder docBuilder, Document document) {
		this.literalBuilder = newLiteralBuilder(docBuilder, document);
	}

	public OMXmlBuilderBase(OMXmlLiteralBuilder literalBuilder) {
		this.literalBuilder = literalBuilder;
	}

	protected OMXmlLiteralBuilder newLiteralBuilder(DocumentBuilder docBuilder,
			Document document) {
		return new OMXmlLiteralBuilder(docBuilder, document);
	}

	protected void set(Element element, QName attribute, String value) {
		element.setAttributeNS(attribute.getNamespaceURI(),
				attribute.getLocalPart(), value);
	}

	abstract protected T build(Node obj);

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

	protected SeqBuilder<T> argumentsBuilder(final QName type) {
		final OMXmlBuilderBase<T> parent = this;
		return new OMXmlSeqBuilder<T>(literalBuilder) {
			Element oma = literalBuilder.create(type);

			@Override
			protected SeqBuilder<T> build(Node obj) {
				oma.appendChild(obj);
				return this;
			}

			@Override
			public T end() {
				return parent.build(oma);
			}
		};
	}

	@Override
	public SeqBuilder<T> apply() {
		return argumentsBuilder(OM.OMA);
	}

	@Override
	public BindingBuilder<T> bind() {
		return new BindingBuilder<T>() {
			final BindingBuilder<T> self = this;
			Node binder, body;
			List<Node> variables = new ArrayList<Node>();

			@Override
			public Builder<BindingBuilder<T>> binder() {
				return new OMXmlBuilderBase<BindingBuilder<T>>(literalBuilder) {
					@Override
					protected BindingBuilder<T> build(Node obj) {
						binder = obj;
						return self;
					}
				};
			}

			@Override
			public VariablesBuilder<BindingBuilder<T>> variables() {
				return new OMXmlVarBuilder<BindingBuilder<T>>(literalBuilder) {
					@Override
					protected VariablesBuilder<BindingBuilder<T>> build(Node obj) {
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
				return new OMXmlBuilderBase<BindingBuilder<T>>(literalBuilder) {
					@Override
					protected BindingBuilder<T> build(Node obj) {
						body = obj;
						return self;
					}
				};
			}

			@Override
			public T end() {
				Element ombind = literalBuilder.create(OM.OMBIND);
				ombind.appendChild(binder);
				Element ombvar = literalBuilder.create(OM.OMBVAR);
				for (Node var : variables) {
					ombvar.appendChild(var);
				}
				ombind.appendChild(ombvar);
				ombind.appendChild(body);
				return build(ombind);
			}
		};
	}

	@Override
	public SeqBuilder<T> error(URI symbol) {
		return argumentsBuilder(OM.OME).s(symbol);
	}

	@Override
	public Builder<Builder<T>> attr(final URI symbol) {
		final OMXmlBuilderBase<T> parent = this;
		final Node omatp = literalBuilder.create(OM.OMATP);
		return new OMXmlBuilderBase<Builder<T>>(literalBuilder) {
			Builder<Builder<T>> self = this;

			@Override
			protected Builder<T> build(Node value) {
				// store key-value pair
				omatp.appendChild(literalBuilder.s(symbol));
				omatp.appendChild(value);
				return new OMXmlBuilderBase<T>(literalBuilder) {
					@Override
					public Builder<Builder<T>> attr(URI symbol) {
						return self;
					}

					@Override
					protected T build(Node target) {
						Element omattr = literalBuilder.create(OM.OMATTR);
						omattr.appendChild(omatp);
						omattr.appendChild(target);
						return parent.build(omattr);
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

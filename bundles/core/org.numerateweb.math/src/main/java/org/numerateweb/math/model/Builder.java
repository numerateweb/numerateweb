package org.numerateweb.math.model;

import net.enilink.komma.core.URI;

/**
 * Builder interface for the fluent creation of mathematical objects.
 * 
 * @param <T>
 *            Type of the resulting objects. This can be either a mathematical
 *            object type or another builder type.
 */
public interface Builder<T> extends LiteralBuilder<T> {
	public interface BindingBuilder<T> {
		Builder<BindingBuilder<T>> binder();

		Builder<BindingBuilder<T>> body();

		T end();

		VariablesBuilder<BindingBuilder<T>> variables();
	}

	public interface SeqBuilder<T> extends Builder<SeqBuilder<T>> {
		T end();
	}

	public interface VariablesBuilder<T> {
		Builder<VariablesBuilder<T>> attrVar(URI symbol);

		T end();

		VariablesBuilder<T> var(String variableName);
	}
	
	Builder<T> id(URI id);

	SeqBuilder<T> apply();

	Builder<Builder<T>> attr(URI symbol);

	BindingBuilder<T> bind();

	SeqBuilder<T> error(URI symbol);
}
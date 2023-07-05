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
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

import org.numerateweb.math.ns.INamespaces;

import net.enilink.komma.core.IReference;
import net.enilink.komma.core.URI;

public abstract class DelegatingBuilder<T> implements Builder<T> {
	private Builder<T> delegate;

	@Override
	public SeqBuilder<T> apply() {
		return delegate().apply();
	}

	@Override
	public Builder<Builder<T>> attr(URI symbol) {
		return delegate().attr(symbol);
	}

	@Override
	public T b(String base64Binary) {
		return build(delegate().b(base64Binary));
	}

	@Override
	public BindingBuilder<T> bind() {
		return delegate().bind();
	}

	@Override
	public SeqBuilder<T> error(URI symbol) {
		return delegate().error(symbol);
	}

	@Override
	public T f(double value) {
		return build(delegate().f(value));
	}

	@Override
	public T foreign(String encoding, String content) {
		return build(delegate().foreign(encoding, content));
	}

	@Override
	public T i(BigInteger value) {
		return build(delegate().i(value));
	}

	@Override
	public Builder<T> id(URI id) {
		delegate().id(id);
		return this;
	}

	@Override
	public T rdfClass(IReference reference, INamespaces ns) {
		return build(delegate().rdfClass(reference, ns));
	}

	@Override
	public T ref(IReference reference) {
		return build(delegate().ref(reference));
	}

	@Override
	public T s(URI symbol) {
		return build(delegate().s(symbol));
	}

	@Override
	public T str(String value) {
		return build(delegate().str(value));
	}

	@Override
	public T var(String variableName) {
		return build(delegate().var(variableName));
	}

	public Builder<T> delegate() {
		return delegate;
	}

	public void delegate(Builder<T> delegate) {
		this.delegate = delegate;
	}

	@SuppressWarnings("unchecked")
	public T build(T element) {
		// return delegate instead of underlying builder
		if (element instanceof Builder<?>) {
			return (T) this;
		}
		return element;
	}
	
}

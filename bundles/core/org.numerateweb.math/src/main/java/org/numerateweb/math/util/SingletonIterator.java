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
package org.numerateweb.math.util;

import java.util.NoSuchElementException;

import net.enilink.commons.iterator.NiceIterator;

public class SingletonIterator<T> extends NiceIterator<T> {
	protected T value;

	public SingletonIterator(T value) {
		this.value = value;
	}

	@Override
	public boolean hasNext() {
		return value != null;
	}

	@Override
	public T next() {
		if (!hasNext()) {
			throw new NoSuchElementException();
		}
		T result = value;
		value = null;
		return result;
	}

	public static <T> SingletonIterator<T> create(T value) {
		return new SingletonIterator<>(value);
	}
}
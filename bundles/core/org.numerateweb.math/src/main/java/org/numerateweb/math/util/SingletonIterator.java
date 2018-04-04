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
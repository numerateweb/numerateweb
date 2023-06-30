package org.numerateweb.math.reasoner;

public abstract class AbstractCache<K, T> implements ICache<K, T> {
	@Override
	public CacheResult<T> get(K key) {
		return getInternal(key);
	}

	protected abstract CacheResult<T> getInternal(K key);
}

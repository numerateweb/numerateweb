package org.numerateweb.math.reasoner;

public abstract class AbstractCache<K, T> implements ICache<K, T> {
	private long hits = 0;
	private long total = 0;

	@Override
	public CacheResult<T> get(K key) {
		CacheResult<T> value = getInternal(key);
		if (value != null) {
			hits++;
		}
		total++;
		return value;
	}

	protected abstract CacheResult<T> getInternal(K key);

	@Override
	public double getHitrate() {
		return (hits * 1.0) / total;
	}
}

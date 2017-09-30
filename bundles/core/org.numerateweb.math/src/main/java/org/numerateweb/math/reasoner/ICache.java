package org.numerateweb.math.reasoner;

public interface ICache<K, T> {
	void put(K key, T value);

	CacheResult<T> get(K key);

	T remove(K key);

	double getHitrate();

	void clear();
}

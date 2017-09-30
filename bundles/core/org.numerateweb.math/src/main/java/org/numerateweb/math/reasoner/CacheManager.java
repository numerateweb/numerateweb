package org.numerateweb.math.reasoner;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.google.inject.Key;
import com.google.inject.TypeLiteral;

public class CacheManager {
	private ICacheFactory factory;

	public CacheManager(ICacheFactory factory) {
		this.factory = factory;
	}

	Map<Key<?>, ICache<?, ?>> map = new HashMap<>();

	@SuppressWarnings("unchecked")
	public <K, V> ICache<K, V> get(TypeLiteral<ICache<K, V>> literal) {
		Key<ICache<K, V>> key = Key.get(literal);
		if (map.get(key) == null) {
			ICache<K, V> cache = this.factory.<K, V>create();
			map.put(key, cache);
		}
		return (ICache<K, V>) map.get(key);
	}

	public void clear() {
		for (Entry<?, ICache<?, ?>> entry : map.entrySet()) {
			entry.getValue().clear();
		}
	}
}

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

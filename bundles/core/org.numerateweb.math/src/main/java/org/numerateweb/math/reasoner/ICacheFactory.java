package org.numerateweb.math.reasoner;

public interface ICacheFactory {
	<K, V> ICache<K, V> create();
}

package org.numerateweb.math.reasoner;

public class GuavaCacheFactory implements ICacheFactory {

	@Override
	public <K, V> ICache<K, V> create() {
		return new GuavaCache<>();
	}

}

package org.numerateweb.math.model;

import org.numerateweb.math.model.Builder.SeqBuilder;

public class DelegatingSeqBuilder<E> extends DelegatingBuilder<SeqBuilder<E>>
		implements SeqBuilder<E> {
	@Override
	public E end() {
		return ((SeqBuilder<E>) delegate()).end();
	}
}
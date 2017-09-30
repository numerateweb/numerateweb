package org.numerateweb.math.reasoner;

public class ResultSpec<T> {
	public Cardinality cardinality;
	public T result;

	protected ResultSpec(Cardinality cardinality, T result) {
		this.cardinality = cardinality;
		this.result = result;
	}
	
	public static <T> ResultSpec<T> create(Cardinality cardinality, T result) {
		return new ResultSpec<T>(cardinality, result);
	}
}

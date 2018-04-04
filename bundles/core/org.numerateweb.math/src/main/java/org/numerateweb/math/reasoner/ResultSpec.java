package org.numerateweb.math.reasoner;

public class ResultSpec<T> {
	public static final ResultSpec<?> EMPTY = create(Cardinality.NONE, null);

	public Cardinality cardinality;
	public T result;

	protected ResultSpec(Cardinality cardinality, T result) {
		this.cardinality = cardinality;
		this.result = result;
	}

	public static <T> ResultSpec<T> create(Cardinality cardinality, T result) {
		return new ResultSpec<T>(cardinality, result);
	}

	@SuppressWarnings("unchecked")
	public static <T> ResultSpec<T> empty() {
		return (ResultSpec<T>) EMPTY;
	}
}

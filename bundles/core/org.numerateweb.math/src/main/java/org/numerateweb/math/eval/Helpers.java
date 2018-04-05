package org.numerateweb.math.eval;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Helper functions for working with arrays, iterators and streams.
 */
public class Helpers {
	public static Function<Object, Object> unaryDouble(UnaryOperator<Double> f) {
		return unaryObj(arg -> {
			return f.apply(((Number) arg).doubleValue());
		});
	}

	public static Function<Object, Object> unaryObj(UnaryOperator<Object> f) {
		return args -> {
			if (args instanceof Object[]) {
				Object[] argsArray = (Object[]) args;
				return f.apply(argsArray[0]);
			} else if (args instanceof Iterable<?>) {
				return f.apply(((Iterable<?>) args).iterator().next());
			} else {
				return f.apply(args);
			}
		};
	}

	public static Function<Object, Object> binaryDouble(BinaryOperator<Double> f) {
		return binaryObj((a, b) -> {
			return f.apply(((Number) a).doubleValue(), ((Number) b).doubleValue());
		});
	}

	public static Function<Object, Object> binaryObj(BinaryOperator<Object> f) {
		return args -> {
			Iterator<?> it = valueToIterator(args);
			Object first = it.next();
			Object second = it.next();
			return f.apply(first, second);
		};
	}

	public static Function<Object, Object> reduce(BinaryOperator<Object> f) {
		return args -> {
			return valueToStream(args).map(x -> {
				return (Object) x;
			}).reduce(f).get();
		};
	}

	public static Iterator<?> valueToIterator(Object value) {
		Iterator<?> it;
		if (value instanceof Object[]) {
			it = Arrays.asList((Object[]) value).iterator();
		} else if (value instanceof Iterable<?>) {
			it = ((Iterable<?>) value).iterator();
		} else {
			// only a single element
			it = Collections.singleton(value).iterator();
		}
		return it;
	}

	public static Spliterator<?> valueToSpliterator(Object value) {
		Spliterator<?> it;
		if (value instanceof Object[]) {
			it = Arrays.spliterator((Object[]) value);
		} else if (value instanceof Iterator<?>) {
			it = Spliterators.spliteratorUnknownSize((Iterator<?>) value, Spliterator.ORDERED);
		} else if (value instanceof Iterable<?>) {
			it = ((Iterable<?>) value).spliterator();
		} else {
			// only a single element
			it = Collections.singleton(value).spliterator();
		}
		return it;
	}

	public static Stream<?> valueToStream(Object value) {
		if (value instanceof Stream<?>) {
			return (Stream<?>) value;
		} else {
			return StreamSupport.stream(valueToSpliterator(value), false);
		}
	}

	public static Set<?> valueToSet(Object value, boolean copy) {
		if (value instanceof Set<?>) {
			return copy ? new HashSet<>((Set<?>) value) : (Set<?>) value;
		} else {
			return valueToStream(value).collect(Collectors.toSet());
		}
	}
}
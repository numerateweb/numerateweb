package org.numerateweb.math.eval;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Spliterator;
import java.util.Stack;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.numerateweb.math.eval.expr.ConstantExpr;
import org.numerateweb.math.eval.expr.Expr;

import net.enilink.commons.iterator.IExtendedIterator;
import net.enilink.commons.util.ValueUtils;
import net.enilink.komma.core.IEntityManager;
import net.enilink.komma.core.IQuery;
import net.enilink.komma.core.IReference;
import net.enilink.komma.core.URI;
import net.enilink.komma.core.URIs;
import net.enilink.komma.em.util.ISparqlConstants;

public class Expressions {
	static final Map<String, Expr> constants = new HashMap<>();
	static final Map<String, Function<Object, Object>> functions = new HashMap<>();

	static ValueUtils values = ValueUtils.getInstance();

	static final String CDBASE = "http://www.openmath.org/cd";

	public static final URI LAMBDA_SYMBOL = URIs.createURI(CDBASE + "/fns1#lambda");

	static Function<Object, Object> unaryDouble(UnaryOperator<Double> f) {
		return unaryObj(arg -> {
			return f.apply(((Number) arg).doubleValue());
		});
	}

	static Function<Object, Object> unaryObj(UnaryOperator<Object> f) {
		return args -> {
			if (args instanceof Object[]) {
				Object[] argsArray = (Object[]) args;
				return f.apply(argsArray[0]);
			} else if (args instanceof Iterable<?>) {
				return f.apply(((Iterable<?>) args).iterator().next());
			} else {
				return Double.NaN;
			}
		};
	}

	static Function<Object, Object> binaryDouble(BinaryOperator<Double> f) {
		return binaryObj((a, b) -> {
			return f.apply(((Number) a).doubleValue(), ((Number) b).doubleValue());
		});
	}

	static Function<Object, Object> binaryObj(BinaryOperator<Object> f) {
		return args -> {
			Iterator<?> it = valueToIterator(args);
			Object first = it.next();
			Object second = it.next();
			return f.apply(first, second);
		};
	}

	static Function<Object, Object> reduce(BinaryOperator<Object> f) {
		return args -> {
			return valueToStream(args).map(x -> {
				return (Object) x;
			}).reduce(f).get();
		};
	}

	static Iterator<?> valueToIterator(Object value) {
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

	static Spliterator<?> valueToSpliterator(Object value) {
		Spliterator<?> it;
		if (value instanceof Object[]) {
			it = Arrays.spliterator((Object[]) value);
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

	static final ThreadLocal<IEntityManager> currentManager = new ThreadLocal<>();

	public static Object withManager(IEntityManager manager, Supplier<Object> func) {
		IEntityManager last = currentManager.get();
		try {
			currentManager.set(manager);
			return func.get();
		} finally {
			currentManager.set(last);
		}
	}

	public static IEntityManager getManager() {
		return currentManager.get();
	}

	static final ThreadLocal<IReference> currentResource = new ThreadLocal<>();

	public static Object withResource(IReference resource, Supplier<Object> func) {
		IReference last = currentResource.get();
		try {
			currentResource.set(resource);
			return func.get();
		} finally {
			currentResource.set(last);
		}
	}

	public static IReference getResource() {
		return currentResource.get();
	}

	static final ThreadLocal<Map<String, Stack<Object>>> currentVars = ThreadLocal.withInitial(() -> {
		return new HashMap<>();
	});

	public static Object withVar(String name, Object value, Supplier<Object> func) {
		Map<String, Stack<Object>> vars = currentVars.get();
		Stack<Object> values = vars.computeIfAbsent(name, varName -> {
			return new Stack<>();
		});
		try {
			values.push(value);
			return func.get();
		} finally {
			values.pop();
		}
	}

	public static Object setVar(String name, Object value) {
		Map<String, Stack<Object>> vars = currentVars.get();
		Stack<Object> values = vars.computeIfAbsent(name, varName -> {
			return new Stack<>();
		});
		if (!values.isEmpty()) {
			// remove current value
			values.pop();
		}
		// set the new value
		values.push(value);
		return value;
	}

	public static Object getVar(String name) {
		Map<String, Stack<Object>> vars = currentVars.get();
		Stack<Object> values = vars.get(name);
		if (values == null || values.isEmpty()) {
			return null;
		} else {
			return values.peek();
		}
	}

	public static IReference toReference(Object value) {
		if (value instanceof IReference) {
			return (IReference) value;
		} else if (value instanceof String) {
			String pname = value.toString();
			int pos = pname.indexOf(":");
			if (pos > 0) {
				String prefix = pname.substring(0, pos);
				URI namespace = getManager().getNamespace(prefix);
				if (namespace == null) {
					throw new RuntimeException("Namespace not found");
				} else {
					return namespace.appendFragment(pname.substring(pos + 1));
				}
			} else {
				return URIs.createURI(pname.toString());
			}
		}

		throw new RuntimeException("Reference expected");
	}

	static {
		functions.put(CDBASE + "/list1#list", args -> {
			return args;
		});
		functions.put(CDBASE + "/set1#set", args -> {
			return valueToStream(args).collect(Collectors.toSet());
		});
		functions.put(CDBASE + "/interval1#interval", binaryObj((a, b) -> {
			return IntStream.rangeClosed(((Number) a).intValue(), ((Number) b).intValue()).mapToObj(i -> {
				return i;
			});
		}));

		functions.put(CDBASE + "/prog1#block", reduce((a, b) -> b));

		functions.put(CDBASE + "/arith1#unary_minus", unaryObj(values::negate));
		functions.put(CDBASE + "/minmax1#max", reduce(Expressions::max));
		functions.put(CDBASE + "/minmax1#min", reduce(Expressions::min));
		functions.put(CDBASE + "/arith1#minus", binaryObj(values::subtract));

		functions.put(CDBASE + "/arith1#root", binaryDouble((a, b) -> {
			return Math.pow(a, 1 / b);
		}));

		functions.put(CDBASE + "/set1#intersect", args -> {
			@SuppressWarnings("unchecked")
			Stream<Object> stream = (Stream<Object>) valueToStream(args);
			return stream.reduce(null, (a, b) -> {
				if (a == null) {
					// the first element
					return valueToSet(b, true);
				} else {
					((Set<?>) a).retainAll(valueToSet(b, false));
					return a;
				}
			});
		});

		functions.put(CDBASE + "/rdf#resource", unaryObj(arg -> {
			// arg should be an IReference here
			return (IReference) arg;
		}));

		functions.put(CDBASE + "/transc1#sin", unaryDouble(Math::sin));
		functions.put(CDBASE + "/transc1#cos", unaryDouble(Math::cos));
		functions.put(CDBASE + "/transc1#tan", unaryDouble(Math::tan));

		functions.put(CDBASE + "/arith1#abs", unaryObj(Expressions::abs));
		functions.put(CDBASE + "/arith1#plus", reduce(values::add));
		functions.put(CDBASE + "/arith1#times", reduce(values::multiply));
		functions.put(CDBASE + "/arith1#power", binaryDouble(Math::pow));
		functions.put(CDBASE + "/arith1#divide", binaryObj(values::divide));

		functions.put(CDBASE + "/rounding1#round", unaryDouble(Expressions::round));
		functions.put(CDBASE + "/rounding1#ceiling", unaryDouble(Math::ceil));
		functions.put(CDBASE + "/rounding1#floor", unaryDouble(Math::floor));
		functions.put(CDBASE + "/rounding1#trunc", unaryDouble(Expressions::truncate));

		constants.put(CDBASE + "/nums1#pi", new ConstantExpr(Math.PI));
	}

	static Object max(Object a, Object b) {
		if (values.compareWithConversion(a, b) >= 0) {
			return a;
		} else {
			return b;
		}
	}

	static Object min(Object a, Object b) {
		if (values.compareWithConversion(a, b) >= 0) {
			return b;
		} else {
			return a;
		}
	}

	static Object abs(Object x) {
		if (values.compareWithConversion(x, 0.0) >= 0) {
			return x;
		} else {
			return values.negate(x);
		}
	}

	static double truncate(double x) {
		return (long) x;
	}

	static double round(double x) {
		return Math.round(x);
	}

	public static IExtendedIterator<?> queryValues(IReference subject, IReference property,
			Optional<IReference> restriction) {
		// DISTINCT is required to suppress duplicates due to explicit and
		// inferred statements
		StringBuilder builder = new StringBuilder(ISparqlConstants.PREFIX + //
				"SELECT DISTINCT ?value WHERE {");
		builder.append("?subject ?property ?value . ");
		restriction.ifPresent(r -> {
			builder.append("?value a ?restriction .");
		});
		builder.append("}");

		IEntityManager em = getManager();
		IQuery<?> query = em.createQuery(builder.toString()).setParameter("subject", subject).setParameter("property",
				property);
		restriction.ifPresent(r -> {
			query.setParameter("restriction", restriction);
		});

		IExtendedIterator<?> it = query.evaluate();
		return it;
	}
}

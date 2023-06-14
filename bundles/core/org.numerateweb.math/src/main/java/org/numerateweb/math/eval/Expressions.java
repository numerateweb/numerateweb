package org.numerateweb.math.eval;

import static org.numerateweb.math.eval.Helpers.binaryDouble;
import static org.numerateweb.math.eval.Helpers.binaryObj;
import static org.numerateweb.math.eval.Helpers.reduce;
import static org.numerateweb.math.eval.Helpers.unaryDouble;
import static org.numerateweb.math.eval.Helpers.unaryObj;
import static org.numerateweb.math.eval.Helpers.valueToIterator;
import static org.numerateweb.math.eval.Helpers.valueToSet;
import static org.numerateweb.math.eval.Helpers.valueToStream;

import java.math.MathContext;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Stack;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.numerateweb.math.eval.expr.ConstantExpr;
import org.numerateweb.math.eval.expr.Expr;

import net.enilink.commons.util.Pair;
import net.enilink.commons.util.ValueType;
import net.enilink.commons.util.ValueUtils;
import net.enilink.komma.core.IReference;
import net.enilink.komma.core.URI;
import net.enilink.komma.core.URIs;

public class Expressions {
	static final Map<String, Expr> constants = new HashMap<>();
	static final Map<String, Function<Object, Object>> functions = new HashMap<>();

	// override two methods to cope with specific issues
	// FIXME: change actual class behaviour
	static ValueUtils values = new ValueUtils() {

		// FIXME: allow assignment of String value to Enum type by trying Enum.valueOf()
		@Override
		@SuppressWarnings("unchecked")
		public Object convertValue(java.lang.Class<?> toType, Object value, Object defaultValue) {
			if (toType.isEnum() && value instanceof String) {
				return Enum.valueOf(toType.asSubclass(Enum.class), (String) value);
			}
			return super.convertValue(toType, value, defaultValue);
		};

		// FIXME: ValueUtils::divide() sets no scale when using BigDecimals, which
		// can result in severely rounded results (e.g. 9.0 / BigInteger(2) -> 4)
		// workaround for now, override full method, use MathContext.DECIMAL64
		// which should be equivalent to calculations with double precision
		@Override
		public Object divide(Object v1, Object v2) {
			ValueType type = getNumericType(getType(v1), getType(v2), false);

			switch (type) {
			case BIGINTEGER:
				return bigIntValue(v1).divide(bigIntValue(v2));

			case BIGDECIMAL:
				return bigDecValue(v1).divide(bigDecValue(v2), MathContext.DECIMAL64);

			case FLOAT:
			case DOUBLE:
				return createReal(type, doubleValue(v1) / doubleValue(v2));

			default:
				return createInteger(type, longValue(v1) / longValue(v2));
			}
		}
	};
	// FIXME: remove this once above changes are applied to ValueUtils
	public static ValueUtils getValueUtils() {
		return values;
	}

	static final String CDBASE = "http://www.openmath.org/cd";

	public static final URI LAMBDA_SYMBOL = URIs.createURI(CDBASE + "/fns1#lambda");

	static {
		functions.put(CDBASE + "/list1#list", args -> {
			return valueToStream(args).collect(Collectors.toList());
		});
		functions.put(CDBASE + "/set1#set", args -> {
			return valueToStream(args).collect(Collectors.toSet());
		});
		functions.put(CDBASE + "/interval1#interval", binaryObj((a, b) -> {
			return IntStream.rangeClosed(((Number) a).intValue(), ((Number) b).intValue()) //
					.mapToObj(i -> i).collect(Collectors.toList());
		}));
		functions.put(CDBASE + "/interval1#interval_oo", binaryObj((a, b) -> {
			return IntStream.range(((Number) a).intValue(), ((Number) b).intValue()) //
					.mapToObj(i -> i).collect(Collectors.toList());
		}));

		functions.put(CDBASE + "/list2#list_selector", binaryObj((i, list) -> {
			if ((int) values.longValue(i) <= 0) {
				throw new IllegalArgumentException("not a positive index: " + i);
			}
			return valueToStream(list).skip((int) values.longValue(i) - 1).findFirst().get();
		}));
		functions.put(CDBASE + "/list3#entry", binaryObj((list, i) -> {
			// FIXME: negative index (count from end) not supported
			// for positive index same as list2#list_selector(i, list)
			return functions.get(CDBASE + "/list2#list_selector").apply(new Object[] { i, list });
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

		functions.put(CDBASE + "/transc1#arcsin", unaryDouble(Math::asin));
		functions.put(CDBASE + "/transc1#arccos", unaryDouble(Math::acos));
		functions.put(CDBASE + "/transc1#arctan", unaryDouble(Math::atan));

		functions.put(CDBASE + "/arith1#abs", unaryObj(Expressions::abs));
		functions.put(CDBASE + "/arith1#plus", reduce(values::add));
		functions.put(CDBASE + "/arith1#times", reduce(values::multiply));
		functions.put(CDBASE + "/arith1#power", binaryDouble(Math::pow));
		functions.put(CDBASE + "/arith1#divide", binaryObj(values::divide));

		functions.put(CDBASE + "/relation1#eq", binaryObj((a, b) -> {
			if (Objects.equals(a, b)) {
				return true;
			}
			return values.compareWithConversion(a, b) == 0;
		}));
		functions.put(CDBASE + "/relation1#lt", binaryObj((a, b) -> values.compareWithConversion(a, b) < 0));
		functions.put(CDBASE + "/relation1#leq", binaryObj((a, b) -> values.compareWithConversion(a, b) <= 0));
		functions.put(CDBASE + "/relation1#gt", binaryObj((a, b) -> values.compareWithConversion(a, b) > 0));
		functions.put(CDBASE + "/relation1#geq", binaryObj((a, b) -> values.compareWithConversion(a, b) >= 0));
		functions.put(CDBASE + "/relation1#neq", binaryObj((a, b) -> values.compareWithConversion(a, b) != 0));

		functions.put(CDBASE + "/logic1#not", unaryObj(arg -> !values.booleanValue(arg)));
		functions.put(CDBASE + "/logic1#or", reduce((a, b) -> values.booleanValue(a) || values.booleanValue(b)));
		functions.put(CDBASE + "/logic1#and", reduce((a, b) -> values.booleanValue(a) && values.booleanValue(b)));

		functions.put(CDBASE + "/rounding1#round", unaryDouble(Expressions::round));
		functions.put(CDBASE + "/rounding1#ceiling", unaryDouble(Math::ceil));
		functions.put(CDBASE + "/rounding1#floor", unaryDouble(Math::floor));
		functions.put(CDBASE + "/rounding1#trunc", unaryDouble(Expressions::truncate));

		constants.put(CDBASE + "/sys#null", new ConstantExpr(null));
		constants.put(CDBASE + "/nums1#pi", new ConstantExpr(Math.PI));
		constants.put(CDBASE + "/nums1#e", new ConstantExpr(Math.E));
		constants.put(CDBASE + "/nums1#NaN", new ConstantExpr(Double.NaN));
		constants.put(CDBASE + "/list2#nil", new ConstantExpr(Collections.emptyList()));
		constants.put(CDBASE + "/logic1#true", new ConstantExpr(true));
		constants.put(CDBASE + "/logic1#false", new ConstantExpr(false));

		// not actually part of the nums1 CD, but NaN is useless without this check
		functions.put(CDBASE + "/nums1#isNaN", unaryObj(arg -> Double.isNaN(values.doubleValue(arg))));

		functions.put(CDBASE + "/out#println", args -> { 
			Iterator<?> it = valueToIterator(args);
			while (it.hasNext()) {
				System.out.print(it.next());
				if (it.hasNext()) {
					System.out.print(" ");
				}
			}
			System.out.println();
			return null;
		});
	}

	static Object divide(Object a, Object b) {
		// TODO improve for integer division and big decimals
		return values.divide(a, values.doubleValue(b));
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

	static final ThreadLocal<IEvaluator> currentEvaluator = new ThreadLocal<>();

	public static Object withEvaluator(IEvaluator evaluator, Supplier<Object> func) {
		IEvaluator last = currentEvaluator.get();
		try {
			currentEvaluator.set(evaluator);
			return func.get();
		} finally {
			currentEvaluator.set(last);
		}
	}

	public static IEvaluator getEvaluator() {
		return currentEvaluator.get();
	}

	static final ThreadLocal<Object> currentSubject = new ThreadLocal<>();

	public static Object withSubject(Object subject, Supplier<Object> func) {
		Object last = currentSubject.get();
		try {
			currentSubject.set(subject);
			return func.get();
		} finally {
			currentSubject.set(last);
		}
	}

	public static Object getSubject() {
		return currentSubject.get();
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
		} catch (Throwable t) {
			t.printStackTrace();
			throw t;
		} finally {
			values.pop();
		}
	}

	public static Object withVars(List<Pair<String, Object>> bindings, Supplier<Object> func) {
		Map<String, Stack<Object>> vars = currentVars.get();
		List<Stack<?>> stacks = bindings.stream().map(nameValue -> {
			Stack<Object> valueStack = vars.computeIfAbsent(nameValue.getFirst(), varName -> {
				return new Stack<>();
			});
			valueStack.push(nameValue.getSecond());
			return valueStack;
		}).collect(Collectors.toList());
		try {
			return func.get();
		} catch (Throwable t) {
			t.printStackTrace();
			throw t;
		} finally {
			stacks.forEach(stack -> stack.pop());
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
}

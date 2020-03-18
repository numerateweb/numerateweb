package org.numerateweb.math.reasoner;

import static java.util.Locale.ENGLISH;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.numerateweb.math.eval.Expressions;
import org.numerateweb.math.model.OMObject;
import org.numerateweb.math.model.OMObject.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Iterators;

import net.enilink.commons.iterator.IExtendedIterator;
import net.enilink.commons.iterator.NiceIterator;
import net.enilink.commons.iterator.WrappedIterator;
import net.enilink.komma.core.IReference;
import net.enilink.komma.core.URI;

/**
 * Support for the evaluation of mathematical formulas on POJO models.
 */
public class PojoModelAccess implements IModelAccess {
	@FunctionalInterface
	interface CheckedFunction<T, R> {
		R apply(T t) throws Exception;
	}

	@FunctionalInterface
	interface CheckedBiFunction<T, U, R> {
		R apply(T t, U u) throws Exception;
	}

	protected final static Logger logger = LoggerFactory.getLogger(PojoModelAccess.class);

	private static final String GET_PREFIX = "get";
	private static final String SET_PREFIX = "set";
	// private static final String IS_PREFIX = "is";

	// the constraints for a class
	static class ClassSpec {
		final Map<String, OMObject> constraints = new HashMap<>();
	}

	protected final Map<String, ClassSpec> classSpecs = new HashMap<>();

	public PojoModelAccess(List<OMObject> constraints) {
		for (OMObject constraint : constraints) {
			if (constraint.getType() == Type.OMA) {
				// load constraints into a map
				URI classUri = omrToUri((OMObject) constraint.getArgs()[1]);
				URI propertyUri = omrToUri((OMObject) constraint.getArgs()[2]);
				OMObject expression = (OMObject) constraint.getArgs()[3];

				addConstraint(classUri.localPart(), propertyUri.localPart(), expression);
			}
		}
	}

	private URI omrToUri(OMObject omr) {
		return (URI) omr.getArgs()[0];
	}

	private void addConstraint(String className, String propertyName, OMObject expression) {
		ClassSpec spec = classSpecs.get(className);
		if (spec == null) {
			spec = new ClassSpec();
			classSpecs.put(className, spec);
		}
		spec.constraints.put(propertyName, expression);
	}

	@Override
	public ResultSpec<OMObject> getExpressionSpec(Object subject, IReference property) {
		String propertyName = property.getURI().localPart();
		Class<?> clazz = subject.getClass();
		// walk super-classes to find a possible constraint
		while (clazz != null) {
			ClassSpec spec = classSpecs.get(clazz.getSimpleName());
			if (null == spec) {
				// try lookup with the full class name as fallback
				spec = classSpecs.get(clazz.getTypeName());
			}
			if (spec != null) {
				OMObject expression = spec.constraints.get(propertyName);
				if (expression != null) {
					if (clazz != subject.getClass()) {
						// constraint on a super-class, add it to the cache
						addConstraint(subject.getClass().getSimpleName(), propertyName, expression);
					}
					return ResultSpec.create(Cardinality.SINGLE, expression);
				}
			}
			clazz = clazz.getSuperclass();
		}
		return ResultSpec.empty();
	}

	@Override
	public IExtendedIterator<?> getInstances(IReference clazz) {
		// TODO currently not supported, used for rdf.resourceset in OpenMath
		throw new UnsupportedOperationException("not implemented");
	}

	@Override
	public Object createInstance(Object scope, URI property, URI uri, IReference clazz, Map<URI, Object> args) {
		throw new UnsupportedOperationException("not implemented");
	}

	protected CheckedFunction<Object, Object> findGetter(Class<?> clazz, String propertyName) {
		CheckedFunction<Object, Object> getter;

		Method m = getMethod(clazz, GET_PREFIX + capitalize(propertyName));
		if (m != null) {
			if (!m.isAccessible()) {
				m.setAccessible(true);
			}
			getter = s -> m.invoke(s);
		} else {
			Field f = getField(clazz, propertyName);
			if (f != null) {
				if (!f.isAccessible()) {
					f.setAccessible(true);
				}
				getter = s -> f.get(s);
			} else {
				getter = null;
			}
		}

		return getter;
	}

	protected CheckedBiFunction<Object, Object, Object> findSetter(Class<?> clazz, String propertyName) {
		CheckedBiFunction<Object, Object, Object> setter;
		final Method m = getMethod(clazz, SET_PREFIX + capitalize(propertyName));
		if (m != null) {
			if (!m.isAccessible()) {
				m.setAccessible(true);
			}
			final boolean unpack = !Collection.class.isAssignableFrom(m.getParameterTypes()[0]);
			setter = (s, arg) -> {
				logger.trace("invoking {}.{}({})", s, m.getName(), arg);
				return m.invoke(s, Expressions.getValueUtils().convertValue(m.getParameterTypes()[0],
						unpack ? ((Collection<?>) arg).toArray()[0] : arg, null));
			};
		} else {
			final Field f = getField(clazz, propertyName);
			if (f != null) {
				if (!f.isAccessible()) {
					f.setAccessible(true);
				}
				final boolean unpack = !Collection.class.isAssignableFrom(f.getType());
				setter = (s, arg) -> {
					logger.trace("setting {}.{} = {}", s, f.getName(), arg);
					f.set(s, Expressions.getValueUtils().convertValue(f.getType(),
							unpack ? ((Collection<?>) arg).toArray()[0] : arg, null));
					return null;
				};
			} else {
				setter = null;
			}
		}

		return setter;
	}

	@Override
	public IExtendedIterator<?> getPropertyValues(Object subject, IReference property,
			Optional<IReference> restriction) {
		// TODO cache getter
		String propertyName = property.getURI().localPart();
		CheckedFunction<Object, Object> getter = findGetter(subject.getClass(), propertyName);
		if (getter != null) {
			try {
				Object result = getter.apply(subject);
				if (null != result) {
					return WrappedIterator.create(Iterators.singletonIterator(result));
				}
			} catch (Exception e) {
				logger.warn("model access failed for {}.{}: {}", subject, property, e.getMessage());
			}
		}
		// FIXME: always return something (NaN?) for non-existent values?!
		return NiceIterator.emptyIterator();
	}

	public void setPropertyValue(Object subject, IReference property, Object value) {
		// TODO cache getter
		String propertyName = property.getURI().localPart();
		// TODO match method signature against the given value type
		CheckedBiFunction<Object, Object, Object> setter = findSetter(subject.getClass(), propertyName);
		if (setter != null) {
			try {
				setter.apply(subject, value);
			} catch (Exception e) {
				logger.error("unable to apply new value={} to ({}, {}): {}", value, subject, property, e);
			}
		}
	}

	/**
	 * Returns the first {@link Field} (also private and package protected ones) in
	 * the hierarchy for the specified name.
	 */
	private static Field getField(Class<?> clazz, String name) {
		Field field = null;
		while (clazz != null && field == null) {
			try {
				field = clazz.getDeclaredField(name);
			} catch (Exception e) {
			}
			clazz = clazz.getSuperclass();
		}
		return field;
	}

	/**
	 * Returns the first {@link Method} (also private and package protected ones) in
	 * the hierarchy for the specified name.
	 */
	private static Method getMethod(Class<?> clazz, String name) {
		Method method = null;
		while (clazz != null && method == null) {
			try {
				method = clazz.getDeclaredMethod(name);
			} catch (NoSuchMethodException nsme) {
				// setters are only found if given the correct parameter types
				// to second guess on invocation (arg type), check for just the method name
				// TODO match method signature against the given value type
				Optional<Method> opt = Arrays.asList(clazz.getDeclaredMethods()).stream()
						.filter(m -> m.getName().equals(name)).findFirst();
				method = opt.orElseGet(() -> null);
			} catch (Exception e) {
			}
			clazz = clazz.getSuperclass();
		}
		return method;
	}

	private static String capitalize(String name) {
		if (name == null || name.length() == 0) {
			return name;
		}
		return name.substring(0, 1).toUpperCase(ENGLISH) + name.substring(1);
	}
}
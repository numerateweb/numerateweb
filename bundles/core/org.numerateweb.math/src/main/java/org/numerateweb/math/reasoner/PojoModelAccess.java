package org.numerateweb.math.reasoner;

import static java.util.Locale.ENGLISH;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.numerateweb.math.model.OMObject;
import org.numerateweb.math.model.OMObject.Type;

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

	private static final String GET_PREFIX = "get";
	private static final String SET_PREFIX = "set";
	private static final String IS_PREFIX = "is";

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
			if (spec != null) {
				OMObject expression = spec.constraints.get(propertyName);
				if (expression != null) {
					if (clazz != subject.getClass()) {
						// this is a constraint on a super-class, add it to the
						// cache
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
		return NiceIterator.emptyIterator();
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

	@Override
	public IExtendedIterator<?> getPropertyValues(Object subject, IReference property,
			Optional<IReference> restriction) {
		// TODO cache getter
		String propertyName = property.getURI().localPart();
		CheckedFunction<Object, Object> getter = findGetter(subject.getClass(), propertyName);
		if (getter != null) {
			try {
				return WrappedIterator.create(Iterators.singletonIterator(getter.apply(subject)));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return NiceIterator.emptyIterator();
	}

	public void setPropertyValue(Object subject, IReference property, Object value) {

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
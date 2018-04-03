package org.numerateweb.math.reasoner;

import java.lang.reflect.Field;
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
 * 
 * Support for the evaluation of mathematical formulas on POJO models.
 *
 */
public class PojoModelAccess implements IModelAccess {
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
		return null;
	}

	@Override
	public IExtendedIterator<?> getInstances(IReference clazz) {
		// TODO currently not supported, used for rdf.resourceset in OpenMath
		return NiceIterator.emptyIterator();
	}

	@Override
	public IExtendedIterator<?> getPropertyValues(Object subject, IReference property,
			Optional<IReference> restriction) {
		try {
			// TODO cache field
			Field f = subject.getClass().getField(property.getURI().localPart());
			return WrappedIterator.create(Iterators.singletonIterator(f.get(subject)));
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return NiceIterator.emptyIterator();
	}
}

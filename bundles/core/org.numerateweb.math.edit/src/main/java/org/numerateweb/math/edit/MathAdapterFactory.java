package org.numerateweb.math.edit;

import net.enilink.komma.core.URI;
import net.enilink.komma.edit.properties.IEditingSupport;
import net.enilink.komma.edit.provider.AdapterFactory;
import net.enilink.komma.edit.provider.ComposedAdapterFactory;
import net.enilink.komma.edit.provider.IComposeableAdapterFactory;
import net.enilink.komma.edit.provider.IItemLabelProvider;
import net.enilink.vocab.rdf.Property;

import org.numerateweb.math.rdf.rules.Constraint;
import org.numerateweb.math.rdf.rules.NWRULES;
import org.numerateweb.math.rdf.vocab.NWMATH;

public class MathAdapterFactory extends AdapterFactory implements
		IComposeableAdapterFactory {
	protected ComposedAdapterFactory parentAdapterFactory;

	@Override
	protected Object createAdapter(Object object, Object type) {
		if (IItemLabelProvider.class.equals(type)) {
			if ((object instanceof Constraint || object instanceof org.numerateweb.math.rdf.vocab.Object)
					&& !(object instanceof Property)) {
				return new MathLabelProvider();
			}
			return null;
		}
		if (IEditingSupport.class.equals(type)) {
			if (NWRULES.PROPERTY_CONSTRAINT.equals(object)
					|| NWRULES.PROPERTY_EXPRESSION.equals(object)) {
				return new PopcornEditingSupport(this);
			}
		}
		return null;
	}

	@Override
	public boolean isFactoryForType(Object type) {
		if (type instanceof URI) {
			if (NWRULES.NAMESPACE_URI.equals(type)) {
				return true;
			}
			if (NWMATH.NAMESPACE_URI.equals(type)) {
				return true;
			}
		}
		if (IEditingSupport.class.equals(type)) {
			return true;
		}
		if (IItemLabelProvider.class.equals(type)) {
			return true;
		}
		return false;
	}

	@Override
	public IComposeableAdapterFactory getRootAdapterFactory() {
		return parentAdapterFactory.getRootAdapterFactory();
	}

	@Override
	public void setParentAdapterFactory(
			ComposedAdapterFactory parentAdapterFactory) {
		this.parentAdapterFactory = parentAdapterFactory;
	}
}
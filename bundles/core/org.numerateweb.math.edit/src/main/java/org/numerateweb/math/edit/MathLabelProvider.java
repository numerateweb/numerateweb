package org.numerateweb.math.edit;

import org.numerateweb.math.ns.INamespaces;
import org.numerateweb.math.ns.Namespaces;
import org.numerateweb.math.popcorn.PopcornBuilder;
import org.numerateweb.math.rdf.NWMathParser;
import org.numerateweb.math.rdf.rules.Constraint;

import net.enilink.komma.edit.provider.IItemLabelProvider;
import net.enilink.komma.model.ModelUtil;
import net.enilink.komma.core.IEntity;
import net.enilink.komma.core.IReference;

public class MathLabelProvider implements IItemLabelProvider {

	@Override
	public String getText(Object object) {
		if (object instanceof Constraint) {
			Constraint constraint = (Constraint) object;
			IReference p = constraint.getOnProperty();
			String expr = renderMath(constraint.getExpression());
			if (p != null) {
				return ModelUtil.getLabel(p) + " = "
						+ (expr == null ? "?" : expr);
			}
			return expr;
		}
		if (object instanceof org.numerateweb.math.rdf.vocab.Object) {
			return renderMath((org.numerateweb.math.rdf.vocab.Object) object);
		}
		return ModelUtil.getLabel(object);
	}

	protected String renderMath(IReference object) {
		if (object == null) {
			return null;
		}
		INamespaces ns = new Namespaces(((IEntity) object).getEntityManager());
		return new NWMathParser(ns).resolveURIs(false)
				.parse(object, new PopcornBuilder(ns)).toString();
	}

	@Override
	public Object getImage(Object object) {
		return MathEditPlugin.INSTANCE.getBundleResourceLocator().getImage(
				"full/obj16/pi.png");
	}

}

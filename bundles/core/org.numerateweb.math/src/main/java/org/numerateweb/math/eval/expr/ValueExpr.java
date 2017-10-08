package org.numerateweb.math.eval.expr;

import java.util.Optional;

import net.enilink.commons.iterator.IExtendedIterator;
import net.enilink.komma.core.IReference;

public class ValueExpr extends ValueSetExpr {
	public ValueExpr(IReference property, Optional<Expr> subjectExpr) {
		super(property, subjectExpr);
	}

	@Override
	protected Object query(IReference subject, IReference property, Optional<IReference> restriction) {
		IExtendedIterator<?> it = (IExtendedIterator<?>) super.query(subject, property, restriction);
		if (!it.hasNext()) {
			it.close();
			throw new IllegalArgumentException("No value for " + property + " of " + subject);
		}
		try {
			Object value = it.next();
			if (it.hasNext()) {
				throw new IllegalArgumentException("Multiple values for " + property + " of " + subject);
			}
			return value;
		} finally {
			it.close();
		}
	}
}

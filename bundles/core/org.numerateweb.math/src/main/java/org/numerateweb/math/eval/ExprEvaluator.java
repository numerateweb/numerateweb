package org.numerateweb.math.eval;

import java.math.BigInteger;

import org.numerateweb.math.eval.expr.ConstantExpr;
import org.numerateweb.math.eval.expr.Expr;
import org.numerateweb.math.model.OMObject;
import org.numerateweb.math.model.OMObjectParser;
import org.numerateweb.math.reasoner.AbstractEvaluator;
import org.numerateweb.math.reasoner.CacheManager;

import net.enilink.komma.core.IEntityManager;
import net.enilink.komma.core.IReference;

/**
 * Simple numeric evaluator for OpenMath objects. 
 */
public class ExprEvaluator extends AbstractEvaluator<Object> {
	protected ExprEvaluator(IEntityManager manager, CacheManager cacheManager) {
		super(manager, cacheManager);
	}

	@Override
	protected Object errorToExpression(String message) {
		return new Exception(message);
	}

	@Override
	protected Object eval(IReference subject, Object expression) {
		return Expressions.withManager(manager, () -> {
			return Expressions.withResource(subject, () -> {
				return ((Expr) expression).eval();
			});
		});
	}

	@Override
	protected Object javaValueToExpression(Object value) {
		return new ConstantExpr(value);
	}

	@Override
	protected Object parse(OMObject omobj) {
		return new OMObjectParser().parse(omobj, new ExprBuilder());
	}

	@Override
	protected OMObject unparse(Object expr) {
		if (expr instanceof Double || expr instanceof Float) {
			return OMObject.OMF(((Number) expr).doubleValue());
		} else if (expr instanceof BigInteger) {
			return OMObject.OMI((BigInteger) expr);
		} else if (expr instanceof Number) {
			return OMObject.OMI(((Number) expr).intValue());
		} else {
			return OMObject.OMSTR(expr.toString());
		}
	}
}
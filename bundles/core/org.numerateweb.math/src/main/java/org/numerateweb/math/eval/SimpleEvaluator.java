package org.numerateweb.math.eval;

import static org.numerateweb.math.model.OMObject.OME;
import static org.numerateweb.math.model.OMObject.OMS;
import static org.numerateweb.math.model.OMObject.OMSTR;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Map;

import org.numerateweb.math.eval.expr.Expr;
import org.numerateweb.math.model.OMObject;
import org.numerateweb.math.model.OMObjectParser;
import org.numerateweb.math.reasoner.AbstractEvaluator;
import org.numerateweb.math.reasoner.CacheManager;
import org.numerateweb.math.reasoner.IModelAccess;

import net.enilink.komma.core.IReference;
import net.enilink.komma.core.URI;

/**
 * Simple numeric evaluator for OpenMath objects.
 */
public class SimpleEvaluator extends AbstractEvaluator<Object> {
	public SimpleEvaluator(IModelAccess modelAccess, CacheManager cacheManager) {
		super(modelAccess, cacheManager);
	}

	@Override
	protected Object errorToExpression(String message) {
		return new Exception(message);
	}

	@Override
	protected Object eval(Object subject, Object expression) {
		try {
			return Expressions.withEvaluator(this, () -> {
				return Expressions.withSubject(subject, () -> {
					return ((Expr) expression).eval();
				});
			});
		} catch (Exception e) {
			return OME(OMS("nw:error"), OMSTR(e.getClass().getName() + (e.getMessage() != null ? ": " + e.getMessage() : "")));
		}
	}

	@Override
	protected Object javaValueToExpression(Object value) {
		return value; // new ConstantExpr(value);
	}

	@Override
	protected Object parse(OMObject omobj) {
		return new OMObjectParser().parse(omobj, new ExprBuilder());
	}

	@Override
	protected OMObject unparse(Object expr) {
		if (expr instanceof OMObject) {
			return (OMObject) expr;
		} else if (expr instanceof Boolean) {
			return ((Boolean) expr).booleanValue() ? OMObject.LOGIC1_TRUE : OMObject.LOGIC1_FALSE;
		} else if (expr instanceof Double || expr instanceof Float || expr instanceof BigDecimal) {
			return OMObject.OMF(((Number) expr).doubleValue());
		} else if (expr instanceof BigInteger) {
			return OMObject.OMI((BigInteger) expr);
		} else if (expr instanceof Number) {
			return OMObject.OMI(((Number) expr).intValue());
		} else {
			return OMObject.OMSTR(expr.toString());
		}
	}

	@Override
	public Object createInstance(URI uri, IReference clazz, Map<URI, Object> args) {
		throw new UnsupportedOperationException("Not implemented");
	}
}
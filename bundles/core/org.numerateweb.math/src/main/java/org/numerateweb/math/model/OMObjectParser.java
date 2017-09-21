package org.numerateweb.math.model;

import java.math.BigInteger;
import java.util.Arrays;

import org.numerateweb.math.model.Builder.BindingBuilder;
import org.numerateweb.math.model.Builder.VariablesBuilder;
import org.numerateweb.math.model.OMObject.Type;
import org.numerateweb.math.ns.INamespaces;

import net.enilink.komma.core.IReference;
import net.enilink.komma.core.URI;

public class OMObjectParser {
	public <T> T parse(OMObject omobj, Builder<T> builder) {
		if (omobj.getId() != null) {
			builder.id(omobj.getId());
		}
		switch (omobj.getType()) {
		case OMS:
			return createOMS(omobj, builder);
		case OMV:
			return createOMV(omobj, builder);
		case OMI:
			return createOMI(omobj, builder);
		case OMB:
			return createOMB(omobj, builder);
		case OMSTR:
			return createOMSTR(omobj, builder);
		case OMF:
			return createOMF(omobj, builder);
		case OMA:
			return createOMA(omobj, builder);
		case OMBIND:
			return createOMBIND(omobj, builder);
		case OME:
			return createOME(omobj, builder);
		case OMATTR:
			return createOMATTR(omobj, builder);
		case OMR:
			return createOMR(omobj, builder);
		case RDF_CLASS:
			return createRDFClass(omobj, builder);
		default:
			// case OMFOREIGN:
			return createOMFOREIGN(omobj, builder);
		}
	}

	@SuppressWarnings("unchecked")
	protected <T> T parse(Object[] objs, Builder<T> builder) {
		T result = null;
		for (Object obj : objs) {
			result = parse((OMObject) obj, builder);
		}
		if (result == null) {
			result = (T) builder;
		}
		return result;
	}

	protected <T> T createOMS(OMObject omobj, Builder<T> builder) {
		return builder.s((URI) omobj.getArgs()[0]);
	}

	protected <T> T createOMV(OMObject omobj, Builder<T> builder) {
		return builder.var((String) omobj.getArgs()[0]);
	}

	protected <T> T createOMI(OMObject omobj, Builder<T> builder) {
		return builder.i(((BigInteger) omobj.getArgs()[0]));
	}

	protected <T> T createOMB(OMObject omobj, Builder<T> builder) {
		return builder.b((String) omobj.getArgs()[0]);
	}

	protected <T> T createOMSTR(OMObject omobj, Builder<T> builder) {
		return builder.str((String) omobj.getArgs()[0]);
	}

	protected <T> T createOMF(OMObject omobj, Builder<T> builder) {
		return builder.f(((Number) omobj.getArgs()[0]).doubleValue());
	}

	protected <T> T createOMA(OMObject omobj, Builder<T> builder) {
		return parse(omobj.getArgs(), builder.apply()).end();
	}

	protected <T> T createOMBIND(OMObject omobj, Builder<T> builder) {
		Object[] args = omobj.getArgs();
		BindingBuilder<T> binding = builder.bind();
		parse((OMObject) args[0], binding.binder());
		VariablesBuilder<?> varBuilder = binding.variables();
		for (Object var : ((OMObject) args[1]).getArgs()) {
			if (var instanceof OMObject
					&& ((OMObject) var).getType() == Type.OMV) {
				varBuilder.var(((OMObject) var).getArgs()[0].toString());
			}
		}
		varBuilder.end();
		return parse((OMObject) args[2], binding.body()).end();
	}

	protected <T> T createOME(OMObject omobj, Builder<T> builder) {
		Object[] args = omobj.getArgs();
		return parse(Arrays.copyOfRange(args, 1, args.length),
				builder.error((URI) ((OMObject) args[0]).getArgs()[0])).end();
	}

	protected <T> T createOMATTR(OMObject omobj, Builder<T> builder) {
		Object[] args = omobj.getArgs();
		Object[] attributes = ((OMObject) args[0]).getArgs();
		for (int i = 0; i < attributes.length; i += 2) {
			builder = parse((OMObject) attributes[i + 1],
					builder.attr((URI) ((OMObject) attributes[i]).getArgs()[0]));
		}
		return parse((OMObject) args[1], builder);
	}

	protected <T> T createOMR(OMObject omobj, Builder<T> builder) {
		return builder.ref((IReference) omobj.getArgs()[0]);
	}

	protected <T> T createOMFOREIGN(OMObject omobj, Builder<T> builder) {
		Object[] args = omobj.getArgs();
		return builder.foreign((String) args[0], (String) args[1]);
	}

	protected <T> T createRDFClass(OMObject omobj, Builder<T> builder) {
		return builder.rdfClass((IReference) omobj.getArgs()[0],
				(INamespaces) omobj.getArgs()[1]);
	}
}

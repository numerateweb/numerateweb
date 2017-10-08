package org.numerateweb.math.model;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

import net.enilink.komma.core.URI;
import net.enilink.komma.core.URIs;

/**
 * A simple in-memory representation of mathematical objects.
 * 
 */
public class OMObject {
	public enum Type {
		OMS, OMV, OMI, OMB, OMSTR, OMF, OMA, OMBIND, OME, OMATTR, OMR, OMATP, OMFOREIGN, OMBVAR, RDF_CLASS
	}

	public static OMObject OMA(List<OMObject> args) {
		return OMA(args.toArray(new OMObject[args.size()]));
	}

	public static OMObject OMA(OMObject... args) {
		return new OMObject(Type.OMA, (Object[]) args);
	}
	
	public static OMObject OME(List<OMObject> args) {
		return OME(args.toArray(new OMObject[args.size()]));
	}
	
	public static OMObject OME(OMObject... args) {
		return new OMObject(Type.OME, (Object[]) args);
	}

	public static OMObject OMATTR(List<OMObject> attributes, OMObject target) {
		return new OMObject(Type.OMATTR, new OMObject(Type.OMATP,
				attributes.toArray()), target);
	}

	public static OMObject OMB(String base64Binary) {
		return new OMObject(Type.OMB, base64Binary);
	}

	public static OMObject OMBIND(OMObject operator, List<OMObject> variables,
			OMObject expression) {
		return new OMObject(Type.OMBIND, operator, new OMObject(Type.OMBVAR,
				variables.toArray()), expression);
	}

	public static OMObject OMBIND(OMObject operator, OMObject variable,
			OMObject expression) {
		return new OMObject(Type.OMBIND, operator, new OMObject(Type.OMBVAR,
				variable), expression);
	}

	public static OMObject OMF(double value) {
		return new OMObject(Type.OMF, value);
	}

	public static OMObject OMFOREIGN(String encoding, String content) {
		return new OMObject(Type.OMFOREIGN, encoding, content);
	}

	public static OMObject OMI(Integer i) {
		return OMI(new BigInteger(i.toString()));
	}

	public static OMObject OMI(BigInteger value) {
		return new OMObject(Type.OMI, value);
	}

	public static OMObject OMR(String uri) {
		return OMR(URIs.createURI(uri));
	}

	public static OMObject OMR(URI uri) {
		return new OMObject(Type.OMR, uri);
	}

	public static OMObject OMS(String symbol) {
		return OMS(URIs.createURI(symbol));
	}

	public static OMObject OMS(URI symbol) {
		return new OMObject(Type.OMS, symbol);
	}

	public static OMObject OMSTR(String value) {
		return new OMObject(Type.OMSTR, value);
	}

	public static OMObject OMV(String variableName) {
		return new OMObject(Type.OMV, variableName);
	}

	protected Object[] args;

	protected URI id;

	protected Type type;

	public OMObject(Type type, Object... args) {
		this(null, type, args);
	}

	public OMObject(URI id, Type type, Object... args) {
		this.id = id;
		this.type = type;
		this.args = args;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		OMObject other = (OMObject) obj;
		if (!Arrays.equals(args, other.args))
			return false;
		if (type != other.type)
			return false;
		return true;
	}

	public Object[] getArgs() {
		return args;
	}

	public URI getId() {
		return id;
	}

	public Type getType() {
		return type;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(args);
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	public String argAsString(int index) {
		if (args.length <= index) {
			throw new IllegalArgumentException("Argument " + index
					+ " does not exists.");
		}
		return String.valueOf(args[index]);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("(");
		sb.append(type);
		for (int i = 0; i < args.length; i++) {
			sb.append(' ').append(args[i]);
		}
		return sb.append(')').toString();
	}

}

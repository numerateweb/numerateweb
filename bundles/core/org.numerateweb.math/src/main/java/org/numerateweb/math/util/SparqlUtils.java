package org.numerateweb.math.util;

public class SparqlUtils {

	public static String prefix(String prefix, String uri) {
		return String.format("PREFIX %s: <%s> \n", prefix, uri);
	}
	
}

package org.numerateweb.math.model;

import org.numerateweb.math.ns.INamespaces;

import net.enilink.komma.parser.manchester.ManchesterSyntaxGenerator;
import net.enilink.komma.core.IReference;
import net.enilink.komma.core.URI;

public class BuilderUtils {

	public static final String CDBASE = "http://www.openmath.org/cd";

	public static String classAsString(IReference arg,
			final INamespaces namespaces) {
		return new ManchesterSyntaxGenerator() {
			protected String getPrefix(IReference reference) {
				String prefix = namespaces != null ? namespaces
						.getPrefix(reference.getURI().namespace()) : null;
				return prefix != null ? prefix : super.getPrefix(reference);
			}
		}.generateText(arg);
	}

	public static String toPNameOrUriString(IReference reference,
			final INamespaces namespaces) {
		URI uri = reference.getURI();
		String str;
		if (uri != null) {
			String prefix = namespaces != null ? namespaces.getPrefix(uri
					.namespace()) : null;
			if (prefix == null) {
				str = "<" + uri.toString() + ">";
			} else {
				str = prefix + (prefix.isEmpty() ? "" : ":") + uri.localPart();
			}
		} else {
			str = reference.toString();
		}
		return str;
	}
}

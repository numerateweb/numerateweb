/*
 * Copyright (c) 2023 Numerate Web contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

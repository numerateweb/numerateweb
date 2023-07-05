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
package org.numerateweb.math.util.stax;

import javax.xml.namespace.QName;
import javax.xml.stream.Location;

public class ParseException extends Exception {
	private static final long serialVersionUID = 2873348872583794388L;

	Location location;
	QName name;

	public ParseException(String msg) {
		this(msg, null, null);
	}

	public ParseException(String msg, Location location, QName name) {
		super(msg);
		this.location = location;
		this.name = name;
	}

	public Location getLocation() {
		return location;
	}

	public QName getName() {
		return name;
	}

	public String toString() {
		if (location == null) {
			return getMessage();
		}

		StringBuffer sb = new StringBuffer(getMessage()).append(" at ")
				.append(location.getLineNumber()).append(":")
				.append(location.getColumnNumber()).append(" <");
		if (name != null) {
			if (name.getPrefix() != null && name.getPrefix().length() > 0) {
				sb.append(name.getPrefix()).append(":");
			}
			sb.append(name.getLocalPart()).append(">");
		}
		return sb.toString();
	}
}

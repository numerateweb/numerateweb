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
package org.numerateweb.math.cli;

import java.util.HashMap;
import java.util.Map;

import net.enilink.commons.iterator.IMap;
import net.enilink.komma.core.BlankNode;
import net.enilink.komma.core.IReference;
import net.enilink.komma.core.IStatement;
import net.enilink.komma.core.IValue;
import net.enilink.komma.core.Statement;

class NodeIdShortener implements IMap<IStatement, IStatement> {
	final Map<String, IReference> bnodeMap = new HashMap<>();
	long nextNodeId = 0;

	@Override
	public IStatement map(IStatement stmt) {
		return new Statement(convert(stmt.getSubject()), convert(stmt.getPredicate()),
				convert((IValue) stmt.getObject()));
	}

	@SuppressWarnings("unchecked")
	<V> V convert(V value) {
		if (value instanceof IReference && ((IReference) value).getURI() == null) {
			String valueAsString = value.toString();
			if (valueAsString.startsWith("_:")) {
				String id = valueAsString.substring(2);
				IReference bnode = bnodeMap.get(id);
				if (bnode == null) {
					String shortId = Long.toString(nextNodeId++, 36);
					String newId = "_:n" + shortId;
					bnode = new BlankNode(newId);
					bnodeMap.put(id, bnode);
				}
				return (V) bnode;
			}
		}
		return value;
	}
}
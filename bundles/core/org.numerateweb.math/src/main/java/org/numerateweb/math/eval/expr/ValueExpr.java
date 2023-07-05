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
package org.numerateweb.math.eval.expr;

import java.util.Optional;

import net.enilink.commons.iterator.IExtendedIterator;
import net.enilink.komma.core.IReference;

public class ValueExpr extends ValueSetExpr {
	public ValueExpr(IReference property, Optional<Expr> subjectExpr) {
		super(property, subjectExpr);
	}

	@Override
	protected Object query(Object subject, IReference property, Optional<IReference> restriction) {
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

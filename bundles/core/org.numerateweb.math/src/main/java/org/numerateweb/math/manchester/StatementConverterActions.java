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
package org.numerateweb.math.manchester;

import java.util.ArrayList;
import java.util.List;

import net.enilink.komma.core.IReference;
import net.enilink.komma.core.IStatement;
import net.enilink.komma.core.Statement;
import net.enilink.komma.parser.manchester.IManchesterActions;

import org.parboiled.BaseActions;

/**
 * This class is used to convert syntactic statements into the representation
 * that is used by KOMMA.
 */
public class StatementConverterActions extends BaseActions<Object> implements
		IManchesterActions {
	private List<Object[]> statementsBuffer = new ArrayList<Object[]>();
	private ValueConverter statementConverter;

	public StatementConverterActions(ValueConverter statementConverter) {
		this.statementConverter = statementConverter;
	}

	public boolean createStmt(Object subject, Object predicate, Object object) {
		statementsBuffer.add(new Object[] { subject, predicate, object });
		return true;
	}

	/**
	 * @return The new (converted) statements
	 */
	public List<IStatement> getResult() {
		List<IStatement> converted = new ArrayList<IStatement>(
				statementsBuffer.size());

		for (Object[] statement : statementsBuffer) {
			Object s = statement[0], p = statement[1], o = statement[2];

			Statement realStatement = new Statement(
					(IReference) this.statementConverter.toValue(s),
					(IReference) this.statementConverter.toValue(p),
					this.statementConverter.toValue(o));

			converted.add(realStatement);
		}

		return converted;
	}

}

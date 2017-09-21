package org.numerateweb.math.helper;

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

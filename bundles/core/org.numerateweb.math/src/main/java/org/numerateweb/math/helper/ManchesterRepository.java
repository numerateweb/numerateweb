package org.numerateweb.math.helper;

import net.enilink.komma.core.IEntityManager;
import net.enilink.komma.core.IReference;
import net.enilink.komma.em.concepts.IClass;
import net.enilink.komma.parser.manchester.ManchesterSyntaxParser;

import org.numerateweb.math.ns.Namespaces;
import org.parboiled.Parboiled;
import org.parboiled.parserunners.BasicParseRunner;
import org.parboiled.support.ParsingResult;

public class ManchesterRepository {
	private IEntityManager em;

	public ManchesterRepository(IEntityManager em) {
		this.em = em;
	}

	public IClass get(String manchesterExpression) {
		ValueConverter statementConverter = new ValueConverter(em,
				new Namespaces(em));

		StatementConverterActions statementConverterActions = new StatementConverterActions(
				statementConverter);

		ManchesterSyntaxParser parser = Parboiled.createParser(
				ManchesterSyntaxParser.class, statementConverterActions);

		BasicParseRunner<Object> parseRunner = new BasicParseRunner<Object>(
				parser.Description());

		ParsingResult<Object> parsingResult = parseRunner
				.run(manchesterExpression);

		em.add(statementConverterActions.getResult());

		return em.find((IReference) statementConverter
				.toValue(parsingResult.resultValue), IClass.class);
	}
}

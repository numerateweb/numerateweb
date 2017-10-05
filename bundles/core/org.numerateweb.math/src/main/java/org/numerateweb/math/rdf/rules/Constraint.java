package org.numerateweb.math.rdf.rules;

import net.enilink.composition.annotations.Iri;

import net.enilink.komma.core.IEntity;
import net.enilink.komma.core.IReference;

@Iri("http://numerateweb.org/vocab/math/rules#Constraint")
public interface Constraint extends IEntity {

	@Iri("http://numerateweb.org/vocab/math/rules#expressionString")
	String getExpressionString();
	
	void setExpressionString(String value);
	
	@Iri("http://numerateweb.org/vocab/math/rules#expression")
	IReference getExpression();

	void setExpression(IReference value);

	@Iri("http://numerateweb.org/vocab/math/rules#onProperty")
	IReference getOnProperty();

	void setOnProperty(IReference property);
}
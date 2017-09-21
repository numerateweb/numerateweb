package org.numerateweb.math.concepts;

import java.util.List;

import net.enilink.composition.annotations.Iri;
import net.enilink.composition.properties.annotations.Type;

import net.enilink.komma.core.IReference;

/**
 * 
 * @generated
 */
@Iri("http://numerateweb.org/vocab/math#Binding")
public interface Binding extends Compound {
	/**
	 * 
	 * @generated
	 */
	@Iri("http://numerateweb.org/vocab/math#binder")
	IReference getBinder();

	/**
	 * 
	 * @generated
	 */
	void setBinder(IReference binder);

	/**
	 * 
	 * @generated
	 */
	@Iri("http://numerateweb.org/vocab/math#body")
	IReference getBody();

	/**
	 * 
	 * @generated
	 */
	void setBody(IReference body);

	/**
	 * 
	 * @generated
	 */
	@Iri("http://numerateweb.org/vocab/math#variables")
	@Type("http://www.w3.org/1999/02/22-rdf-syntax-ns#List")
	List<Variable> getVariables();

	/**
	 * 
	 * @generated
	 */
	void setVariables(List<Variable> variables);

}

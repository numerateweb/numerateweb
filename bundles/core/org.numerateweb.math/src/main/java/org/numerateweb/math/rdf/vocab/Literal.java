package org.numerateweb.math.rdf.vocab;

import net.enilink.composition.annotations.Iri;

import net.enilink.komma.core.ILiteral;

/** 
 * 
 * @generated 
 */
@Iri("http://numerateweb.org/vocab/math#Literal")
public interface Literal extends Object {
	/** 
	 * 
	 * @generated 
	 */
	@Iri("http://numerateweb.org/vocab/math#value")
	ILiteral getValue();
	/** 
	 * 
	 * @generated 
	 */
	void setValue(ILiteral value);

}

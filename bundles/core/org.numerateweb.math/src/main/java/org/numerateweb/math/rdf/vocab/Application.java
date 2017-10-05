package org.numerateweb.math.rdf.vocab;

import java.util.List;

import net.enilink.composition.annotations.Iri;
import net.enilink.composition.properties.annotations.Type;

import net.enilink.komma.core.IReference;

/** 
 * 
 * @generated 
 */
@Iri("http://numerateweb.org/vocab/math#Application")
public interface Application extends Compound {
	/** 
	 * 
	 * @generated 
	 */
	@Iri("http://numerateweb.org/vocab/math#arguments")
	@Type("http://www.w3.org/1999/02/22-rdf-syntax-ns#List")
	List<IReference> getArguments();
	/** 
	 * 
	 * @generated 
	 */
	void setArguments(List<IReference> arguments);

	/** 
	 * 
	 * @generated 
	 */
	@Iri("http://numerateweb.org/vocab/math#operator")
	IReference getOperator();
	/** 
	 * 
	 * @generated 
	 */
	void setOperator(IReference operator);

}

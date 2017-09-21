package org.numerateweb.math.concepts;

import java.util.List;

import net.enilink.composition.annotations.Iri;
import net.enilink.composition.properties.annotations.Type;

import net.enilink.komma.core.IReference;

/** 
 * 
 * @generated 
 */
@Iri("http://numerateweb.org/vocab/math#Error")
public interface Error extends Compound {
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
	@Iri("http://numerateweb.org/vocab/math#symbol")
	Symbol getSymbol();
	/** 
	 * 
	 * @generated 
	 */
	void setSymbol(Symbol symbol);

}

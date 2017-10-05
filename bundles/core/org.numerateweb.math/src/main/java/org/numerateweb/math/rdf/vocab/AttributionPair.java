package org.numerateweb.math.rdf.vocab;

import net.enilink.composition.annotations.Iri;

import net.enilink.vocab.owl.Thing;
import net.enilink.komma.core.IEntity;
import net.enilink.komma.core.IReference;

/** 
 * 
 * @generated 
 */
@Iri("http://numerateweb.org/vocab/math#AttributionPair")
public interface AttributionPair extends Thing, IEntity {
	/** 
	 * 
	 * @generated 
	 */
	@Iri("http://numerateweb.org/vocab/math#attributeKey")
	Symbol getAttributeKey();
	/** 
	 * 
	 * @generated 
	 */
	void setAttributeKey(Symbol attributeKey);

	/** 
	 * 
	 * @generated 
	 */
	@Iri("http://numerateweb.org/vocab/math#attributeValue")
	IReference getAttributeValue();
	/** 
	 * 
	 * @generated 
	 */
	void setAttributeValue(IReference attributeValue);

}

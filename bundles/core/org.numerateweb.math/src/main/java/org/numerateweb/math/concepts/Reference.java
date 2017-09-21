package org.numerateweb.math.concepts;

import net.enilink.composition.annotations.Iri;
import net.enilink.komma.core.IReference;

/** 
 * 
 * @generated 
 */
@Iri("http://numerateweb.org/vocab/math#Reference")
public interface Reference extends IReference {
	/** 
	 * 
	 * @generated 
	 */
	@Iri("http://numerateweb.org/vocab/math#target")
	IReference getTarget();
	/** 
	 * 
	 * @generated 
	 */
	void setTarget(IReference target);
}

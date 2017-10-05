package org.numerateweb.math.eval.expr;

import java.util.Optional;

import net.enilink.komma.core.IReference;

/**
 * Interface for expressions that may have an optional RDF restriction.
 */
public interface EvalWithRestriction {
	/**
	 * Set the RDF restriction for this expression.
	 * 
	 * @param restriction A reference to an RDF type.
	 */
	Object evalWithRestriction(Optional<IReference> restriction);
}

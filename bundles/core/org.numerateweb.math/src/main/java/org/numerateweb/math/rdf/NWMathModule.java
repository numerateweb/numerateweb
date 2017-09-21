package org.numerateweb.math.rdf;

import net.enilink.komma.core.KommaModule;

import org.numerateweb.math.concepts.Application;
import org.numerateweb.math.concepts.Attribution;
import org.numerateweb.math.concepts.AttributionPair;
import org.numerateweb.math.concepts.Binding;
import org.numerateweb.math.concepts.Compound;
import org.numerateweb.math.concepts.Error;
import org.numerateweb.math.concepts.Foreign;
import org.numerateweb.math.concepts.Literal;
import org.numerateweb.math.concepts.Object;
import org.numerateweb.math.concepts.Reference;
import org.numerateweb.math.concepts.Symbol;
import org.numerateweb.math.concepts.Variable;
import org.numerateweb.math.rules.Constraint;

public class NWMathModule extends KommaModule {
	public NWMathModule() {
		addConcept(Constraint.class);
		addConcept(Application.class);
		addConcept(Attribution.class);
		addConcept(AttributionPair.class);
		addConcept(Binding.class);
		addConcept(Compound.class);
		addConcept(Error.class);
		addConcept(Foreign.class);
		addConcept(Literal.class);
		addConcept(Object.class);
		addConcept(Reference.class);
		addConcept(Symbol.class);
		addConcept(Variable.class);

		addBehaviour(ObjectSupport.class);
	}
}

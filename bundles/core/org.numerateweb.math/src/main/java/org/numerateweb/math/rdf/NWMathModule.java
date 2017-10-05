package org.numerateweb.math.rdf;

import net.enilink.komma.core.KommaModule;

import org.numerateweb.math.rdf.rules.Constraint;
import org.numerateweb.math.rdf.vocab.Application;
import org.numerateweb.math.rdf.vocab.Attribution;
import org.numerateweb.math.rdf.vocab.AttributionPair;
import org.numerateweb.math.rdf.vocab.Binding;
import org.numerateweb.math.rdf.vocab.Compound;
import org.numerateweb.math.rdf.vocab.Error;
import org.numerateweb.math.rdf.vocab.Foreign;
import org.numerateweb.math.rdf.vocab.Literal;
import org.numerateweb.math.rdf.vocab.Object;
import org.numerateweb.math.rdf.vocab.Reference;
import org.numerateweb.math.rdf.vocab.Symbol;
import org.numerateweb.math.rdf.vocab.Variable;

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

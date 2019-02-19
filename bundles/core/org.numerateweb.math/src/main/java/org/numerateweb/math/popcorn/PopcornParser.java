package org.numerateweb.math.popcorn;

import static org.numerateweb.math.model.OMObject.OMA;
import static org.numerateweb.math.model.OMObject.OMATTR;
import static org.numerateweb.math.model.OMObject.OMBIND;
import static org.numerateweb.math.model.OMObject.OMF;
import static org.numerateweb.math.model.OMObject.OMFOREIGN;
import static org.numerateweb.math.model.OMObject.OMI;
import static org.numerateweb.math.model.OMObject.OMS;
import static org.numerateweb.math.model.OMObject.OMSTR;
import static org.numerateweb.math.model.OMObject.OMV;
import static org.numerateweb.math.popcorn.PopcornSymbols.symbol;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import net.enilink.komma.core.URI;
import net.enilink.komma.core.URIs;
import net.enilink.komma.parser.BaseRdfParser;
import net.enilink.komma.parser.manchester.ManchesterSyntaxParser;
import net.enilink.komma.parser.sparql.tree.DoubleLiteral;
import net.enilink.komma.parser.sparql.tree.IntegerLiteral;
import net.enilink.komma.parser.sparql.tree.IriRef;
import net.enilink.komma.parser.sparql.tree.NumericLiteral;
import net.enilink.komma.parser.sparql.tree.QName;

import org.numerateweb.math.model.Builder;
import org.numerateweb.math.model.OMObject;
import org.numerateweb.math.model.OMObject.Type;
import org.numerateweb.math.model.OMObjectParser;
import org.numerateweb.math.ns.INamespaces;
import org.numerateweb.math.om.rdf.OMRdfSymbols;
import org.numerateweb.math.search.MATCH;
import org.parboiled.Parboiled;
import org.parboiled.Rule;
import org.parboiled.annotations.DontLabel;
import org.parboiled.support.Var;

/**
 * Parser for the POPCORN representation of OpenMath.
 */
public class PopcornParser extends BaseRdfParser {
	public static final Class<OMObject> objClass = OMObject.class;

	public ManchesterSyntaxParser manchesterParser = Parboiled.createParser(ManchesterSyntaxParser.class);

	static class Namespaces implements INamespaces {
		INamespaces next;
		String prefix;
		URI ns;

		public Namespaces(String prefix, URI ns, INamespaces next) {
			this.prefix = prefix;
			this.ns = ns;
			this.next = next;
		}

		@Override
		public URI getNamespace(String prefix) {
			if (prefix.equals(this.prefix)) {
				return ns;
			}
			return next != null ? next.getNamespace(prefix) : null;
		}

		@Override
		public String getPrefix(URI namespace) {
			if (namespace.equals(this.ns)) {
				return prefix;
			}
			return next != null ? next.getPrefix(namespace) : null;
		}
	}

	public INamespaces ns;

	public PopcornParser() {
		this(null);
	}

	public PopcornParser(INamespaces ns) {
		this.ns = ns;
	}

	public boolean pushSymbol(String prefix, String name) {
		return pushSymbolOrRef(prefix, name, true);
	}

	public boolean pushSymbolOrRef(String prefix, String name, boolean isSymbol) {
		URI namespace = ns != null ? ns.getNamespace(prefix) : null;

		URI uri;
		if (namespace != null) {
			uri = namespace.appendLocalPart(name);
		} else if (isSymbol) {
			if (prefix == null) {
				uri = URIs.createURI("symbol:" + name);
			} else {
				uri = symbol(prefix, name);
			}
		} else {
			// invalid expression
			// references must be resolvable
			// throw new IllegalArgumentException("Unknown prefix \"" + prefix +
			// "\" for reference \"" + prefix + ":" + name + "\".");
			return false;
		}
		return push(new OMObject(isSymbol ? Type.OMS : Type.OMR, uri));
	}

	public boolean pushSymbol(Object value) {
		return pushSymbolOrRef(value, true);
	}

	public boolean pushRef(Object value) {
		return pushSymbolOrRef(value, false);
	}

	public boolean pushSymbolOrRef(Object value, boolean isSymbol) {
		Type type = isSymbol ? Type.OMS : Type.OMR;
		if (value instanceof URI) {
			push(new OMObject(type, (URI) value));
		} else if (value instanceof QName) {
			String prefix = ((QName) value).getPrefix();
			String localPart = ((QName) value).getLocalPart();
			if (isSymbol) {
				return pushSymbol(prefix != null && prefix.length() > 0
						? new StringBuilder(prefix).append(":").append(localPart).toString()
						: localPart);
			} else {
				return pushSymbolOrRef(prefix, localPart, false);
			}
		} else {
			push(new OMObject(type, URIs.createURI(value.toString())));
		}
		return true;
	}

	public boolean pushSymbol(String opOrShortHand) {
		URI symbol = PopcornSymbols.toOpenMath(opOrShortHand.trim().toLowerCase());
		if (symbol != null) {
			push(OMS(symbol));
		} else {
			String[] parts = opOrShortHand.split("[:]");
			if (parts.length == 2) {
				pushSymbol(parts[0], parts[1]);
			} else {
				pushSymbol(null, opOrShortHand);
			}
		}
		return true;
	}

	public Rule Start(Builder<?> builder) {
		return sequence(Expr(), push(builder != null ? new OMObjectParser().parse((OMObject) pop(), builder) : pop()),
				EOI);
	}

	public Rule Expr() {
		Var<Boolean> nsChanged = new Var<>(false);
		return sequence(optional(PrefixDecl(), nsChanged.set(true)), BlockExpr(), resetNamespaces(nsChanged.get()));
	}

	public boolean updateNamespaces(String prefix, URI namespace) {
		// add a new prefix -> namespace mapping
		this.ns = new Namespaces(prefix, namespace, this.ns);
		return true;
	}

	public boolean resetNamespaces(boolean doReset) {
		if (doReset) {
			// reset to previous value
			this.ns = ((Namespaces) this.ns).next;
		}
		return true;
	}

	public Rule PrefixDecl() {
		return sequence("prefix", PNAME_NS(), WS(), IRI_REF(), //
				updateNamespaces((String) pop(1), URIs.createURI(((IriRef) pop()).getIri())));
	}

	public boolean startList() {
		pushSymbol(match());
		push(LIST_BEGIN);
		swap(3);
		return true;
	}

	public Rule BlockExpr() {
		return sequence(AssignExpr(), optional(OpList(';', AssignExpr())));
	}

	public Rule AssignExpr() {
		return sequence(ImplExpr(), optional(":=", startList(), ImplExpr(), push(OMA(popList(objClass)))));
	}

	public Rule ImplExpr() {
		return sequence(OrExpr(), optional(firstOf("==>", "<=>"), startList(), OrExpr(), push(OMA(popList(objClass)))));
	}

	public Rule OrExpr() {
		return sequence(AndExpr(), optional(OpList("or", AndExpr())));
	}

	public Rule AndExpr() {
		return sequence(RelExpr(), optional(OpList("and", RelExpr())));
	}

	public Rule RelExpr() {
		return sequence(IntervalExpr(), optional(firstOf('=', "<=", '<', ">=", '>', "!=", "<>"), startList(),
				IntervalExpr(), push(OMA(popList(objClass)))));
	}

	public Rule IntervalExpr() {
		return sequence(AddExpr(), optional("..", startList(), AddExpr(), push(OMA(popList(objClass)))));
	}

	public Rule AddExpr() {
		return sequence(MultExpr(), zeroOrMore(firstOf(OpList('-', MultExpr()), OpList('+', MultExpr()))));
	}

	public Rule MultExpr() {
		return sequence(PowerExpr(), zeroOrMore(firstOf(OpList('/', PowerExpr()), OpList('*', PowerExpr()))));
	}

	public Rule PowerExpr() {
		return sequence(ComplexExpr(), optional('^', startList(), ComplexExpr(), push(OMA(popList(objClass)))));
	}

	public Rule ComplexExpr() {
		return sequence(RationalExpr(), optional('|', startList(), RationalExpr(), push(OMA(popList(objClass)))));
	}

	public Rule RationalExpr() {
		return sequence(NegExpr(), optional("//", startList(), NegExpr(), push(OMA(popList(objClass)))));
	}

	/**
	 * Rule that captures an repeated application of the same operator
	 * <code>op</code>.
	 * <p>
	 * A <code>op</code> B <code>op</code> C <code>op</code> ...
	 * </p>
	 */
	@DontLabel
	public Rule OpList(Object operator, Object operand) {
		return sequence(operator, startList(), operand, zeroOrMore(operator, operand), push(OMA(popList(objClass))));
	}

	public Rule NegExpr() {
		Var<String> unaryOp = new Var<>();
		return sequence(optional(firstOf('-', "not"), unaryOp.set(match())), CompExpr(),
				unaryOp.get() != null ? push(
						OMA(OMS("-".equals(unaryOp.get()) ? symbol("arith1", "unary_minus") : symbol("logic1", "not")),
								(OMObject) pop()))
						: true);
	}

	public Rule CompExpr() {
		return sequence(firstOf( //
				Call(), //
				// ECall(), //
				ListExpr(), //
				SetExpr(), //
				CompactLambda(), //
				sequence(Anchor(), optional(BindingSuffix())) //
		), optional(AttributionSuffix()));
	}

	public Rule CommaList(Rule element) {
		return optional(element, zeroOrMore(',', element));
	}

	public Rule Call() {
		return sequence(push(LIST_BEGIN), Anchor(), '(', CommaList(Expr()), ')', push(OMA(popList(objClass))));
	}

	// public Rule ECall() {
	// return sequence(Anchor(), '!', '(', CommaList(), ')');
	// }

	public Rule ListExpr() {
		return sequence('[', push(LIST_BEGIN), pushSymbol("list1", "list"), CommaList(Expr()), ']',
				push(OMA(popList(objClass))));
	}

	public Rule SetExpr() {
		return sequence('{', push(LIST_BEGIN), pushSymbol("set1", "set"), CommaList(Expr()), '}',
				push(OMA(popList(objClass))));
	}

	public Rule AttributionSuffix() {
		return sequence(push(LIST_BEGIN), '{', AttributionList(), '}',
				push(OMATTR(popList(objClass), (OMObject) pop())));
	}

	public Rule AttributionList() {
		return sequence(AttributionPair(), zeroOrMore(',', AttributionPair()));
	}

	public Rule AttributionPair() {
		// pairs are created in Attribution() rule
		return sequence(Expr(), "->", Expr());
	}

	// lambda expression in the form: $a, $b -> $a + $b
	public Rule CompactLambda() {
		Var<List<OMObject>> vars = new Var<>();
		return sequence(push(LIST_BEGIN),
				// list of variables with optional attributions
				CommaList(sequence(Var(), optional(AttributionSuffix()))), vars.set(popList(objClass)), "->", Expr(),
				push(OMBIND(OMS(symbol("fns1", "lambda")), vars.get(), (OMObject) pop())));
	}

	public Rule BindingSuffix() {
		Var<List<OMObject>> vars = new Var<>();
		return sequence('[', push(LIST_BEGIN),
				// list of variables with optional attributions
				CommaList(sequence(Var(), optional(AttributionSuffix()))), vars.set(popList(objClass)), "->", Expr(),
				']', push(OMBIND((OMObject) pop(1), vars.get(), (OMObject) pop())));
	}

	public Rule Anchor() {
		return sequence(Atom(), optional(":", ID(),
				// TODO handle ID
				drop()));
	}

	public Rule Atom() {
		return firstOf(ParaExpr(), //
				Var(), //
				Rdf(), //
				IfExpr(), //
				WhileExpr(), //
				Pattern(), //
				Ref(), //
				Symbol(), //
				sequence(NumericLiteral(), push(createNumber((NumericLiteral) pop()))), //
				OMB(), //
				FOREIGN(), //
				sequence(StringLiteral(), push(OMSTR((String) pop()))) //
		);
	}

	public Rule Symbol() {
		return sequence(IriRef(), pushSymbol(pop()));
	}

	public Rule Ref() {
		return sequence(ch('#'), IriRef(), pushRef(pop()));
	}

	public Object createNumber(NumericLiteral literal) {
		if (literal instanceof IntegerLiteral) {
			return OMI(new BigInteger(String.valueOf(((IntegerLiteral) literal).getValue())));
		}
		return OMF(((DoubleLiteral) literal).getValue());
	}

	public Rule IfExpr() {
		return sequence(push(LIST_BEGIN), pushSymbol("prog1", "if"), "if", Expr(), "then", Expr(), "else", Expr(),
				"endif", push(OMA(popList(objClass))));
	}

	public Rule WhileExpr() {
		return sequence(push(LIST_BEGIN), pushSymbol("prog1", "while"), "while", Expr(), "do", Expr(), "endwhile",
				push(OMA(popList(objClass))));
	}

	public Rule ParaExpr() {
		return sequence('(', Expr(), ')');
	}

	public Rule Var() {
		return sequence(ch('$'), ID(), push(OMV(pop().toString())));
	}

	public Rule OMB() {
		return sequence(string("%"), zeroOrMore(firstOf(DIGIT(), charRange('a', 'z'), charRange('A', 'Z'))),
				push(OMObject.OMB(match())), "%");
	}

	public Rule ID() {
		return sequence(sequence(PN_CHARS_U(), zeroOrMore(firstOf(DIGIT(), PN_CHARS_U()))), push(match()), WS());
	}

	/* <RDF support> */

	public Rule Rdf() {
		return firstOf(sequence(string("@@"), RdfResourceSet()), //
				sequence(ch('@'), RdfResource()), //
				sequence(ch('@'), RdfProperty()));
	}

	public Rule RdfResourceSet() {
		// set of resources
		return sequence('[', manchesterParser.Description(), drop(),
				push(OMA(Arrays.asList(OMS(symbol("rdf", "resourceset")), //
						// TODO check if this can be directly converted to OpenMath
						OMSTR(match().trim())))),
				']');
	}

	public Rule RdfResource() {
		// exactly one resource
		return sequence('(', IriRef(), pushRef(pop()), // save reference to value stack
				push(OMA(Arrays.asList(OMS(symbol("rdf", "resource")), //
						(OMObject) pop() // use reference from value stack
				))), ')');
	}

	public OMObject createPropertyPath(boolean isSet, boolean hasArg) {
		OMObject arg = hasArg ? (OMObject) pop() : null;
		for (Iterator<OMObject> pathIt = popList(objClass).iterator(); pathIt.hasNext();) {
			OMObject pathElem = pathIt.next();
			OMObject valueFunc = OMS(!pathIt.hasNext() && isSet ? OMRdfSymbols.VALUESET : OMRdfSymbols.VALUE);
			arg = arg == null ? OMA(valueFunc, pathElem) : OMA(valueFunc, pathElem, arg);
		}
		return arg;
	}

	public Rule RdfPropertyPathElem() {
		return sequence(IriRef(), pushRef(pop()));
	}

	public Rule RdfProperty() {
		Var<Boolean> isSet = new Var<>(false);
		Var<Boolean> hasArg = new Var<>(false);
		return sequence(optional(ch('@'), isSet.set(true)), push(LIST_BEGIN),
				firstOf(RdfPropertyPathElem(),
						sequence("'", RdfPropertyPathElem(), zeroOrMore("/", RdfPropertyPathElem(), "'"))),
				optional('(', Expr(), hasArg.set(true), ')'), push(createPropertyPath(isSet.get(), hasArg.get())),
				// an optional restriction @@property(...)[Restriction]
				optional(!!isSet.get(), // fail if this is not a set
						RdfResourceSet(), push(OMA(Arrays.asList(OMS(symbol("set1", "intersect")), (OMObject) pop(1),
								(OMObject) pop())))));
	}

	/* </RDF support> */

	// allow to use a wider range of prefixed names, e.g. 3Dgeo1:circle
	public Rule PN_PREFIX() {
		return sequence(zeroOrMore(DIGIT()), PN_CHARS_U(), zeroOrMore(firstOf(PN_CHARS(), sequence('.', PN_CHARS()))));
	}

	public Rule IriRef() {
		return sequence(firstOf(IRI_REF(),
				// PrefixedNameEscaped(),
				PrefixedName(),
				// allow pure local names without using ':'
				sequence(sequence(zeroOrMore(DIGIT()), PN_CHARS_U(), optional(PN_LOCAL(), drop()) //
				), push(new QName("", match().trim())))//
		), WS());
	}

	public Rule FOREIGN() {
		return sequence('`', zeroOrMore(noneOf("<`")), push(match().trim()),
				sequence('<', oneOrMore(testNot("`"), ANY)), push(OMFOREIGN((String) pop(), match())), '`');
	}

	public Rule COMMENT() {
		return sequence("/*", zeroOrMore(testNot("*/"), ANY), "*/");
	}

	// these rules are required to enforce that decimals and doubles have at
	// least one decimal place
	public Rule DECIMAL() {
		return sequence(
				firstOf(sequence(oneOrMore(DIGIT()), '.', oneOrMore(DIGIT())), sequence('.', oneOrMore(DIGIT()))),
				push(new DoubleLiteral(Double.parseDouble(match().trim()))), WS());
	}

	public Rule DOUBLE() {
		return sequence(
				firstOf(sequence(oneOrMore(DIGIT()), '.', oneOrMore(DIGIT()), EXPONENT()),
						sequence('.', oneOrMore(DIGIT()), EXPONENT()), sequence(oneOrMore(DIGIT()), EXPONENT())),
				push(new DoubleLiteral(Double.parseDouble(match().trim()))), WS());
	}

	public Rule Pattern() {
		return firstOf(sequence(".!", pushSymbol(MATCH.NOT)), sequence(".|", pushSymbol(MATCH.ANY)),
				sequence(".&", pushSymbol(MATCH.ALL)), sequence(".^", pushSymbol(MATCH.ROOT)),
				sequence("...", pushSymbol(MATCH.SELF_OR_DESCENDANT)), sequence("..+", pushSymbol(MATCH.DESCENDANT)), //
				Wildcard());
	}

	public Rule Wildcard() {
		return sequence(ch('?'), firstOf(ID(), sequence(WS(), push(""))), pushSymbol(MATCH.variable((String) pop())));
	}
}

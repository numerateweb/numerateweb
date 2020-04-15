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
import org.numerateweb.math.search.PATTERNS;
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
		return Sequence(Expr(), push(builder != null ? new OMObjectParser().parse((OMObject) pop(), builder) : pop()),
				EOI);
	}

	public Rule Expr() {
		Var<Boolean> nsChanged = new Var<>(false);
		return Sequence(Optional(PrefixDecl(), nsChanged.set(true)), //
				FirstOf(Sequence("begin", BlockExpr(), "end"), BlockExpr()), //
				resetNamespaces(nsChanged.get()));
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
		return Sequence("prefix", PNAME_NS(), WS(), IRI_REF(), //
				updateNamespaces((String) pop(1), URIs.createURI(((IriRef) pop()).getIri())));
	}

	public boolean startList() {
		pushSymbol(match());
		push(LIST_BEGIN);
		swap3();
		return true;
	}

	public Rule BlockExpr() {
		return Sequence(AssignExpr(), Optional(NAryOp(';', AssignExpr())));
	}

	public Rule AssignExpr() {
		return Sequence(ImplExpr(), Optional(":=", startList(), ImplExpr(), push(OMA(popList(objClass)))));
	}

	public Rule ImplExpr() {
		return Sequence(OrExpr(), Optional(FirstOf("==>", "<=>"), startList(), OrExpr(), push(OMA(popList(objClass)))));
	}

	public Rule OrExpr() {
		return Sequence(AndExpr(), Optional(NAryOp("or", AndExpr())));
	}

	public Rule AndExpr() {
		return Sequence(RelExpr(), Optional(NAryOp("and", RelExpr())));
	}

	public Rule RelExpr() {
		return Sequence(IntervalExpr(), Optional(FirstOf('=', "<=", '<', ">=", '>', "!=", "<>"), startList(),
				IntervalExpr(), push(OMA(popList(objClass)))));
	}

	public Rule IntervalExpr() {
		return Sequence(AddExpr(),
				Optional(FirstOf("until", ".."), startList(), AddExpr(), push(OMA(popList(objClass)))));
	}

	public Rule AddExpr() {
		return Sequence(MultExpr(), ZeroOrMore(FirstOf(BinaryOp('-', MultExpr()), NAryOp('+', MultExpr()))));
	}

	public Rule MultExpr() {
		return Sequence(PowerExpr(), ZeroOrMore(FirstOf(BinaryOp('/', PowerExpr()), NAryOp('*', PowerExpr()))));
	}

	public Rule PowerExpr() {
		return Sequence(ComplexExpr(), Optional(BinaryOp('^', ComplexExpr())));
	}

	public Rule ComplexExpr() {
		return Sequence(RationalExpr(), Optional(BinaryOp('|', RationalExpr())));
	}

	public Rule RationalExpr() {
		return Sequence(NegExpr(), Optional(BinaryOp("//", NegExpr())));
	}

	/**
	 * Rule that captures an n-ary application of an operator <code>op</code>.
	 * <p>
	 * A <code>op</code> B <code>op</code> C <code>op</code> ...
	 * </p>
	 */
	@DontLabel
	public Rule NAryOp(Object operator, Object operand) {
		return Sequence(operator, startList(), operand, ZeroOrMore(operator, operand), push(OMA(popList(objClass))));
	}

	/**
	 * Rule that captures a Sequence of binary applications of a left-associative
	 * operator <code>op</code>.
	 * <p>
	 * [( ] [( ]A <code>op</code> B[ )] <code>op</code> C[ )] <code>op</code> ...
	 * </p>
	 */
	@DontLabel
	public Rule BinaryOp(Object operator, Object operand) {
		return OneOrMore(operator, pushSymbol(match()), operand,
				push(OMA((OMObject) pop(1), (OMObject) pop(1), (OMObject) pop())));
	}

	public Rule NegExpr() {
		Var<String> unaryOp = new Var<>();
		return Sequence(Optional(FirstOf('-', "not"), unaryOp.set(match())), CompExpr(),
				unaryOp.get() != null ? push(
						OMA(OMS("-".equals(unaryOp.get()) ? symbol("arith1", "unary_minus") : symbol("logic1", "not")),
								(OMObject) pop()))
						: true);
	}

	public Rule CompExpr() {
		return Sequence(FirstOf( //
				Call(), //
				// ECall(), //
				ListExpr(), //
				SetExpr(), //
				CompactLambda(), //
				Sequence(Anchor(), Optional(BindingSuffix())) //
		), Optional(AttributionSuffix()));
	}

	public Rule CommaList(Rule element) {
		return Optional(element, ZeroOrMore(',', element));
	}

	public Rule Call() {
		return Sequence(push(LIST_BEGIN), Anchor(), '(', CommaList(Expr()), ')', push(OMA(popList(objClass))));
	}

	// public Rule ECall() {
	// return Sequence(Anchor(), '!', '(', CommaList(), ')');
	// }

	public Rule ListExpr() {
		return Sequence('[', push(LIST_BEGIN), pushSymbol("list1", "list"), CommaList(Expr()), ']',
				push(OMA(popList(objClass))));
	}

	public Rule SetExpr() {
		return Sequence('{', push(LIST_BEGIN), pushSymbol("set1", "set"), CommaList(Expr()), '}',
				push(OMA(popList(objClass))));
	}

	public Rule AttributionSuffix() {
		return Sequence(push(LIST_BEGIN), '{', AttributionList(), '}',
				push(OMATTR(popList(objClass), (OMObject) pop())));
	}

	public Rule AttributionList() {
		return Sequence(AttributionPair(), ZeroOrMore(',', AttributionPair()));
	}

	public Rule AttributionPair() {
		// pairs are created in Attribution() rule
		return Sequence(Expr(), "->", Expr());
	}

	// lambda expression in the form: $a, $b -> $a + $b
	public Rule CompactLambda() {
		Var<List<OMObject>> vars = new Var<>();
		return Sequence(push(LIST_BEGIN),
				// list of variables with Optional attributions
				CommaList(Sequence(Var(), Optional(AttributionSuffix()))), vars.set(popList(objClass)), "->", //
				// block expressions where statements are separated by ; are not allowed here
				// parentheses have to be used
				AssignExpr(), //
				push(OMBIND(OMS(symbol("fns1", "lambda")), vars.get(), (OMObject) pop())));
	}

	public Rule BindingSuffix() {
		Var<List<OMObject>> vars = new Var<>();
		return Sequence('[', push(LIST_BEGIN),
				// list of variables with Optional attributions
				CommaList(Sequence(Var(), Optional(AttributionSuffix()))), vars.set(popList(objClass)), "->", Expr(),
				']', push(OMBIND((OMObject) pop(1), vars.get(), (OMObject) pop())));
	}

	public Rule Anchor() {
		return Sequence(Atom(), Optional(":", ID(),
				// TODO handle ID
				drop()));
	}

	public Rule Atom() {
		return FirstOf(ParaExpr(), //
				Var(), //
				Rdf(), //
				IfExpr(), //
				WhileExpr(), //
				Pattern(), //
				Ref(), //
				Symbol(), //
				Sequence(NumericLiteral(), push(createNumber((NumericLiteral) pop()))), //
				OMB(), //
				FOREIGN(), //
				Sequence(StringLiteral(), push(OMSTR((String) pop()))) //
		);
	}

	public Rule Symbol() {
		return Sequence(IriRef(), pushSymbol(pop()));
	}

	public Rule Ref() {
		return Sequence(Ch('#'), IriRef(), pushRef(pop()));
	}

	public Object createNumber(NumericLiteral literal) {
		if (literal instanceof IntegerLiteral) {
			return OMI(new BigInteger(String.valueOf(((IntegerLiteral) literal).getValue())));
		}
		return OMF(((DoubleLiteral) literal).getValue());
	}

	public Rule IfExpr() {
		return Sequence(push(LIST_BEGIN), pushSymbol("prog1", "if"), "if", Expr(), "then", Expr(),
				Optional("else", Expr()), "end", push(OMA(popList(objClass))));
	}

	public Rule WhileExpr() {
		return Sequence(push(LIST_BEGIN), pushSymbol("prog1", "while"), "while", Expr(), "do", Expr(), "end",
				push(OMA(popList(objClass))));
	}

	public Rule ParaExpr() {
		return Sequence('(', Expr(), ')');
	}

	public Rule Var() {
		return Sequence(Ch('$'), ID(), push(OMV(pop().toString())));
	}

	public Rule OMB() {
		return Sequence(String("%"), ZeroOrMore(FirstOf(DIGIT(), CharRange('a', 'z'), CharRange('A', 'Z'))),
				push(OMObject.OMB(match())), "%");
	}

	public Rule ID() {
		return Sequence(Sequence(PN_CHARS_U(), ZeroOrMore(FirstOf(DIGIT(), PN_CHARS_U()))), push(match()), WS());
	}

	/* <RDF support> */

	public Rule Rdf() {
		return FirstOf(Sequence(String("@@"), RdfResourceSet()), //
				Sequence(Ch('@'), RdfResource()), //
				Sequence(Ch('@'), RdfProperty()));
	}

	public Rule RdfResourceSet() {
		// set of resources
		return Sequence('[', manchesterParser.Description(), drop(),
				push(OMA(Arrays.asList(OMS(symbol("rdf", "resourceset")), //
						// TODO check if this can be directly converted to OpenMath
						OMSTR(match().trim())))),
				']');
	}

	public Rule RdfResource() {
		// exactly one resource
		return Sequence('(', IriRef(), pushRef(pop()), // save reference to value stack
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
		return Sequence(IriRef(), pushRef(pop()));
	}

	public Rule RdfProperty() {
		Var<Boolean> isSet = new Var<>(false);
		Var<Boolean> hasArg = new Var<>(false);
		return Sequence(Optional(Ch('@'), isSet.set(true)), push(LIST_BEGIN),
				FirstOf(RdfPropertyPathElem(),
						Sequence("'", RdfPropertyPathElem(), ZeroOrMore("/", RdfPropertyPathElem(), "'"))),
				Optional('(', Expr(), hasArg.set(true), ')'), push(createPropertyPath(isSet.get(), hasArg.get())),
				// an Optional restriction @@property(...)[Restriction]
				Optional(!!isSet.get(), // fail if this is not a set
						RdfResourceSet(), push(OMA(Arrays.asList(OMS(symbol("set1", "intersect")), (OMObject) pop(1),
								(OMObject) pop())))));
	}

	/* </RDF support> */

	// allow to use a wider range of prefixed names, e.g. 3Dgeo1:circle
	public Rule PN_PREFIX() {
		return Sequence(ZeroOrMore(DIGIT()), PN_CHARS_U(), ZeroOrMore(FirstOf(PN_CHARS(), Sequence('.', PN_CHARS()))));
	}

	public Rule IriRef() {
		return Sequence(FirstOf(IRI_REF(),
				// PrefixedNameEscaped(),
				PrefixedName(),
				// allow pure local names without using ':'
				Sequence(Sequence(ZeroOrMore(DIGIT()), PN_CHARS_U(), Optional(PN_LOCAL(), drop()) //
				), push(new QName("", match().trim())))//
		), WS());
	}

	public Rule FOREIGN() {
		return Sequence('`', ZeroOrMore(NoneOf("<`")), push(match().trim()),
				Sequence('<', OneOrMore(TestNot("`"), ANY)), push(OMFOREIGN((String) pop(), match())), '`');
	}

	public Rule COMMENT() {
		return Sequence("/*", ZeroOrMore(TestNot("*/"), ANY), "*/");
	}

	// these rules are required to enforce that decimals and doubles have at
	// least one decimal place
	public Rule DECIMAL() {
		return Sequence(
				FirstOf(Sequence(OneOrMore(DIGIT()), '.', OneOrMore(DIGIT())), Sequence('.', OneOrMore(DIGIT()))),
				push(new DoubleLiteral(Double.parseDouble(match().trim()))), WS());
	}

	public Rule DOUBLE() {
		return Sequence(
				FirstOf(Sequence(OneOrMore(DIGIT()), '.', OneOrMore(DIGIT()), EXPONENT()),
						Sequence('.', OneOrMore(DIGIT()), EXPONENT()), Sequence(OneOrMore(DIGIT()), EXPONENT())),
				push(new DoubleLiteral(Double.parseDouble(match().trim()))), WS());
	}

	public Rule Pattern() {
		return FirstOf(Sequence(".!", pushSymbol(PATTERNS.NONE_OF)), Sequence(".|", pushSymbol(PATTERNS.ANY_OF)),
				Sequence(".&", pushSymbol(PATTERNS.ALL_OF)), Sequence(".^", pushSymbol(PATTERNS.ROOT)),
				Sequence(".,", pushSymbol(PATTERNS.ARGUMENT)), Sequence("...", pushSymbol(PATTERNS.SELF_OR_DESCENDANT)),
				Sequence("..+", pushSymbol(PATTERNS.DESCENDANT)), //
				Wildcard());
	}

	public Rule Wildcard() {
		return Sequence(Ch('?'), pushSymbol(PATTERNS.ANY));
	}
}

package org.numerateweb.math.search;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.enilink.komma.core.IEntityManager;
import net.enilink.komma.core.IEntityManagerFactory;
import net.enilink.komma.core.IGraph;
import net.enilink.komma.core.IReference;
import net.enilink.komma.core.IStatement;
import net.enilink.komma.core.LinkedHashGraph;
import net.enilink.komma.core.URI;
import net.enilink.vocab.rdf.RDF;

import org.numerateweb.math.ns.INamespaces;
import org.numerateweb.math.popcorn.PopcornParser;
import org.numerateweb.math.rdf.RdfHelpers;
import org.numerateweb.math.rdf.NWMathBuilder;
import org.numerateweb.math.rdf.vocab.NWMATH;
import org.numerateweb.math.util.SparqlUtils;
import org.parboiled.Parboiled;
import org.parboiled.parserunners.ReportingParseRunner;
import org.parboiled.support.ParsingResult;

/**
 * Transforms patterns for mathematical expression given in a POPCORN notation
 * into equivalent SPARQL queries.
 */
public class PopcornPatternToSparql {
	protected static class Result {
		INamespaces ns;
		int nextVar = 0;
		int nextNode = 0;
		StringBuilder sb = new StringBuilder();
		StringBuilder indent = new StringBuilder();
		Map<String, String> bnodes = new HashMap<>();
		Map<URI, String> usedPrefixes = new HashMap<>();

		Result(INamespaces ns) {
			this.ns = ns;
		}

		Result append(Object o) {
			sb.append(o instanceof IReference ? encode((IReference) o) : o);
			return this;
		}

		Result append(String s) {
			sb.append(s);
			return this;
		}

		Result dedent() {
			int start = Math.max(0, indent.length() - 2);
			int end = Math.min(indent.length(), start + 2);
			indent.replace(start, end, "");
			return this;
		}

		String newVar() {
			return "?var" + nextVar++;
		}

		String encode(IReference ref) {
			URI uri = ref.getURI();
			if (uri != null) {
				// support for wild cards
				if (PATTERNS.ANY.equals(uri)) {
					return newVar();
				}

				URI uriNS = uri.namespace();
				String prefix = ns.getPrefix(uriNS);
				if (prefix != null) {
					if (!usedPrefixes.containsKey(uriNS)) {
						usedPrefixes.put(uriNS, prefix);
					}
					return prefix + ":" + uri.localPart();
				} else {
					return "<" + uri.toString().replace("\\", "\\\\").replace(">", "\\>") + ">";
				}
			}
			String refStr = ref.toString();
			String nodeVar = bnodes.get(refStr);
			if (nodeVar == null) {
				nodeVar = "?n" + nextNode++;
				bnodes.put(refStr, nodeVar);
			}
			return nodeVar;
		}

		Result indent() {
			indent.append("  ");
			return this;
		}

		Result newLine() {
			sb.append("\n").append(indent);
			return this;
		}

		@Override
		public String toString() {
			return sb.toString();
		}
	}

	INamespaces ns;

	static final String anyChildPath = "(math:arguments|math:symbol|math:operator|math:target|math:variables|"
			+ "math:binder|math:body|math:attributeKey|math:attributeValue|rdf:first|rdf:rest)";

	static final String listItemPath = "rdf:first|(rdf:rest+/rdf:first)";

	final Set<URI> MATCH_OPS = new HashSet<>(Arrays.asList(PATTERNS.ROOT, PATTERNS.ARGUMENT, PATTERNS.DESCENDANT,
			PATTERNS.SELF_OR_DESCENDANT, PATTERNS.ALL_OF, PATTERNS.ANY_OF, PATTERNS.NONE_OF));

	public PopcornPatternToSparql(final INamespaces ns) {
		this.ns = new INamespaces() {
			@Override
			public String getPrefix(URI namespace) {
				if (namespace.equals(NWMATH.NAMESPACE_URI)) {
					return "math";
				} else if (namespace.equals(RDF.NAMESPACE_URI)) {
					return "rdf";
				}
				return ns != null ? ns.getPrefix(namespace) : null;
			}

			@Override
			public URI getNamespace(String prefix) {
				if ("math".equals(prefix)) {
					return NWMATH.NAMESPACE_URI;
				} else if ("rdf".equals(prefix)) {
					return RDF.NAMESPACE_URI;
				}
				return ns != null ? ns.getNamespace(prefix) : null;
			}
		};
	}

	protected void expandMatchOp(IReference target, IReference opApplication, IReference op, IGraph graph,
			Result result, Set<IReference> seen) {
		if (seen.add(opApplication)) {
			String propertyPath = null;
			boolean isUnion = false;
			boolean isNot = false;
			if (PATTERNS.ROOT.equals(op)) {
				isUnion = true;
			} else if (PATTERNS.ARGUMENT.equals(op)) {
				propertyPath = listItemPath;
			} else if (PATTERNS.DESCENDANT.equals(op)) {
				propertyPath = anyChildPath + "+";
			} else if (PATTERNS.SELF_OR_DESCENDANT.equals(op)) {
				propertyPath = anyChildPath + "*";
			} else if (PATTERNS.ANY_OF.equals(op)) {
				isUnion = true;
			} else if (PATTERNS.ALL_OF.equals(op)) {
				isUnion = false;
			} else if (PATTERNS.NONE_OF.equals(op)) {
				isNot = true;
			}
			IReference list = graph.filter(opApplication, NWMATH.PROPERTY_ARGUMENTS, null).objectReference();
			int count = 0;
			int closeParens = 0;
			while (list != null && !RDF.NIL.equals(list)) {
				IReference first = graph.filter((IReference) list, RDF.PROPERTY_FIRST, null).objectReference();
				IReference nextList = first != null
						? graph.filter((IReference) list, RDF.PROPERTY_REST, null).objectReference()
						: null;
				if (RDF.NIL.equals(nextList)) {
					nextList = null;
				}
				if (first != null) {
					if (isNot && count == 0) {
						result.newLine().append("FILTER NOT EXISTS {").indent();
						closeParens++;
					}
					if (isUnion || isNot) {
						if (count > 0) {
							result.dedent().newLine().append("} UNION {").indent();
						} else if (nextList != null) {
							result.newLine().append("{").indent();
							closeParens++;
						}
					}
					
					if (first.getURI() != null && !graph.contains(first, null, null)) {
						// result.newLine().append("FILTER (").append(target).append(" = ").append(first).append(") . ");
						result.newLine().append(target).append(" ").append(propertyPath != null ? propertyPath : "<x:y>?").append(" ").append(first).append(" . ");
					} else {
						if (propertyPath == null) {
							// unify variable name of target and first
							result.bnodes.put(first.toString(), result.bnodes.get(target.toString()));
						}
						toSparql(first, graph, result, seen);
						// add path last since other patterns may be evaluated faster
						if (propertyPath != null) {
							result.newLine().append(target).append(" ").append(propertyPath).append(" ").append(first).append(" . ");
						}
					}
					count++;
				}
				list = nextList;
			}
			while (closeParens > 0) {
				closeParens--;
				result.dedent().newLine().append("}");
			}
			if (PATTERNS.ROOT.equals(op)) {
				result.newLine().append("FILTER NOT EXISTS {").indent();
				result.newLine().append("[]").append(" ").append(anyChildPath).append(" ").append(target).append(" . ");
				result.dedent().newLine().append("}");
			}
		}
	}

	protected void toSparql(IReference s, IGraph graph, Result result, Set<IReference> seen) {
		Object op = graph.filter(s, NWMATH.PROPERTY_OPERATOR, null).objectValue();
		if (op instanceof IReference && MATCH_OPS.contains(op)) {
			expandMatchOp(s, s, (IReference) op, graph, result, seen);
		} else if (seen.add(s)) {
			IGraph stmts = graph.filter(s, null, null);
			for (IStatement stmt : stmts) {
				IReference p = stmt.getPredicate();
				Object o = stmt.getObject();
				if (RDF.PROPERTY_TYPE.equals(p) && RDF.TYPE_LIST.equals(o)) {
					// skip list type and list closing
					continue;
				}
				if (NWMATH.PROPERTY_NAME.equals(p) && o.toString().startsWith("\"_\"")) {
					// skip special variable with name "_" since it is
					// interpreted as wildcard
					continue;
				}
				result.newLine().append(s).append(" ").append(p).append(" ").append(o).append(" . ");
				if (NWMATH.PROPERTY_ARGUMENTS.equals(p)) {
					if (RDF.NIL.equals(o)) {
						// do not match empty argument lists
						continue;
					}
					
					// special handling for matching all arguments with .,
					IReference first = graph.filter((IReference) o, RDF.PROPERTY_FIRST, null).objectReference();
					Object firstOp = graph.filter(first, NWMATH.PROPERTY_OPERATOR, null).objectValue();
					if (PATTERNS.ARGUMENT.equals(firstOp)) {
						expandMatchOp((IReference) o, first, (IReference) firstOp, graph, result, seen);
						continue;
					}
				}
				if (o instanceof IReference) {
					toSparql((IReference) o, graph, result, seen);
				}
			}
		}
	}

	protected Result toSparql(String popcornPattern, String rootVariable) {
		PopcornParser parser = Parboiled.createParser(PopcornParser.class, ns);
		try (IEntityManagerFactory emFactory = RdfHelpers.createInMemoryEMFactory();
				IEntityManager em = emFactory.get()) {
			// parse Popcorn expression into OMObject representation
			final ParsingResult<IReference> result = new ReportingParseRunner<IReference>(
					parser.Start(new NWMathBuilder(em, ns))).run(popcornPattern);
			if (result.matched) {
				// convert to RDF representation
				IReference mathExpr = result.resultValue;
				IGraph graph = new LinkedHashGraph(em.match(null, null, null).toList());
				Result sparqlResult = new Result(ns);
				if (!rootVariable.startsWith("?")) {
					rootVariable = "?" + rootVariable;
				}
				if (graph.filter(mathExpr, null, null).isEmpty()) {
					sparqlResult.newLine().append("BIND ( ").append(mathExpr).append(" as ").append(rootVariable)
							.append(" ) . ");
				} else {
					sparqlResult.bnodes.put(mathExpr.toString(), rootVariable);
					toSparql(mathExpr, graph, sparqlResult, new HashSet<IReference>());
					sparqlResult.newLine().append("FILTER NOT EXISTS { ").append(rootVariable)
							.append(" rdf:first [] }");
				}
				return sparqlResult;
			}
		}
		return null;
	}

	public String toSparqlPatterns(String popcornPattern, String rootVariable) {
		Result sparqlResult = toSparql(popcornPattern, rootVariable);
		return sparqlResult != null ? sparqlResult.sb.toString() : null;
	}

	public String toSparqlSelect(String popcornPattern) {
		return toSparqlSelect(popcornPattern, "result");
	}

	public String toSparqlSelect(String popcornPattern, String rootVariable) {
		Result sparqlResult = toSparql(popcornPattern, rootVariable);
		if (sparqlResult != null) {
			String where = sparqlResult.sb.toString();
			StringBuilder prefixes = new StringBuilder();
			Set<String> seen = new HashSet<>();
			for (Map.Entry<URI, String> e : sparqlResult.usedPrefixes.entrySet()) {
				seen.add(e.getValue());
				prefixes.append(SparqlUtils.prefix(e.getValue(), e.getKey().toString()));
			}
			if (!seen.contains("math")) {
				prefixes.append(SparqlUtils.prefix("math", NWMATH.NAMESPACE));
			}
			if (!seen.contains("rdf")) {
				prefixes.append(SparqlUtils.prefix("rdf", RDF.NAMESPACE));
			}
			return prefixes + "\nSELECT DISTINCT ?result WHERE {" + where + " }";
		}
		return null;
	}
}
package org.numerateweb.math.edit;

import java.util.ArrayList;
import java.util.List;

import net.enilink.komma.common.command.CommandResult;
import net.enilink.komma.common.command.ICommand;
import net.enilink.komma.common.command.SimpleCommand;
import net.enilink.komma.core.IEntity;
import net.enilink.komma.core.IEntityManager;
import net.enilink.komma.core.IReference;
import net.enilink.komma.core.IStatement;
import net.enilink.komma.edit.assist.IContentProposal;
import net.enilink.komma.edit.assist.IContentProposalProvider;
import net.enilink.komma.edit.assist.ParboiledProposalProvider;
import net.enilink.komma.edit.assist.ReflectiveSemanticProposals;
import net.enilink.komma.edit.properties.IProposalSupport;
import net.enilink.komma.edit.properties.ResourceEditingSupport;
import net.enilink.komma.edit.properties.ResourceFinder;
import net.enilink.komma.edit.properties.ResourceFinder.Match;
import net.enilink.komma.edit.properties.ResourceFinder.Options;
import net.enilink.komma.edit.provider.IItemLabelProvider;
import net.enilink.vocab.rdf.Property;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.numerateweb.math.model.Builder;
import org.numerateweb.math.ns.INamespaces;
import org.numerateweb.math.ns.Namespaces;
import org.numerateweb.math.popcorn.PopcornParser;
import org.numerateweb.math.rdf.NWMathBuilder;
import org.numerateweb.math.rdf.rules.Constraint;
import org.numerateweb.math.rdf.rules.NWRULES;
import org.parboiled.Parboiled;
import org.parboiled.Rule;
import org.parboiled.errors.ErrorUtils;
import org.parboiled.parserunners.ReportingParseRunner;
import org.parboiled.support.ParsingResult;

public class PopcornEditingSupport extends ResourceEditingSupport {
	static class ConstraintParser extends PopcornParser {
		public ConstraintParser(INamespaces ns) {
			super(ns);
		}

		public Rule Start(Builder<?> builder) {
			return Sequence(IriRef(), "=", super.Start(builder));
		}
	}

	class PopcornProposals extends ReflectiveSemanticProposals {
		IEntityManager entityManager;

		public PopcornProposals(IEntityManager entityManager) {
			this.entityManager = entityManager;
		}

		public IContentProposal[] IriRef(ParsingResult<?> result, int index,
				String prefix) {
			StringBuilder text = new StringBuilder();
			for (int l = 1; l <= result.inputBuffer.getLineCount(); l++) {
				text.append(result.inputBuffer.extractLine(l));
			}

			int insertPos = index - prefix.length();

			List<IContentProposal> proposals = new ArrayList<IContentProposal>();
			Options options = Options.create(entityManager, null, prefix, 20);
			for (Match match : new ResourceFinder().findAnyResources(options)) {
				String label = getLabel(match.resource);
				String origText = text.substring(insertPos, index);
				// insert proposal text
				text.replace(insertPos, index, label);
				// create proposal
				proposals.add(new ResourceProposal(text.toString(), insertPos
						+ label.length(), match.resource));
				// restore original text
				text.replace(insertPos, insertPos + label.length(), origText);
			}
			return proposals.toArray(new IContentProposal[proposals.size()]);
		}
	};

	public PopcornEditingSupport(MathAdapterFactory adapterFactory) {
		super(adapterFactory.getRootAdapterFactory());
	}

	@Override
	public boolean canEdit(Object element) {
		return true;
	}

	@Override
	public ICommand convertEditorValue(final Object editorValue,
			final IEntityManager entityManager, final Object element) {
		return new SimpleCommand() {
			@Override
			protected CommandResult doExecuteWithResult(
					IProgressMonitor progressMonitor, IAdaptable info)
					throws ExecutionException {
				boolean editConstraint = false;
				Class<? extends PopcornParser> parserClass = PopcornParser.class;
				if (element instanceof IStatement
						&& NWRULES.PROPERTY_CONSTRAINT
								.equals(((IStatement) element).getPredicate())) {
					parserClass = ConstraintParser.class;
					editConstraint = true;
				}
				INamespaces ns = new Namespaces(entityManager);
				PopcornParser parser = Parboiled.createParser(parserClass, ns);
				// parse Popcorn expression into OMObject representation
				final ParsingResult<IReference> result = new ReportingParseRunner<IReference>(
						parser.Start(new NWMathBuilder(entityManager, ns)))
						.run((String) editorValue);
				if (result.matched) {
					// convert to RDF representation
					IReference mathExpr = result.resultValue;
					if (editConstraint) {
						// we are editing a constraint of the form:
						// property := expression
						Constraint constraint = entityManager
								.create(Constraint.class);
						constraint
								.setOnProperty(entityManager.find(
										toURI(entityManager,
												result.valueStack.peek(1)),
										Property.class));
						constraint.setExpression(mathExpr);
						return CommandResult.newOKCommandResult(constraint);
					} else {
						return CommandResult.newOKCommandResult(mathExpr);
					}
				} else {
					return CommandResult.newErrorCommandResult(ErrorUtils
							.printParseErrors(result.parseErrors));
				}
			}
		};
	}

	@Override
	public IProposalSupport getProposalSupport(Object element) {
		if (element instanceof IStatement) {
			IStatement stmt = (IStatement) element;
			final IEntityManager em = ((IEntity) stmt.getSubject())
					.getEntityManager();

			final IItemLabelProvider resourceLabelProvider = super
					.getProposalSupport(element).getLabelProvider();
			Class<? extends PopcornParser> parserClass = PopcornParser.class;
			if (NWRULES.PROPERTY_CONSTRAINT.equals(stmt.getPredicate())) {
				parserClass = ConstraintParser.class;
			}
			final PopcornParser parser = Parboiled.createParser(parserClass,
					new Namespaces(em));
			return new IProposalSupport() {
				@Override
				public IContentProposalProvider getProposalProvider() {
					return new ParboiledProposalProvider(parser.Start(null),
							new PopcornProposals(em));
				}

				@Override
				public char[] getAutoActivationCharacters() {
					return null;
				}

				@Override
				public IItemLabelProvider getLabelProvider() {
					return new IItemLabelProvider() {
						@Override
						public String getText(Object object) {
							if (object instanceof ResourceProposal) {
								return resourceLabelProvider.getText(object);
							}
							return ((IContentProposal) object).getLabel();
						}

						@Override
						public Object getImage(Object object) {
							if (object instanceof ResourceProposal) {
								return resourceLabelProvider.getImage(object);
							}
							return null;
						}
					};
				}
			};
		}
		return null;
	}
}

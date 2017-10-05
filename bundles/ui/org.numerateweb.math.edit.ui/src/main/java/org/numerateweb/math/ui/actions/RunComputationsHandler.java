package org.numerateweb.math.ui.actions;

import java.util.Collection;
import java.util.Iterator;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;
import org.numerateweb.math.edit.ui.MathEditUIPlugin;
import org.numerateweb.math.eval.SimpleEvaluator;
import org.numerateweb.math.reasoner.CacheManager;
import org.numerateweb.math.reasoner.GuavaCacheFactory;
import org.numerateweb.math.reasoner.Reasoner;

import net.enilink.komma.core.IUnitOfWork;
import net.enilink.komma.em.concepts.IClass;
import net.enilink.komma.model.IModel;
import net.enilink.komma.model.IObject;

public class RunComputationsHandler extends AbstractHandler {
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		final ISelection selection = HandlerUtil.getCurrentSelection(event);
		if (selection instanceof IStructuredSelection) {
			final Object first = ((IStructuredSelection) selection).getFirstElement();
			if (first instanceof IObject) {
				final IModel model = ((IObject) first).getModel();
				final IUnitOfWork uow = model.getModelSet().getUnitOfWork();
				Job job = new Job("Run computations") {
					@Override
					public IStatus run(IProgressMonitor monitor) {
						long begin = System.currentTimeMillis();
						monitor.beginTask("Run computations", IProgressMonitor.UNKNOWN);
						try {
							uow.begin();
							model.getModelSet().getDataChangeSupport().setEnabled(null, false);

							Reasoner reasoner = createReasoner(model);
							for (Object element : ((IStructuredSelection) selection).toArray()) {
								if (element instanceof IClass) {
									try {
										reasoner.run((IClass) element);
									} catch (Exception e) {
										return new Status(IStatus.ERROR, MathEditUIPlugin.PLUGIN_ID, 0, e.getMessage(),
												e);
									}
								}
							}
						} finally {
							uow.end();
							monitor.done();
							model.getModelSet().getDataChangeSupport().setEnabled(null, true);

							long end = System.currentTimeMillis();
							System.out.println("Run computations took: " + ((end - begin) / 1000d) + " seconds");
						}
						return Status.OK_STATUS;
					}
				};
				job.setUser(true);
				job.schedule();
			}
		}
		return null;
	}

	public void setEnabled(Object evaluationContext) {
		if (evaluationContext instanceof IEvaluationContext) {
			Object target = ((IEvaluationContext) evaluationContext).getDefaultVariable();
			if (target instanceof Collection<?>) {
				Iterator<?> it = ((Collection<?>) target).iterator();
				setBaseEnabled(it.hasNext() && (it.next() instanceof IClass));
				return;
			}
		}
		setBaseEnabled(false);
	}

	Reasoner createReasoner(IModel model) {
		Reasoner reasoner = new Reasoner(model.getManager(),
				new SimpleEvaluator(model.getManager(), new CacheManager(
						new GuavaCacheFactory())));
		return reasoner;
	}

}

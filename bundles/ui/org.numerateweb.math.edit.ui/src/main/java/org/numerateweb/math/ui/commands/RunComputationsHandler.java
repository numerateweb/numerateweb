/*
 * Copyright (c) 2023 Numerate Web contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.numerateweb.math.ui.commands;

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
import org.numerateweb.math.reasoner.RdfModelAccess;
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
		CacheManager cacheManager = new CacheManager(new GuavaCacheFactory());
		Reasoner reasoner = new Reasoner(model.getManager(),
				new SimpleEvaluator(new RdfModelAccess(model.getManager(), cacheManager), cacheManager));
		return reasoner;
	}

}

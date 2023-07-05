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
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;
import org.numerateweb.math.popcorn.rules.MathRulesGenerator;

import net.enilink.komma.model.IModel;
import net.enilink.komma.model.IObject;

public class ExportRulesHandler extends AbstractHandler {
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		final ISelection selection = HandlerUtil.getCurrentSelection(event);
		if (selection instanceof IStructuredSelection) {
			final Object first = ((IStructuredSelection) selection).getFirstElement();
			if (first instanceof IObject) {
				final IModel model = ((IObject) first).getModel();

				String rules = new MathRulesGenerator().ontologyDocument(model.getOntology());
				System.out.println(rules);
			}
		}
		return null;
	}

	public void setEnabled(Object evaluationContext) {
		if (evaluationContext instanceof IEvaluationContext) {
			Object target = ((IEvaluationContext) evaluationContext).getDefaultVariable();
			if (target instanceof Collection<?>) {
				Iterator<?> it = ((Collection<?>) target).iterator();
				setBaseEnabled(it.hasNext() && (it.next() instanceof IObject));
				return;
			}
		}
		setBaseEnabled(false);
	}
}

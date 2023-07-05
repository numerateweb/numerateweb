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

import net.enilink.komma.core.URIs;
import net.enilink.komma.model.IModel;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.numerateweb.math.om.cd.OMCDImporter;

public class ImportOpenMathCDCommandHandler extends AbstractHandler {
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchPart part = HandlerUtil.getActiveEditor(event);
		final IModel model = part != null ? (IModel) part
				.getAdapter(IModel.class) : null;
		if (model != null) {
			Shell shell = part.getSite().getShell();
			FileDialog fileDialog = new FileDialog(shell, SWT.TITLE);
			fileDialog.setText("Import OpenMath CD to <" + model + ">");
			final String file = fileDialog.open();
			if (file != null) {
				new Job("Import OpenMath content dictionaries") {
					{
						setUser(true);
					}

					@Override
					protected IStatus run(IProgressMonitor monitor) {
						return new OMCDImporter(model).readAll(
								URIs.createFileURI(file), monitor);
					}
				}.schedule();
			}
		}
		return null;
	}
}

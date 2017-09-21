/**
 * <copyright> 
 *
 * Copyright (c) 2002, 2009 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 *   IBM - Initial API and implementation
 *
 * </copyright>
 *
 * $Id: EMFEditPlugin.java,v 1.9 2008/01/29 21:13:13 emerks Exp $
 */
package org.numerateweb.math.edit;

import net.enilink.komma.common.AbstractKommaPlugin;
import net.enilink.komma.common.util.IResourceLocator;

/**
 * The <b>Plugin</b> for the model EMF.Edit library. EMF must run within an
 * Eclipse workbench, within a headless Eclipse workspace, or just stand-alone
 * as part of some other application. To support this, all resource access
 * should be directed to the resource locator, which can redirect the service as
 * appropriate to the runtime. During stand-alone invocation no plugin
 * initialization takes place. In this case, emf.edit.resources.jar must be on
 * the CLASSPATH.
 * 
 * @see #INSTANCE
 */
public final class MathEditPlugin extends AbstractKommaPlugin {
	public static final String PLUGIN_ID = "org.numerateweb.math.edit";

	/**
	 * The singleton instance of the plugin.
	 */
	public static final MathEditPlugin INSTANCE = new MathEditPlugin();

	/**
	 * The one instance of this class.
	 */
	private static Implementation plugin;

	/**
	 * Creates the singleton instance.
	 */
	private MathEditPlugin() {
		super(new IResourceLocator[] {});
	}

	/*
	 * Javadoc copied from base class.
	 */
	@Override
	public IResourceLocator getBundleResourceLocator() {
		return plugin;
	}

	/**
	 * Returns the singleton instance of the Eclipse plugin.
	 * 
	 * @return the singleton instance.
	 */
	public static Implementation getPlugin() {
		return plugin;
	}

	/**
	 * The actual implementation of the Eclipse <b>Plugin</b>.
	 */
	public static class Implementation extends EclipsePlugin {
		/**
		 * Creates an instance.
		 */
		public Implementation() {
			super();

			// Remember the static instance.
			//
			plugin = this;
		}
	}
}

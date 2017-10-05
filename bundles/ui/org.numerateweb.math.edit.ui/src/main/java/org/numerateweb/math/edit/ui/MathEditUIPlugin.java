package org.numerateweb.math.edit.ui;

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
public final class MathEditUIPlugin extends AbstractKommaPlugin {
	public static final String PLUGIN_ID = "org.numerateweb.math.edit.ui";

	/**
	 * The singleton instance of the plugin.
	 */
	public static final MathEditUIPlugin INSTANCE = new MathEditUIPlugin();

	/**
	 * The one instance of this class.
	 */
	private static Implementation plugin;

	/**
	 * Creates the singleton instance.
	 */
	private MathEditUIPlugin() {
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

package org.numerateweb.lift

import org.osgi.framework.{BundleActivator, BundleContext}

object Activator {
  private var bundleContext: BundleContext = _
  def context = bundleContext
}

class Activator extends BundleActivator {
  def start(context: BundleContext) {
    Activator.bundleContext = context
  }

  def stop(context: BundleContext) {
    Activator.bundleContext = null
  }
}
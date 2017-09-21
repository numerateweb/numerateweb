package org.numerateweb.lift

import scala.Option.option2Iterable
import scala.collection.JavaConversions._

import org.osgi.framework.BundleActivator
import org.osgi.framework.BundleContext

import net.liftweb.http.LiftFilter
import net.liftweb.http.LiftRulesMocker.toLiftRules
import net.liftweb.osgi.OsgiBootable

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
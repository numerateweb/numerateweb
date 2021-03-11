package org.numerateweb.website

import net.enilink.platform.lift.sitemap.Menus
import net.liftweb.http._
import net.liftweb.sitemap.Loc._
import net.liftweb.sitemap._

/**
 * A class that's instantiated early and run.  It allows the application
 * to modify lift's environment
 */
class LiftModule {
  def sitemapMutator: SiteMap => SiteMap = {
    val omModel = ("model", "http://www.openmath.org/cd/")
    val entries = List[Menu](
      Menus.application("nw", List("nw"), List(
        Menu("nw.Home", S ? "Home") / "nw" / "index",
        Menu("nw.Symbols", S ? "Symbols") / "nw" / "symbols" >> QueryParameters(() => List(omModel)) submenus (
          Menu("nw.Symbol", S ? "Symbol") / "nw" / "symbol" >> Hidden),
        Menu("nw.Tools", S ? "Tools") / "nw" / "tools" >> PlaceHolder submenus (
          Menu("nw.Search", S ? "Search expressions") / "nw" / "search" >> QueryParameters(() => List(omModel)),
          Menu("nw.Popcorn2Sparql", S ? "Popcorn2Sparql") / "nw" / "popcorn2sparql"),
        // /nw/static path to be visible
        Menu(Loc("nw.Static", Link(List("nw", "static"), true, "/nw/static/index"),
          "Static Content", Hidden)))))

        Menus.sitemapMutator(entries)
  }

  def boot {
    // nothing to do here for now
  }
}

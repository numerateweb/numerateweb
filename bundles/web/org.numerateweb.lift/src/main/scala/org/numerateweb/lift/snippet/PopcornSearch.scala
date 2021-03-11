package org.numerateweb.lift.snippet

import scala.xml.NodeSeq
import org.numerateweb.math.search.PopcornPatternToSparql
import net.liftweb.http.S
import net.liftweb.util.Helpers._
import net.enilink.platform.lift.util.Globals

class PopcornSearch {
  def render = {
    val popcorn = S.param("popcorn") openOr ""
    ".popcorn-patterns" #> {
      val sparql = new PopcornPatternToSparql(null).toSparqlPatterns(popcorn, "?expr")
      <span data-pattern={ sparql }></span>
    } & "name=popcorn *" #> popcorn andThen
      "form *" #> ((n: NodeSeq) => n ++ Globals.contextModel.vend.map(m => <input type="hidden" name="model" value={ m.getURI.toString }/>))
  }
}
package org.numerateweb.lift.snippet

import net.liftweb.http.SHtml
import net.liftweb.http.js.JsCmd
import net.liftweb.util.Helpers._
import net.liftweb.util.Helpers
import net.liftweb.util.Helpers.strToCssBindPromoter
import scala.xml.NodeSeq
import net.liftweb.http.js.JE._
import net.liftweb.http.js.JsCmds.SetValById
import net.liftweb.http.S
import net.liftweb.json.JsonAST.JString
import org.numerateweb.math.search.PopcornPatternToSparql
import net.enilink.lift.util.Globals
import net.liftweb.http.js.JsCmds
import org.numerateweb.math.ns.Namespaces
import net.enilink.komma.core.URIs

class Popcorn2Sparql {
  def render = {
    def process: JsCmd = {
      val popcorn = S.param("popcorn") openOr ""
      val sparql = new PopcornPatternToSparql(null).toSparqlSelect(popcorn)
      SetValById("sparql", Str(sparql))
    }

    "*" #> ((n: NodeSeq) => n ++ SHtml.hidden(process _))
  }
}
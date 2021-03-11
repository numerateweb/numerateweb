package org.numerateweb.lift.snippet

import net.liftweb.http.{S, SHtml}
import net.liftweb.http.js.JE._
import net.liftweb.http.js.JsCmd
import net.liftweb.http.js.JsCmds.SetValById
import net.liftweb.util.Helpers._
import org.numerateweb.math.search.PopcornPatternToSparql

import scala.xml.NodeSeq

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
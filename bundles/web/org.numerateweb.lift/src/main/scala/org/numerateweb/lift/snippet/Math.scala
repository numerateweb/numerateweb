package org.numerateweb.lift.snippet

import java.net.URL

import scala.collection.JavaConversions._
import scala.xml.Node
import scala.xml.NodeSeq
import scala.xml.NodeSeq.seqToNodeSeq
import scala.xml.Text
import scala.xml.XML
import scala.xml.parsing.NoBindingFactoryAdapter

import org.numerateweb.lift.util.OMXmlBuilderWithMathml
import org.numerateweb.lift.util.PopcornBuilderWithHtml
import org.numerateweb.math.rdf.vocab.Object
import org.numerateweb.math.ns.Namespaces
import org.numerateweb.math.ns.Namespaces
import org.numerateweb.math.rdf.NWMathParser
import org.numerateweb.math.rdf.rules.Constraint

import com.sun.org.apache.xalan.internal.xsltc.trax.DOM2SAX

import net.enilink.komma.model.ModelUtil
import net.enilink.komma.core.IReference
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.TransformerException
import javax.xml.transform.TransformerFactory
import javax.xml.transform.URIResolver
import javax.xml.transform.dom.DOMResult
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamSource
import net.enilink.lift.util.CurrentContext
import net.enilink.lift.util.Globals
import net.liftweb.common.Box.box2Option
import net.liftweb.common.Empty
import net.liftweb.common.Full
import net.liftweb.http.DispatchSnippet
import net.liftweb.http.S
import net.liftweb.util.ClearNodes
import net.liftweb.util.Helpers

object Transformers {
  val basePath = "xsl/"

  lazy val documentBuilder = DocumentBuilderFactory.newInstance.newDocumentBuilder

  lazy val factory = {
    val factory = TransformerFactory.newInstance
    factory.setURIResolver(new URIResolver {
      def resolve(href: String, base: String) = {
        getClass.getClassLoader.getResource(basePath + href) match {
          case url: URL => new StreamSource(url.openStream())
          case _ => throw new TransformerException("Unable to resolve reference: " + href)
        }
      }
    })
    factory
  }

  lazy val omToMathMLTemplates = {
    val xsl = getClass.getClassLoader.getResource(basePath + "om2pmml.xsl")
    factory.newTemplates(new StreamSource(xsl.openStream))
  }

  def asXml(dom: org.w3c.dom.Node): Node = {
    val dom2sax = new DOM2SAX(dom)
    val adapter = new NoBindingFactoryAdapter
    dom2sax.setContentHandler(adapter)
    dom2sax.parse
    return adapter.rootElem
  }

  def omToMathML(omXml: org.w3c.dom.Node) = {
    val mathML = new DOMResult(Transformers.documentBuilder.newDocument.createElement("math"))
    Transformers.omToMathMLTemplates.newTransformer.transform(new DOMSource(omXml), mathML)
    mathML;
  }
}

class Math extends DispatchSnippet {
  def getLink(resource: IReference)(implicit linkto: String) = {
    linkto.replace("{}", Helpers.urlEncode(resource.toString)) +
      Globals.contextModel.vend.dmap("")(m => "&model=" + Helpers.urlEncode(m.toString))
  }

  def linkTarget(n: NodeSeq) = {
    (n \ "@data-linkto").headOption.map(_.text) getOrElse (Globals.application.vend.dmap("")(_.path.mkString("/", "/", "")) + "/describe?resource={}")
  }

  def dispatch: DispatchIt = {
    case method => CurrentContext.value match {
      case Full(c) => method match {
        case "mathml" => (n: NodeSeq) => {
          implicit val linkto: String = linkTarget(n)
          var prefix: NodeSeq = Nil
          val target = c.subject match {
            case constraint: Constraint => {
              prefix = <mtext href={ getLink(constraint.getOnProperty) }> { ModelUtil.getLabel(constraint.getOnProperty) } </mtext> ++ <mo>:=</mo>
              Full(constraint.getExpression)
            }
            case o: Object => Full(o)
            case _ => Empty
          }
          target.map { t =>
            val ns = Globals.contextModel.vend.map(m => new Namespaces(m.getManager)) getOrElse null
            val omXml = new NWMathParser(ns).parse(t, new OMXmlBuilderWithMathml(Transformers.documentBuilder, ns) {
              def getLink(resource: IReference) = Math.this.getLink(resource)
            })
            val mathML = Transformers.omToMathML(omXml)
            <math display={ S.attr("display") openOr "block" } indentalign={ S.attr("align") openOr "left" }>{ prefix ++ Transformers.asXml(mathML.getNode).child }</math>
          } getOrElse Nil
        }
        case "popcorn" => (n: NodeSeq) => {
          implicit val linkto: String = linkTarget(n)
          var prefix: NodeSeq = Nil
          val target = c.subject match {
            case constraint: Constraint => {
              prefix = <a href={ getLink(constraint.getOnProperty) }> { ModelUtil.getLabel(constraint.getOnProperty) } </a> ++ Text(" := ")
              Full(constraint.getExpression)
            }
            case o: Object => Full(o)
            case _ => Empty
          }
          target.map { t =>
            val ns = Globals.contextModel.vend.map(m => new Namespaces(m.getManager)) getOrElse null
            val xml = "<span>" + (new NWMathParser(ns).parse(t, new PopcornBuilderWithHtml(ns) {
              def getLink(resource: IReference) = Math.this.getLink(resource)
            })) + "</span>"
            prefix ++ XML.loadString(xml).child
          } getOrElse Nil
        }
        case _ => ClearNodes
      }
      // no current RDF context
      case _ => ClearNodes
    }
  }
}
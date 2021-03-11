package org.numerateweb.lift.util

import scala.xml.Utility
import org.numerateweb.math.ns.INamespaces
import org.numerateweb.math.popcorn.PopcornBuilder
import org.numerateweb.math.popcorn.PopcornExpr
import org.numerateweb.math.popcorn.PopcornLiteralBuilder
import org.numerateweb.math.xml.OM
import org.numerateweb.math.xml.OMXmlBuilder
import org.numerateweb.math.xml.OMXmlLiteralBuilder
import org.w3c.dom.Document
import org.w3c.dom.Element
import net.enilink.komma.parser.manchester.ManchesterSyntaxGenerator
import net.enilink.komma.core.IReference
import net.enilink.komma.core.URIs
import javax.xml.namespace.QName
import javax.xml.parsers.DocumentBuilder
import net.enilink.komma.core.URI

/**
 * Popcorn generator that embeds HTML links to RDF resources.
 */
abstract class PopcornBuilderWithHtml(ns: INamespaces) extends PopcornBuilder(ns) {

  def getLink(reference: IReference): String

  /**
   * Escapes the characters &lt; &gt; &amp; and &quot; from text while
   * allowing to use '\' as escape symbol to skip escaping of single characters.
   */
  final def escape(text: String, s: StringBuilder): StringBuilder = {
    val escMap = Utility.Escapes.escMap
    var skip = false
    text.foreach(c => {
      if (c == '\\' && !skip) skip = true
      else if (skip) {
        s append c
        skip = false
      } else escMap.get(c) match {
        case Some(str) => s append str
        case None => s append c
      }
    })
    s
  }

  override def build(expr: PopcornExpr) = new PopcornExpr(escape(expr.toString, new StringBuilder).toString)

  override def newLiteralBuilder(ns: INamespaces) = new PopcornLiteralBuilder(ns) {
    override def s(symbol: URI) = {
      val oms = super.s(symbol)
      oms.text("\\<a href=\\\"" + getLink(symbol) + "\\\"\\>" + oms + "\\</a\\>")
    }

    override def str(value: String) = {
      val result = super.str(value)
      result.text(result.text.replace("\\", "\\\\"))
    }

    override def rdfClass(clazz: IReference, ns: INamespaces) = {
      str(new ManchesterSyntaxGenerator {
        override def getPrefix(reference: IReference) = {
          val prefix = ns.getPrefix(reference.getURI.namespace)
          if (prefix != null) prefix else super.getPrefix(reference)
        }

        override def value(value: Any) = value match {
          case ref: IReference =>
            append("\\<a href=\\\"" + getLink(ref) + "\\\"\\>")
            val gen = super.value(value)
            append("\\</a\\>")
            gen
          case _ => super.value(value)
        }
      }.generateText(clazz))
    }

    override def ref(reference: IReference) = {
      val uri = reference.getURI
      val text = if (uri != null) {
        val prefix = ns.getPrefix(uri.namespace)
        prefix match {
          case null => "<" + uri + ">"
          case "" => uri.localPart
          case _ => prefix + ":" + uri.localPart
        }
      } else reference.toString
      new PopcornExpr("\\<a href=\\\"" + getLink(reference) + "\\\"\\>" + text + "\\</a\\>")
    }
  }
}

/**
 * OpenMath XML generator that embeds MathML with links to RDF resources.
 */
abstract class OMXmlBuilderWithMathml(builder: DocumentBuilder, ns: INamespaces) extends OMXmlBuilder(builder) {
  def getLink(reference: IReference): String

  override def newLiteralBuilder(docBuilder: DocumentBuilder, document: Document) = new OMXmlLiteralBuilder(docBuilder, document) {
    val MATHML_NS = "http://www.w3.org/1998/Math/MathML"
    def createMathml = create(new QName(MATHML_NS, "math"))
    def createMtext = create(new QName(MATHML_NS, "mtext"))
    def createMi = create(new QName(MATHML_NS, "mi"))
    def createMrow = create(new QName(MATHML_NS, "mrow"))

    // add specific MathML rendering as attribute
    def withMathml(mathml: org.w3c.dom.Node, target: org.w3c.dom.Node) = {
      val children = mathml.getChildNodes
      val length = children.getLength
      // optionally add <mrow> for grouping
      if (length > 1) {
        val mrow = createMrow
        // child is removed from children, hence use index 0 instead of i
        for (i <- 0 until length) mrow.appendChild(children.item(0))
        mathml.appendChild(mrow)
      }

      val omattr = create(OM.OMATTR);
      val omatp = create(OM.OMATP);
      omatp.appendChild(s(URIs.createURI("http://www.openmath.org/cd/altenc#MathML-Presentation")))
      omatp.appendChild(foreign("text/mathml", mathml.getFirstChild))
      omattr.appendChild(omatp);
      omattr.appendChild(target);
      omattr
    }

    override def s(symbol: URI) = super.s(symbol) match {
      case e: Element =>
        e.setAttribute("href", getLink(symbol)); e
      case other => other
    }

    override def rdfClass(clazz: IReference, ns: INamespaces) = {
      val mathml = createMathml
      val node = str(new ManchesterSyntaxGenerator {
        val mtext = new StringBuilder
        override def getPrefix(reference: IReference) = {
          val prefix = ns.getPrefix(reference.getURI.namespace)
          if (prefix != null) prefix else super.getPrefix(reference)
        }

        override def generateText(value: Any) = {
          val text = super.generateText(value)
          appendMTEXT(null)
          text
        }

        override def append(value: Any) = {
          mtext.append(value.toString.replace(" ", "&nbsp;"))
          super.append(value)
        }

        def appendMTEXT(href: String) {
          if (mtext.length > 0) {
            val e = createMtext
            e.setTextContent(mtext.toString)
            if (href != null) e.setAttribute("href", href)
            mathml.appendChild(e)
            mtext.clear
          }
        }

        override def value(value: Any) = value match {
          case ref: IReference =>
            appendMTEXT(null)
            val gen = super.value(value)
            appendMTEXT(getLink(ref))
            gen
          case _ => super.value(value)
        }
      }.generateText(clazz))
      withMathml(mathml, node)
    }

    override def ref(reference: IReference) = {
      val uri = reference.getURI
      val text = if (uri != null) {
        val prefix = ns.getPrefix(uri.namespace)
        prefix match {
          case null => "<" + uri + ">"
          case "" => uri.localPart
          case _ => prefix + ":" + uri.localPart
        }
      } else reference.toString

      val mathml = createMathml
      val mi = createMi
      mi.setTextContent(text)
      mi.setAttribute("href", getLink(reference))
      mathml.appendChild(mi)
      withMathml(mathml, str(text))
    }
  }
}
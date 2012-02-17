package com.tinylabproductions.as3stacktracer.parser

import util.matching.Regex
import util.matching.Regex.MatchData

/**
 * Created by IntelliJ IDEA.
 * User: arturas
 * Date: 2/17/12
 * Time: 2:09 PM
 * To change this template use File | Settings | File Templates.
 */

abstract class Scope(
  name: String,
  protected[this] val parent: Option[Scope]
) {
  protected[this] val matchers: List[Scope.Matcher]
  protected[this] val scopeType: String

  private[this] var _parts = List.empty[Either[String, Scope]]
  def parts = _parts
  
  protected[this] def addPart(s: String) { _parts = parts :+ Left(s) }
  protected[this] def addPart(s: Scope) { _parts = parts :+ Right(s) }

  protected[this] val buffer: StringBuilder = new StringBuilder

  override def toString = "<Scope.%s name: %s, parent: %s>".format(
    scopeType, name, parent.toString
  )

  def append(char: Char): Scope = {
    buffer.append(char)
    tryToMatch() match {
      case Some(scope) => scope
      case None => this
    }
  }

  private[this] def tryToMatch(): Option[Scope] = {
    val bufferString = buffer.toString()
    matchers.foreach { matcher =>
      matcher.attempt(bufferString, this) match {
        case Scope.Matcher.Failure => ()
        case Scope.Matcher.Success(before, scope) =>
          buffer.clear()
          addPart(before)
          addPart(scope)
          return Some(scope)
      }
    }

    None
  }

  def qualifiedName: String

  def fullName = fullNameImpl.mkString("/")
  private def fullNameImpl: List[String] = parent match {
    case None => List(qualifiedName)
    case Some(p) => p.fullNameImpl :+ qualifiedName
  }
}

object Scope {
  private[Scope] abstract class Block(
    body: String,
    name: String,
    parent: Scope
  ) extends Scope(name, Some(parent)) {
    addPart(body)

    // 1 because initial opening brace is consumed in matching.
    private[this] var blockCounter = 1

    override def append(char: Char): Scope = {
      // Check for closing bracket before trying to match.
      if (char == '}')
        blockCounter -= 1

      if (blockCounter == 0) {
        onClose(char)
        parent
      }
      else {
        val newScope = super.append(char)
        // Check for opening bracket only if new scope hasn't been opened.
        if (newScope == this && char == '{')
          blockCounter += 1
        newScope
      }
    }

    protected def onClose(char: Char) {
      buffer.append(char)
      addPart(buffer.toString())
      buffer.clear()
    }
  }

  private[parser] object Matcher {
    sealed abstract class Result
    case class Success(before: String, scope: Scope) extends Result
    case object Failure extends Result
  }
  
  private[parser] trait Matcher {
    protected[this] val matcher: Regex

    def attempt(buffer: String, parent: Scope): Matcher.Result = {
      matcher.findFirstMatchIn(buffer) match {
        case None => Matcher.Failure
        case Some(matchData) =>
          val matched = matchData.group(0)
          Matcher.Success(
            buffer.substring(0, buffer.length() - matched.length()),
            createScope(matchData, parent)
          )
      }
    }

    protected[this] def createScope(
      matchData: MatchData, parent: Scope
    ): Scope
  }
  
  class File(name: String) extends Scope(name, None) {
    protected[this] val scopeType = "File"
    def qualifiedName = name
    
    protected val matchers = List(Package, Class, Function)
  }

  object Package extends Matcher {
    protected[this] val matcher =
      """package(\s|\n)+([a-zA-Z_][\w\.]*)?(\s|\n)*\{""".r

    private[Package] val Import =
      "\nimport com.tinylabproductions.stacktracer.StacktraceError;\n"

    protected[this] def createScope(
      matchData: MatchData, parent: Scope
    ) = {
      val name = matchData.group(2) match {
        case null => "root_pkg"
        case s: String => s
      }
      val body = matchData.group(0)
      new Package(body, name, parent)
    }
  }

  class Package(body: String, name: String, parent: Scope)
    extends Block(body, name, parent)
  {
    buffer.append(Package.Import)

    protected[this] val scopeType = "Package"
    def qualifiedName = "pkg:" + name

    protected val matchers = List(Class, Function)

    override protected def onClose(char: Char) {
      super.onClose(char)
      addPart(Package.Import)
    }
  }

  object Class extends Matcher {
    protected[this] val scopeType = "Class"
    protected[this] val matcher = """class(\s|\n)+([a-zA-Z_]\w*)(\s|\n)*\{""".r

    protected[this] def createScope(
      matchData: MatchData, parent: Scope
    ) = {
      val body = matchData.group(0)
      val name = matchData.group(2)
      new Class(body, name, parent)
    }
  }

  class Class(body: String, name: String, parent: Scope)
    extends Block(body, name, parent)
  {
    protected[this] val scopeType = "Class"
    def qualifiedName = name

    protected val matchers = List(Function)
  }

  object Function extends Matcher {
    protected[this] val matcher =
      """(?ix)
      (
        (var|const)(\s|\n)+([a-zA-Z_][\w]*).+?=(\s|\n)*
      )? # anonymous functions assigned with var a = function
      function(\s|\n)*
      ([a-zA-Z_][\w]*)? # functions might not have a name
      (\s\n)*
      .*? # arg list and type info
      \{""".r

    protected[this] def createScope(
      matchData: MatchData, parent: Scope
    ) = {
      val name = matchData.group(7) match {
        case null => matchData.group(4) // Anonymous function via var.
        case s: String => s // Named function.
      }
      val body = matchData.group(0)
      new Function(body, name, parent)
    }
  }

  class Function(body: String, name: String, parent: Scope)
    extends Block(body, name, parent)
  {
    protected[this] val scopeType = "Function"
    buffer.append(" try {")

    def qualifiedName = name + "()"

    protected val matchers = List(Function)

    override protected def onClose(char: Char) {
      buffer.append(
        """} catch (e: Error) { throw StacktraceError.trace(e, "%s"); } """.
          format(fullName)
      )
      super.onClose(char)
    }
  }
}
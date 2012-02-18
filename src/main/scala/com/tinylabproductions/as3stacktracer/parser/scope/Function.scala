package com.tinylabproductions.as3stacktracer.parser.scope

import util.matching.Regex.MatchData
import com.tinylabproductions.as3stacktracer.parser.Scope

/**
 * Created by IntelliJ IDEA.
 * User: arturas
 * Date: 2/18/12
 * Time: 12:06 PM
 * To change this template use File | Settings | File Templates.
 */

private[scope] object Function extends Matcher {
  protected[this] val matcher =
    """(?x)
    (
      (var|const)(\s|\n)+([a-zA-Z_][\w]*).+?=(\s|\n)*
    )? # anonymous functions assigned with var a = function
    function
    (\s|\n)* # whitespace
    (get|set)? # getter/setter support
    (\s|\n)* # whitespace
    ([a-zA-Z_][\w]*)? # functions might not have a name
    (\s|\n)* # whitespace
    \((.*?)\) # arg list
    .*? # type info
    (\s|\n)* # whitespace
    \{""".r

  private[this] val ArgMatcher = """(?x)
    ([a-zA-Z_][\w]*) # argument name
    (\s|\n)* # whitespace
    (:(\s|\n)*.+?)? # type declaration
    (\s|\n)* # whitespace
    (,|$) # arg separator or end of list
  """.r

  protected[this] def createScope(
    matchData: MatchData, parent: Scope
  ) = {
    val functionScope = matchData.group(7) match {
      case null => None
      case s: String => Some(s)
    }

    val name = matchData.group(9) match {
      case null => matchData.group(4) // Anonymous function via var.
      case s: String => s // Named function.
    }
    val body = matchData.group(0)
    val scope = new Function(functionScope, body, name, parent)

    // Match function arguments
    val argList = matchData.group(11)
    ArgMatcher.findAllIn(argList).matchData.foreach { argMatchData =>
      val argName = argMatchData.group(1)
      scope.addVariable(argName)
    }
    
    scope
  }
}

private[scope] class Function(
  functionScope: Option[String], body: String, name: String, parent: Scope
) extends Block(body, name, parent) with CurlyBlock with HasVariables
{
  protected[this] val scopeType = "Function"
  addPart(" try {")

  def qualifiedName = "%s%s()".format(
    functionScope match {
      case Some(s) => "%s:".format(s)
      case None => ""
    },
    name
  )

  protected val matchers = List(LocalVariable, Function)

  protected def onClose() {
    addPart(
      """} catch (e: Error) { throw StacktraceError.trace(e, "%s", %s); } """.
        format(fullName, variablesString)
    )
  }
}
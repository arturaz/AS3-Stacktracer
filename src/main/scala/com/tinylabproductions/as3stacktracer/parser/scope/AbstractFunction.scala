package com.tinylabproductions.as3stacktracer.parser.scope

import com.tinylabproductions.as3stacktracer.parser.Scope
import util.matching.Regex.MatchData

/**
 * Created by IntelliJ IDEA.
 * User: arturas
 * Date: 2/20/12
 * Time: 10:04 AM
 * To change this template use File | Settings | File Templates.
 */

private[scope] object AbstractFunction {
  private[scope] val Matcher =
    """(?xs)
    (
      (var|const)(\s|\n)+([a-zA-Z_][\w]*).+?=(\s|\n)*
    )? # anonymous functions assigned with var a = function
    function
    (\s|\n)* # whitespace
    (get|set)? # getter/setter support
    (\s|\n)* # whitespace
    ([a-zA-Z_][\w]*)? # functions might not have a name
    (\s|\n)* # whitespace
    \((.*?)\) # arg list, may contain newlines
    .*? # type info
    (\s|\n)* # whitespace
    \{""".r
  
  private[scope] def createScope[T >: AbstractFunction](
    matchData: MatchData, parent: Scope
  )(
    create: (Option[String], String, String, String, Scope) => T
  ): T = {
    val functionScope = matchData.group(7) match {
      case null => None
      case s: String => Some(s)
    }

    val name = matchData.group(9) match {
      case null => matchData.group(4) // Anonymous function via var.
      case s: String => s // Named function.
    }
    val body = matchData.group(0)
    val argList = matchData.group(11)
    create(functionScope, argList, body, name, parent)
  }

  private[AbstractFunction] val ArgMatcher = """(?x)
    ([a-zA-Z_][\w]*) # argument name
    (\s|\n)* # whitespace
    (:(\s|\n)*.+?)? # type declaration
    (\s|\n)* # whitespace
    (,|$) # arg separator or end of list
  """.r
}

private[scope] abstract class AbstractFunction(
  functionScope: Option[String],
  argList: String,
  body: String,
  name: String,
  parent: Scope
) extends Block(body, name, parent) with CurlyBlock with HasVariables {
  protected val matchers = List(LocalVariable, LocalFunction)
  addPart(" try {")

  // Add arguments.
  AbstractFunction.ArgMatcher.findAllIn(argList).matchData.foreach { matchData =>
    val body = matchData.group(0)
    val name = matchData.group(1)
    val variable = new LocalVariable(body, name, this)
    addVariable(variable)
  }

  def qualifiedName = "%s%s()".format(
    functionScope match {
      case Some(s) => "%s:".format(s)
      case None => ""
    },
    name
  )

  protected def onClose() {
    addPart(
      """} catch (e: Error) { throw StacktraceError.trace(e, "%s", %s); } """.
        format(fullName, variablesString)
    )
  }
}

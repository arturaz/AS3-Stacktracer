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
  private[AbstractFunction] val Anonymous = "_anon_"
  private[AbstractFunction] val ExceptionName = "___st_e"

  private[scope] val Matcher =
    """(?xs)
    (
      (var|const)(\s|\n)+([a-zA-Z_][\w]*).+?=(\s|\n)*
    )? # anonymous functions assigned with var a = function
    function
    (\s|\n)* # whitespace
    (get\b|set\b)? # getter/setter support, \b ensures we don't match getFoo()
    (\s|\n)* # whitespace
    ([a-zA-Z_][\w]*)? # functions might not have a name
    (\s|\n)* # whitespace
    \((.*?)\) # arg list, may contain newlines
    (\s|\n)* # whitespace
    (
      : # type info
      (\s|\n)* # whitespace
      ([a-zA-Z_\*][\w\.<>]*) # function return type
    )?
    (\s|\n)* # whitespace
    \{""".r
  
  private[scope] def createScope[T >: AbstractFunction](
    matchData: MatchData, parent: Scope
  )(
    create: (Option[String], String, Type, String, Option[String], Scope) =>
      Option[T]
  ): Option[T] = {
    val functionScope = matchData.group(7) match {
      case null => None
      case s: String => Some(s)
    }

    val name = matchData.group(9) match {
      case null => matchData.group(4) match {
        case null => None // Truly anonymous function.
        case s: String => Some(s)  // Anonymous function via var.
      }
      case s: String => Some(s) // Named function.
    }
    val body = matchData.group(0)
    val argList = matchData.group(11)
    val returnType = createReturnType(matchData.group(15))
    create(functionScope, argList, returnType, body, name, parent)
  }

  private[scope] def createReturnType(signature: String) = signature match {
    case null => Type.Void
    case s: String => Type.create(s)
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
  returnType: Type,
  body: String,
  name: Option[String],
  parent: Scope
) extends Block(body, name.getOrElse(AbstractFunction.Anonymous), parent)
  with CurlyBlock with HasVariables
{
  addPart(" try {")

  protected val matchers = List(Comment, LocalVariable, LocalFunction)

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
    name.getOrElse(AbstractFunction.Anonymous)
  )

  protected def onClose() {
    addPart(
      """} catch (%s: Error) { throw StacktraceError.trace(%s, "%s", %s); } %s """.
        format(
          AbstractFunction.ExceptionName, AbstractFunction.ExceptionName,
          fullName, variablesString, returnType.returnValue
        )
    )
  }
}

package com.tinylabproductions.as3stacktracer.parser.scope

import com.tinylabproductions.as3stacktracer.parser.Scope
import util.matching.Regex.MatchData

/**
 * Created by IntelliJ IDEA.
 * User: arturas
 * Date: 2/18/12
 * Time: 5:08 PM
 * To change this template use File | Settings | File Templates.
 */


private[scope] object StaticFunction extends Matcher {
  protected[this] val matcher =
    """(?xs)
    static.+?
    function
    (\s|\n)* # whitespace
    (get|set)? # getter/setter support
    (\s|\n)* # whitespace
    ([a-zA-Z_][\w]*) # functions name
    (\s|\n)* # whitespace
    \((.*?)\) # arg list, may contain newlines
    .*? # type info
    (\s|\n)* # whitespace
    \{""".r

  protected[this] def createScope(
    matchData: MatchData, parent: Scope
  ) = {
    val functionScope = matchData.group(2) match {
      case null => None
      case s: String => Some(s)
    }
    val name = matchData.group(4)
    val body = matchData.group(0)
    val argList = matchData.group(6)
    new StaticFunction(functionScope, argList, body, name, parent)
  }
}

private[scope] class StaticFunction(
  functionScope: Option[String],
  argList: String,
  body: String,
  name: String,
  parent: Scope
) extends AbstractFunction(functionScope, argList, body, name, parent)
{
  override protected[this] val scopeType = "StaticFunction"

  override def qualifiedName = ":%s".format(super.qualifiedName)

  // Include static variables if we're in class.
  override protected def variablesString = parent match {
    case clazz: Class => "%s(%s)".format(Class.SVarsMethodName, super.variablesString)
    case _ => throw new RuntimeException("%s parent is not Class, but %s!".format(
      this, parent
    ))
  }
}
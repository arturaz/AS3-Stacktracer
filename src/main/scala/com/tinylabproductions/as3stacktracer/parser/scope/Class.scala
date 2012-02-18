package com.tinylabproductions.as3stacktracer.parser.scope

import util.matching.Regex.MatchData
import com.tinylabproductions.as3stacktracer.parser.Scope

/**
 * Created by IntelliJ IDEA.
 * User: arturas
 * Date: 2/18/12
 * Time: 12:04 PM
 * To change this template use File | Settings | File Templates.
 */

private[scope] object Class extends Matcher {
  protected[this] val matcher =
    """(?x)
    class
    (\s|\n)+ # whitespace
    ([a-zA-Z_]\w*) # class name
    (\s|\n)* # whitespace
    .*? # extends/implements
    \{""".r

  protected[this] def createScope(
    matchData: MatchData, parent: Scope
  ) = {
    val body = matchData.group(0)
    val name = matchData.group(2)
    new Class(body, name, parent)
  }
}

private[scope] class Class(body: String, name: String, parent: Scope)
  extends Block(name, parent) with CurlyBlock with HasVariables
{
  parent match {
    case f: File => f.addNonPackageImport()
    case _ => ()
  }
  addPart("%s // %s\n".format(body, fullName))

  protected[this] val scopeType = "Class"
  def qualifiedName = name

  protected val matchers = List(StaticVariable, InstanceVariable, Function)

  protected[this] def onClose() {}
}
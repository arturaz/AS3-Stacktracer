package com.tinylabproductions.as3stacktracer.parser.scope

import util.matching.Regex.MatchData
import com.tinylabproductions.as3stacktracer.parser.Scope
import annotation.tailrec

/**
 * Created by IntelliJ IDEA.
 * User: arturas
 * Date: 2/18/12
 * Time: 12:06 PM
 * To change this template use File | Settings | File Templates.
 */

private[scope] object LocalFunction extends Matcher {
  protected[this] val matcher = AbstractFunction.Matcher

  protected[this] def createScope(
    matchData: MatchData, parent: Scope
  ) = AbstractFunction.createScope(matchData, parent) {
    case (functionScope, argList, returnType, body, name, parent) =>
      Some(new LocalFunction(functionScope, argList, returnType, body, name, parent))
  }
}

private[scope] class LocalFunction(
  functionScope: Option[String],
  argList: String,
  returnType: Type,
  body: String,
  name: Option[String],
  parent: Scope
) extends AbstractFunction(functionScope, argList, returnType, body, name, parent)
{
  protected[this] val scopeType = "LocalFunction"

  // Add support for anonymous functions defined in instance/static functions.
  override protected def variablesString = {
    @tailrec
    def traverse(vars: Set[Variable], par: Option[Scope]): String = par match {
      case Some(i: InstanceFunction) =>
        HasVariables.toClassString(i.variables ++ vars)
      case Some(s: StaticFunction) =>
        HasVariables.toClassString(s.variables ++ vars)
      case Some(c: Class) =>
        HasVariables.toClassString(vars)
      case Some(hv: HasVariables) =>
        traverse(hv.variables ++ vars, par.get.parent)
      case _ => super.variablesString
    }
    
    traverse(variables, Some(parent))
  }
}
package com.tinylabproductions.as3stacktracer.parser.scope

import util.matching.Regex.MatchData
import com.tinylabproductions.as3stacktracer.parser.Scope

/**
 * Created by IntelliJ IDEA.
 * User: arturas
 * Date: 2/20/12
 * Time: 10:10 AM
 * To change this template use File | Settings | File Templates.
 */

private[scope] object InstanceFunction extends Matcher {
  protected[this] val matcher = AbstractFunction.Matcher

  protected[this] def createScope(
    matchData: MatchData, parent: Scope
  ) = AbstractFunction.createScope(matchData, parent) {
    case (functionScope, argList, returnType, body, name, parent) =>
      name match {
        case None => None // Anonymous functions cannot be instance functions.
        case Some(n) =>
          Some(new InstanceFunction(
            functionScope, argList, returnType, body, name.get, parent
          ))
      }
  }
}

private[scope] class InstanceFunction(
  functionScope: Option[String],
  argList: String,
  returnType: Type,
  body: String,
  name: String,
  parent: Scope
) extends AbstractFunction(
  functionScope, argList, returnType, body, Some(name), parent
) {
  protected[this] val scopeType = "InstanceFunction"

  // Include instance variables if we are in class.
  override protected def variablesString = parent match {
    case clazz: Class => "%s(%s)".format(Class.IVarsMethodName, super.variablesString)
    case _ => throw new RuntimeException("%s parent is not Class, but %s!".format(
      this, parent
    ))
  }
}
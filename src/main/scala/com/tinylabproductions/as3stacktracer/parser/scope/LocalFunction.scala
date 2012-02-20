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

private[scope] object LocalFunction extends Matcher {
  protected[this] val matcher = AbstractFunction.Matcher

  protected[this] def createScope(
    matchData: MatchData, parent: Scope
  ) = AbstractFunction.createScope(matchData, parent) {
    case (functionScope, argList, body, name, parent) =>
      new LocalFunction(functionScope, argList, body, name, parent)
  }
}

private[scope] class LocalFunction(
  functionScope: Option[String],
  argList: String,
  body: String,
  name: String,
  parent: Scope
) extends AbstractFunction(functionScope, argList, body, name, parent)
{
  protected[this] val scopeType = "LocalFunction"
}
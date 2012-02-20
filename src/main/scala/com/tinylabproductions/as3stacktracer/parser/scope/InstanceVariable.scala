package com.tinylabproductions.as3stacktracer.parser.scope

import util.matching.Regex.MatchData
import com.tinylabproductions.as3stacktracer.parser.Scope

/**
 * Created by IntelliJ IDEA.
 * User: arturas
 * Date: 2/18/12
 * Time: 1:53 PM
 * To change this template use File | Settings | File Templates.
 */

private[scope] object InstanceVariable extends Matcher {
  protected[this] val matcher = LocalVariable.matcher

  override protected[this] def createScope(
    matchData: MatchData, parent: Scope
  ): Scope = {
    val body = matchData.group(0)
    val name = matchData.group(3)
    new InstanceVariable(body, name, parent)
  }
}

private[scope] class InstanceVariable(body: String, name: String, parent: Scope)
  extends Variable(body, name, parent)
{
  protected[this] val scopeType = "InstanceVariable"
  def qualifiedName = "this.%s".format(name)
}

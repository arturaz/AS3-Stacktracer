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
  protected[this] val matcher =
    """(?x)
      ([a-zA-Z_]\w*)? # namespace name here
      (\s|\n)+
      (var|const)
      (\s|\n)+
      ([a-zA-Z_]\w*) # name
      (:|\s|\n)+ # require something so we wouldn't match until we have read
                 # full var name
    """.r

  override protected[this] def createScope(
    matchData: MatchData, parent: Scope
  ): Scope = {
    val body = matchData.group(0)
    val namespace = ClassVariable.createNamespace(matchData.group(1))
    val name = matchData.group(5)
    new InstanceVariable(namespace, body, name, parent)
  }
}

private[scope] class InstanceVariable(
  namespace: Namespace, body: String, name: String, parent: Scope
) extends ClassVariable(namespace, body, name, parent)
{
  protected[this] val scopeType = "InstanceVariable"
  def qualifiedName = "this.%s%s".format(namespace.prefix, name)
}

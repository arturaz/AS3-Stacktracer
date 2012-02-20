package com.tinylabproductions.as3stacktracer.parser.scope

import util.matching.Regex.MatchData
import com.tinylabproductions.as3stacktracer.parser.Scope

/**
 * Created by IntelliJ IDEA.
 * User: arturas
 * Date: 2/18/12
 * Time: 1:35 PM
 * To change this template use File | Settings | File Templates.
 */

private[scope] object StaticVariable extends Matcher {
  protected[this] val matcher =
    """(?x)
      ((
        ([a-zA-Z_]\w*)? # namespace name here
        (\s|\n)+
        static
        (\s|\n)+
        (var|const)
      )|(
        static
        (\s|\n)+
        ([a-zA-Z_]\w*)? # namespace name here
        (\s|\n)+
        (var|const)
      ))
      (\s|\n)+
      ([a-zA-Z_]\w*) # name
      (:|\s|\n)+ # require something so we wouldn't match until we have read
                 # full var name
    """.r

  protected[this] def createScope(matchData: MatchData, parent: Scope) = {
    val body = matchData.group(0)
    val namespace = ClassVariable.createNamespace(
      matchData.group(3), matchData.group(9)
    )
    val name = matchData.group(13)
    Some(new StaticVariable(namespace, body, name, parent))
  }
}

private[scope] class StaticVariable(
  namespace: Namespace, body: String, name: String, parent: Scope
) extends ClassVariable(namespace, body, name, parent)
{
  override protected[this] val scopeType = "StaticVariable"
  override def qualifiedName = "%s.%s%s".format(
    parent.qualifiedName, namespace.prefix, name
  )
}
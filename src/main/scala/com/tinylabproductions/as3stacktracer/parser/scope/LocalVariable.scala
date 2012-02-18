package com.tinylabproductions.as3stacktracer.parser.scope

import util.matching.Regex.MatchData
import com.tinylabproductions.as3stacktracer.parser.Scope

/**
 * Created by IntelliJ IDEA.
 * User: arturas
 * Date: 2/18/12
 * Time: 1:50 PM
 * To change this template use File | Settings | File Templates.
 */


private[scope] object LocalVariable extends Matcher {
  protected[scope] val matcher =
    """(?x)
      (var|const)
      (\s|\n)+
      ([a-zA-Z_]\w*) # name
      (:|\s|\n)+ # require something so we wouldn't match until we have read
                 # full var name
    """.r

  protected[this] def createScope(matchData: MatchData, parent: Scope): Scope =
  {
    val body = matchData.group(0)
    val name = matchData.group(3)
    new LocalVariable(body, name, parent)
  }
}

class LocalVariable(body: String, name: String, parent: Scope)
  extends Variable(body, name, parent)
{
  protected[this] val scopeType = "LocalVariable"
  def qualifiedName = name
}

package com.tinylabproductions.as3stacktracer.parser.scope

import util.matching.Regex.MatchData
import com.tinylabproductions.as3stacktracer.parser.Scope

/**
 * Created by IntelliJ IDEA.
 * User: arturas
 * Date: 2/20/12
 * Time: 2:03 PM
 * To change this template use File | Settings | File Templates.
 */

private[scope] object Script extends Matcher {
  private[Script] val ScriptEnd = "</fx:Script>"

  protected[this] val matcher = """(?x)
    <fx:Script>
    (\s|\n)*
    <!\[CDATA\[
  """.r

  protected[this] def createScope(matchData: MatchData, parent: Scope) = {
    val body = matchData.group(0)
    Some(new Script(body, parent))
  }
}

class Script(body: String, parent: Scope) extends Scope("script", Some(parent)) {
  addPart("%s // %s\n%s".format(body, fullName, Package.ImportStatement))

  private[this] val bodyBuffer = new StringBuilder

  protected[this] val scopeType = "Script"
  protected[this] val matchers = List(LocalFunction, ASString)

  def qualifiedName = "script"

  // Exit script when encountering exit tag.
  override private[parser] def append(char: Char) = {
    bodyBuffer.append(char)
    if (bodyBuffer.toString().endsWith(Script.ScriptEnd)) {
      clearBuffer()
      addPart(char.toString)
      parent
    }
    else {
      super.append(char)
    }
  }
}

package com.tinylabproductions.as3stacktracer.parser.scope

import util.matching.Regex.MatchData
import com.tinylabproductions.as3stacktracer.parser.Scope

/**
 * Created by IntelliJ IDEA.
 * User: arturas
 * Date: 2/18/12
 * Time: 12:44 PM
 * To change this template use File | Settings | File Templates.
 */

private[scope] abstract class Variable(body: String, name: String, parent: Scope)
  extends Scope(name, Some(parent))
{
  addPart(body)
  // Add variable to parent.
  parent match {
    case hv: HasVariables => hv.addVariable(qualifiedName)
    case _ => throw new IllegalArgumentException(
      "Variable (%s) parent scope (%s) does not track variables!".format(
        this, parent
      )
    )
  }

  protected[this] val matchers = List.empty

  // Exit this scope on first character.
  override def append(char: Char) = {
    parent.append(char)
    parent
  }
}

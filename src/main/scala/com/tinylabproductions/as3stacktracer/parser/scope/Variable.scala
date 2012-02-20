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
  extends Literal(body, name, parent)
{
  // Add variable to parent.
  parent match {
    case hv: HasVariables => hv.addVariable(this)
    case _ => throw new IllegalArgumentException(
      "Variable (%s) parent scope (%s) does not track variables!".format(
        this, parent
      )
    )
  }

  override def hashCode() = qualifiedName.hashCode()

  override def equals(other: Any) = other match {
    case v: Variable => qualifiedName == v.qualifiedName
    case _ => false
  }
}

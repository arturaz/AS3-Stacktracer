package com.tinylabproductions.as3stacktracer.parser.scope

import com.tinylabproductions.as3stacktracer.parser.Scope

/**
 * Created by IntelliJ IDEA.
 * User: arturas
 * Date: 2/20/12
 * Time: 7:22 PM
 * To change this template use File | Settings | File Templates.
 */

abstract class Literal(body: String, name: String, parent: Scope)
  extends Scope(name, Some(parent))
{
  addPart(body)

  protected[this] val matchers = List.empty

  // Exit this scope on first character.
  override def append(char: Char) = {
    parent.append(char)
    parent
  }
}

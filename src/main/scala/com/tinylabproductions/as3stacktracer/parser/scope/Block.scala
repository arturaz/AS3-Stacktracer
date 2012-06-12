package com.tinylabproductions.as3stacktracer.parser.scope

import com.tinylabproductions.as3stacktracer.parser.Scope

/**
 * Created by IntelliJ IDEA.
 * User: arturas
 * Date: 2/18/12
 * Time: 12:00 PM
 * To change this template use File | Settings | File Templates.
 */

private[scope] abstract class Block(name: String, parent: Scope)
  extends Scope(name, Some(parent))
{
  // Allow passing body as first argument.
  def this(body: String, name: String, parent: Scope) {
    this(name, parent)
    addPart(body)
  }

  protected[this] def OpeningChar: Char
  protected[this] def ClosingChar: Char

  // 1 because initial opening brace is consumed in matching.
  private[this] var blockCounter = 1

  override def append(char: Char): Scope = {
    // Check for closing bracket before trying to match.
    if (char == ClosingChar)
      blockCounter -= 1

    if (blockCounter == 0) {
      clearBuffer()
      onClose()
      addPart(char.toString)
      afterClose()
      parent
    }
    else {
      val newScope = super.append(char)
      // Check for opening bracket only if new scope hasn't been opened.
      if (newScope == this && char == OpeningChar)
        blockCounter += 1
      newScope
    }
  }

  protected[this] def onClose() {}
  protected[this] def afterClose() {}
}
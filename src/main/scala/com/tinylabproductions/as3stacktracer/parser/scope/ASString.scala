package com.tinylabproductions.as3stacktracer.parser.scope

import util.matching.Regex.MatchData
import com.tinylabproductions.as3stacktracer.parser.Scope

private[scope] object ASString extends Matcher
{
  private[ASString] val EscapeRe = """.\\""".r

  protected[this] val matcher = """('|")""".r

  protected[this] def createScope(matchData: MatchData, parent: Scope) = {
    val quote = matchData.group(0)
    Some(new ASString(quote.charAt(0), parent))
  }
}

private[scope] class ASString(quote: Char, parent: Scope)
  extends Scope("string", Some(parent))
{
  addPart(quote)

  protected[this] val matchers = List.empty
  protected[this] val scopeType = "String"

  def qualifiedName = "String"

  private[this] val buffer = new StringBuilder

  override def append(char: Char): Scope = {
    def finish() = {
      clearBuffer()
      addPart(char)
      parent
    }
    def append() = {
      buffer.append(char)
      super.append(char)
    }

    if (char == quote) {
      val tail = buffer.toString().takeRight(2)

      if (tail == """\\""" || ! tail.endsWith("""\""")) finish()
      else append()
    }
    else append()
  }
}
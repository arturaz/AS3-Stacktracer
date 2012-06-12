package com.tinylabproductions.as3stacktracer.parser.scope

import util.matching.Regex.MatchData
import com.tinylabproductions.as3stacktracer.parser.Scope

private[scope] object ASRegex extends Matcher
{
  protected[this] val matcher = """/[^/*\s]""".r

  protected[this] def createScope(matchData: MatchData, parent: Scope) = {
    val beginning = matchData.group(0)
    Some(new StringLike("Regex", beginning.charAt(0), parent)
      .append(beginning.charAt(1)))
  }
}

private[scope] object ASString extends Matcher
{
  protected[this] val matcher = """('|")""".r

  protected[this] def createScope(matchData: MatchData, parent: Scope) = {
    val quote = matchData.group(0)
    Some(new StringLike("String", quote.charAt(0), parent))
  }
}

private[scope] object StringLike {
  val allMatchers = List(ASString)//, ASRegex)
}

private class StringLike(name: String,
                         beginEndChar: Char,
                         parent: Scope)
  extends Scope(name.toLowerCase, Some(parent))
{
  addPart(beginEndChar)

  protected[this] val matchers = List.empty
  protected[this] val scopeType = name
  def qualifiedName = name

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

    if (char == beginEndChar) {
      val tail = buffer.toString().takeRight(2)

      if (tail == """\\""" || ! tail.endsWith("""\""")) finish()
      else append()
    }
    else append()
  }
}

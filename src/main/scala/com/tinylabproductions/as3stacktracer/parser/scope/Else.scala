package com.tinylabproductions.as3stacktracer.parser.scope

import util.matching.Regex.MatchData
import com.tinylabproductions.as3stacktracer.parser.Scope

private[scope] object Else extends Matcher
{
  protected[this] val matcher =
    """(?xs)
      else ( \{ | \s+ (if[^\s(] | i[^f] | [^\si]) )
    """.r

  protected[this] def createScope(matchData: MatchData, parent: Scope) = {
    val extraChars = matchData.group(1)
    val braceIdx = extraChars.indexOf('{')
    val usesBraces = braceIdx >= 0
    val body = "else {" + SemicolonTracker.lineNumAssignWithSemi(parent.lineNum)
    val toAppend = (if (usesBraces) extraChars.substring(braceIdx + 1) else extraChars)
    var scope: Scope = new Else(body, parent, usesBraces)
    toAppend.foreach( char => scope = scope.append(char))
    Some(scope)
  }
}

private[scope] class Else(body: String, parent: Scope, usesBraces: Boolean)
  extends OptionalBracesBlock(
    body, "Else", parent,
    lineNumAfterBlock = true,
    seekForBraces = false,
    usesBraces = usesBraces)
{
  protected[this] val scopeType = "Else"
  def qualifiedName = "Else"
}

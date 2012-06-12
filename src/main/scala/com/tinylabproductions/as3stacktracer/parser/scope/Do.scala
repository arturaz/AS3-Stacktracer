package com.tinylabproductions.as3stacktracer.parser.scope

import util.matching.Regex.MatchData
import com.tinylabproductions.as3stacktracer.parser.Scope

private[scope] object Do extends Matcher
{
  protected[this] val matcher =
    """(?xs)
       do ( \{ | \s )
    """.r

  protected[this] def createScope(matchData: MatchData, parent: Scope) = {
    val hasBraces = matchData.group(1).startsWith("{")
    Some(new Do(
      "do {" + SemicolonTracker.lineNumAssignWithSemi(parent.lineNum),
      parent, !hasBraces, hasBraces))
  }
}

private[scope] class Do(body: String,
                        parent: Scope,
                        seekForBraces: Boolean,
                        usesBraces: Boolean)
  extends OptionalBracesBlock(
    body, "Do", parent,
    lineNumAfterBlock = false,
    seekForBraces = seekForBraces,
    usesBraces = usesBraces)
{
  protected[this] val scopeType = "Do"
  def qualifiedName = "Do"
}
package com.tinylabproductions.as3stacktracer.parser.scope

import com.tinylabproductions.as3stacktracer.parser.Scope
import util.matching.Regex.MatchData

private[scope] object For extends Matcher {
  protected[this] val matcher =
    """(?xs)
       for ( (\s+each)? \s* \( )
    """.r

  protected[this] def createScope(matchData: MatchData, parent: Scope) = {
    Some(new For(matchData.group(0), parent))
  }
}

private[scope] class For(body: String, parent: Scope)
  extends OptionalBracesBlock(body, "For", parent) with BlockWithCondition
{
  protected[this] val scopeType = "For"
  def qualifiedName = "For"
}

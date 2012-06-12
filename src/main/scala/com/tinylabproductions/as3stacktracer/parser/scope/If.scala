package com.tinylabproductions.as3stacktracer.parser.scope

import com.tinylabproductions.as3stacktracer.parser.Scope
import util.matching.Regex.MatchData

private[scope] object If extends Matcher {
  protected[this] val matcher =
    """(?xs)
       (if | else\s+if) \s* \(
    """.r

  protected[this] def createScope(matchData: MatchData, parent: Scope) = {
    Some(new If(matchData.group(0), parent))
  }
}

private class IfCondition(parent: Scope) extends ConditionBeforeBody(parent)
{
  addPart("(" + SemicolonTracker.lineNumAssign(lineNum) + ") && (")

  override protected[this] def onClose() {
    addPart(")")
  }
}

private[scope] class If(body: String, parent: Scope)
  extends OptionalBracesBlock(body, "If", parent, lineNumAfterBlock = false)
          with BlockWithCondition
{
  override def createCondition() = new IfCondition(this)

  protected[this] val scopeType = "If"
  def qualifiedName = "If"
}

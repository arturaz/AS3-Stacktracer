package com.tinylabproductions.as3stacktracer.parser.scope

import com.tinylabproductions.as3stacktracer.parser.Scope
import util.matching.Regex.MatchData

private[scope] object While extends Matcher {
  protected[this] val matcher =
    """(?xs)
       while \s* \(
    """.r

  protected[this] def createScope(matchData: MatchData, parent: Scope) = {
    // Is this a DO WHILE?
    // WHILE after DO is not a real block actually. It only has condition block
    // but it does not have body itself.
    val afterDo = (parent.parts.reverseIterator.find { item =>
      item match {
        case Left(s) => false
        case Right(scope) if scope.isInstanceOf[Comment] => false
        case Right(scope) => true
      }
    }) match {
      case Some(Right(scope: Do)) => true
      case _ => false
    }
    Some(new While(matchData.group(0), parent, afterDo))
  }
}

private[scope] class While(body: String,
                           parent: Scope,
                           private[this] val afterDo: Boolean)
  extends OptionalBracesBlock(body, "While", parent) with BlockWithCondition
{
  override def createCondition() = {
    if (afterDo) new DoWhileCondition(parent)
    else super.createCondition()
  }

  protected[this] val scopeType = "While"
  def qualifiedName = "While"
}

private class DoWhileCondition(parent: Scope) extends Condition(parent)
{
  override protected[this] def afterClose() {
    addPart(SemicolonTracker.lineNumAssignWithSemi(lineNum + 1))
  }
}
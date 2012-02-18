package com.tinylabproductions.as3stacktracer.parser.scope

import util.matching.Regex
import util.matching.Regex.MatchData
import com.tinylabproductions.as3stacktracer.parser.Scope

/**
 * Created by IntelliJ IDEA.
 * User: arturas
 * Date: 2/18/12
 * Time: 11:56 AM
 * To change this template use File | Settings | File Templates.
 */

private[parser] trait Matcher {
  protected[this] val matcher: Regex

  def attempt(buffer: String, parent: Scope): Matcher.Result = {
    matcher.findFirstMatchIn(buffer) match {
      case None => Matcher.Failure
      case Some(matchData) =>
        val matched = matchData.group(0)
        Matcher.Success(
          buffer.substring(0, buffer.length() - matched.length()),
          createScope(matchData, parent)
        )
    }
  }

  protected[this] def createScope(
    matchData: MatchData, parent: Scope
  ): Scope
}

private[parser] object Matcher {
  sealed abstract class Result
  case class Success(before: String, scope: Scope) extends Result
  case object Failure extends Result
}

package com.tinylabproductions.as3stacktracer.parser.scope

import util.matching.Regex.MatchData
import com.tinylabproductions.as3stacktracer.parser.Scope

/**
 * Created by IntelliJ IDEA.
 * User: arturas
 * Date: 3/12/12
 * Time: 11:15 AM
 * To change this template use File | Settings | File Templates.
 */

private[scope] object Catch extends Matcher {
  protected[this] val matcher =
    """(?x)
    catch
    (\s|\n)*
    \(
      (\s|\n)*
      ([a-zA-Z_][\w]*) # error variable identifier
      (\s|\n)*
      (
        :
        (\s|\n)*
        ([a-zA-Z_][\w]*) # error type identifier
        (\s|\n)*
      )? # defaults to Error if not given
    \)
    (\s|\n)*
    \{""".r

  protected[this] def createScope(
    matchData: MatchData, parent: Scope
  ) = {
    val exceptionName = matchData.group(3)
    val errorType = matchData.group(7) match {
      case null => "Error"
      case s: String => s
    }
    Some(new Catch(exceptionName, errorType, parent))
  }
}

@EnhanceStrings
private[scope] class Catch(
  errorName: String, errorType: String, parent: Scope
) extends Block("catch-%s:%s".format(errorName, errorType), parent)
  with CurlyBlock
{
  {
    val catched = "___st_catched_e"
    // Avoid duplicate variable definition warnings.
    val unwrapped = "___st_unwrapped_e_%d".format(File.nextCatchIndex)

    addPart(
      """catch(#catched: Error) {
  var #unwrapped: Error = (#catched is StacktraceError) ? (#catched as StacktraceError).cause : #catched;
  if (! (#unwrapped is TypeError)) throw #catched;
  else
    // This is needed because variable '#errorName' might have a collision with
    // another var somewhere, however this rule does not exist in catch clause.
    try { throw #unwrapped as #errorType; }
    catch (#errorName: #errorType) {"""
    )
  }

  protected[this] val scopeType = "Catch"
  def qualifiedName = name

  protected val matchers = List.empty

  protected[this] def onClose() {
    addPart(ClosingChar)
  }
}

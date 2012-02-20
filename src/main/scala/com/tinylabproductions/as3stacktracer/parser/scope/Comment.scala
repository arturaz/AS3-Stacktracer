package com.tinylabproductions.as3stacktracer.parser.scope

import util.matching.Regex.MatchData
import com.tinylabproductions.as3stacktracer.parser.Scope

/**
 * Created by IntelliJ IDEA.
 * User: arturas
 * Date: 2/20/12
 * Time: 7:20 PM
 * To change this template use File | Settings | File Templates.
 */

private[scope] object Comment extends Matcher {
  protected[this] val matcher = """(//|/\*)""".r

  protected[this] def createScope(matchData: MatchData, parent: Scope) = {
    val body = matchData.group(0)
    Some(new Comment(body, body == "/*", parent))
  }
}

private[scope] class Comment(body: String, multiline: Boolean, parent: Scope)
  extends Scope("comment", Some(parent))
{
  addPart(body)
  
  protected[this] val matchers = List.empty
  protected[this] val scopeType = "Comment"

  def qualifiedName = "comment"
  
  private[this] val buffer = new StringBuilder

  // Ensure whole comment is read ignoring everything in it.
  override def append(char: Char): Scope = {
    def finish() = {
      clearBuffer()
      addPart(char)
      parent
    }

    if (multiline) {
      buffer.append(char)
      if (buffer.toString().endsWith("*/")) finish()
      else super.append(char)
    }
    else {
      if (char == '\n') finish()
      else super.append(char)
    }
  }
}

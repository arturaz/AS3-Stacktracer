package com.tinylabproductions.as3stacktracer.parser.scope

import util.matching.Regex.MatchData
import com.tinylabproductions.as3stacktracer.parser.Scope

/**
 * Created by IntelliJ IDEA.
 * User: arturas
 * Date: 2/18/12
 * Time: 11:58 AM
 * To change this template use File | Settings | File Templates.
 */

private[scope] object Package extends Matcher {
  protected[this] val matcher =
    """(?x)
    package
    (\s|\n)+
    ([a-zA-Z_][\w\.]*)?
    (\s|\n)*
    \{""".r

  private[scope] val ImportStatement =
    "\nimport com.tinylabproductions.stacktracer.StacktraceError;\n"

  protected[this] def createScope(
    matchData: MatchData, parent: Scope
  ) = {
    val name = matchData.group(2) match {
      case null => "root_pkg"
      case s: String => s
    }
    val body = matchData.group(0)
    Some(new Package(body, name, parent))
  }
}

private[scope] class Package(body: String, name: String, parent: Scope)
  extends Block(body, name, parent) with CurlyBlock
{
  addPart(" // %s\n%s".format(fullName, Package.ImportStatement))

  protected[this] val scopeType = "Package"
  def qualifiedName = "pkg:" + name

  protected val matchers = List(Comment, Class, LocalFunction, ASString)

  override protected[this] def onClose() {}
}
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

private[scope] class Catch(
  errorName: String, errorType: String, parent: Scope
) extends Block("catch-"+errorName+":"+errorType, parent)
  with CurlyBlock with SemicolonTracker
{
  {
    val catched = "___st_catched_e"
    // Avoid duplicate variable definition warnings.
    val unwrapped = "___st_unwrapped_e_"+File.nextCatchIndex

    addPart(
      "catch("+catched+": Error) {\n"+
        "var "+unwrapped+": Error = ("+catched+" is StacktraceError) "+
          "? ("+catched+" as StacktraceError).cause : "+catched+";\n"+
        "if (! ("+unwrapped+" is "+errorType+")) throw "+catched+";\n"+
        "else\n"+
        "// This is needed because variable '"+errorName+"' might have a \n"+
        "// collision with another var somewhere, however this rule does \n"+
        "// not exist in catch clause.\n"+
        "try { throw "+unwrapped+" as "+errorType+"; }\n"+
        "catch ("+errorName+": "+errorType+") {"+
          AbstractFunction.LineNumberVarName+"="+lineNum+";"
    )
  }

  protected[this] val scopeType = "Catch"
  def qualifiedName = name

  protected val matchers =
    List(Comment, LocalFunction)
      .union(OptionalBracesBlock.allMatchers)
      .union(StringLike.allMatchers)

  override def append(char: Char) =
    semicolonAppend(char) { () => super.append(char) }

  override protected[this] def onClose() {
    addPart(ClosingChar)
  }
}

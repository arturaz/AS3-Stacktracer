package com.tinylabproductions.as3stacktracer.parser.scope

import com.tinylabproductions.as3stacktracer.parser.Scope

private[scope] object SemicolonTracker {
  def lineNumAssignWithSemi(lineNum: Int) = {
    lineNumAssign(lineNum) + "; "
  }

  def lineNumAssign(lineNum: Int) = {
    AbstractFunction.LineNumberVarName + "=" + lineNum
  }
}

private[scope] trait SemicolonTracker { self: Scope =>
  def semicolonAppend(char: Char)(parent: () => Scope): Scope = {
    if (char == ';') {
      clearBuffer()
      // lineNum + 1 because this is a line number for next statement.
      addPart(";" + SemicolonTracker.lineNumAssign(lineNum + 1))
    }
    parent()
  }
}

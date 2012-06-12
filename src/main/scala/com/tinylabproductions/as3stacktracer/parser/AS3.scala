package com.tinylabproductions.as3stacktracer.parser

import scope.File

/**
 * Created by IntelliJ IDEA.
 * User: arturas
 * Date: 2/17/12
 * Time: 10:59 AM
 * To change this template use File | Settings | File Templates.
 */

object AS3 {
  def convert(filename: String, input: Traversable[Char]): String = {
    val parser = new AS3(filename)
    parser.parse(input)
    parser.output
  }
}

class AS3(filename: String) extends LineCounter {
  private[this] var scope: Scope = File(filename, this)

  private[this] var _lineNumber: Int = 1
  override def currentLineNum = _lineNumber

  def parse(input: Traversable[Char]) = {
    input.foreach { char =>
      if (char == '\n') _lineNumber += 1
      scope = scope.append(char)
    }

    // Finalize parsing
    var previous: Scope = null
    while (previous != scope) {
      previous = scope
      scope = scope.finished()
    }
  }

  def output: String = {
    def traverse(scope: Scope, builder: StringBuilder) {
      scope.parts.foreach { part =>
        part match {
          case Left(str) => builder.append(str)
          case Right(childScope) => traverse(childScope, builder)
        }
      }
    }
    val builder = new StringBuilder
    traverse(scope, builder)
    builder.toString()
  }
}

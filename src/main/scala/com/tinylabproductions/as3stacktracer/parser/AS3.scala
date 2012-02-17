package com.tinylabproductions.as3stacktracer.parser

/**
 * Created by IntelliJ IDEA.
 * User: arturas
 * Date: 2/17/12
 * Time: 10:59 AM
 * To change this template use File | Settings | File Templates.
 */



class AS3(filename: String) {
  private[this] var scope: Scope = new Scope.File(filename)

  def parse(input: String) = {
    input.foreach { char =>
      scope = scope.append(char)
    }
  }

  override def toString = {
    def traverse(scope: Scope, builder: StringBuilder): StringBuilder = {
      scope.parts.foreach { part =>
        part match {
          case Left(str) => builder.append(str)
          case Right(childScope) => traverse(childScope, builder)
        }
      }
      builder
    }
    traverse(scope, new StringBuilder).toString
  }
}

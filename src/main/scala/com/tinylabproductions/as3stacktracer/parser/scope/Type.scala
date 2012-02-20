package com.tinylabproductions.as3stacktracer.parser.scope

/**
 * Created by IntelliJ IDEA.
 * User: arturas
 * Date: 2/20/12
 * Time: 11:46 AM
 * To change this template use File | Settings | File Templates.
 */

private[scope] sealed abstract class Type {
  val returnValue: String
}

private[scope] object Type {
  def create(signature: String) = signature match {
    case "int" | "uint" | "Number" => Numeric
    case "Boolean" => Boolean
    case "void" => Void
    case _ => Object
  }

  case object Numeric extends Type {
    val returnValue = "return 0;"
  }
  case object Boolean extends Type {
    val returnValue = "return false;"
  }
  case object Void extends Type {
    val returnValue = ""
  }
  case object Object extends Type {
    val returnValue = "return null;"
  }
}
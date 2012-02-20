package com.tinylabproductions.as3stacktracer.parser.scope

/**
 * Created by IntelliJ IDEA.
 * User: arturas
 * Date: 2/20/12
 * Time: 12:24 PM
 * To change this template use File | Settings | File Templates.
 */

private[scope] abstract sealed class Namespace {
  val prefix: String
}

private[this] trait NoPrefix {
  val prefix = ""
}

private[scope] object Namespace {
  case object Public extends Namespace with NoPrefix
  case object Protected extends Namespace with NoPrefix
  case object Private extends Namespace with NoPrefix
  case class Custom(name: String) extends Namespace {
    val prefix = "%s::".format(name)
  }

  def create(signature: String) = signature match {
    case null | "public" => Public
    case "protected" => Protected
    case "private" => Private
    case s: String => Custom(s)
  }
}
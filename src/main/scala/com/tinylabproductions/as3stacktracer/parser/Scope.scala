package com.tinylabproductions.as3stacktracer.parser

import scope.Matcher
import collection.mutable.ListBuffer

/**
 * Created by IntelliJ IDEA.
 * User: arturas
 * Date: 2/17/12
 * Time: 2:09 PM
 * To change this template use File | Settings | File Templates.
 */

private[parser] abstract class Scope(
  val name: String,
  val parent: Option[Scope]
) {
  protected[this] val matchers: List[Matcher]
  protected[this] val scopeType: String

  private[this] val _parts = ListBuffer.empty[Either[String, Scope]]
  def parts = _parts
  
  protected[this] def addPart(c: Char) { _parts += Left(c.toString) }
  protected[this] def addPart(s: String) { _parts += Left(s) }
  protected[this] def addPart(s: Scope) { _parts += Right(s) }

  private[this] val buffer: StringBuilder = new StringBuilder

  protected def lineCounter: LineCounter = parent match {
    case Some(p) => p.lineCounter
    case None => throw new RuntimeException(
      "Cannot find line counter, consider overriding this method in root " +
      "class!"
    )
  }

  private[parser] def lineNum = lineCounter.currentLineNum

  override def toString = "<Scope.%s name: %s, parent: %s>".format(
    scopeType, name, parent.toString
  )

  private[parser] def append(char: Char): Scope = {
    buffer.append(char)
    tryToMatch() match {
      case Some(scope) => scope
      case None => this
    }
  }

  private[parser] def clearBuffer() {
    val str = buffer.toString()
    if (! str.isEmpty) addPart(str)
    buffer.clear()
  }

  private[parser] def finished(): Scope = {
    clearBuffer()
    parent match {
      case Some(parent) => parent
      case None => this
    }
  }

  private[this] def tryToMatch(): Option[Scope] = {
    val bufferString = buffer.toString()
    matchers.foreach { matcher =>
      matcher.attempt(bufferString, this) match {
        case Matcher.Failure => ()
        case Matcher.Success(before, scope) =>
          buffer.clear()
          addPart(before)
          addPart(scope)
          return Some(scope)
      }
    }

    None
  }

  def qualifiedName: String

  def fullName = fullNameImpl.mkString("/")
  private def fullNameImpl: List[String] = parent match {
    case None => List(qualifiedName)
    case Some(p) => p.fullNameImpl :+ qualifiedName
  }
}
package com.tinylabproductions.as3stacktracer.parser.scope

/**
 * Created by IntelliJ IDEA.
 * User: arturas
 * Date: 2/18/12
 * Time: 12:34 PM
 * To change this template use File | Settings | File Templates.
 */

private[scope] trait CurlyBlock { this: Block =>
  protected[this] val OpeningChar = '{'
  protected[this] val ClosingChar = '}'
}

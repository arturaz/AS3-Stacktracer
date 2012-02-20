package com.tinylabproductions.as3stacktracer.parser.scope

import com.tinylabproductions.as3stacktracer.parser.Scope

/**
 * Created by IntelliJ IDEA.
 * User: arturas
 * Date: 2/20/12
 * Time: 12:33 PM
 * To change this template use File | Settings | File Templates.
 */

private[scope] object ClassVariable {
  def createNamespace(signature: String) = Namespace.create(signature)

  def createNamespace(ns1: String, ns2: String) = (ns1, ns2) match {
    case (null, null) => Namespace.Public
    case (s: String, null) => Namespace.create(s)
    case (null, s: String) => Namespace.create(s)
    case (s1: String, s2: String) => throw new RuntimeException(
      "Two namespaces? Huh?! First: %s. Second: %s.".format(s1, s2)
    )
  }
}

private[scope] abstract class ClassVariable(
  namespace: Namespace, body: String, name: String, parent: Scope
) extends Variable(body, name, parent)
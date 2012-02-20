package com.tinylabproductions.as3stacktracer.parser.scope

import com.tinylabproductions.as3stacktracer.parser.Scope

/**
 * Created by IntelliJ IDEA.
 * User: arturas
 * Date: 2/18/12
 * Time: 11:58 AM
 * To change this template use File | Settings | File Templates.
 */

private[parser] class File(name: String) extends Scope(name, None) {
  protected[this] val scopeType = "File"
  def qualifiedName = name

  private[this] var nonPackageImportAdded = false
  // Add import statement for file inner classes.
  private[scope] def addNonPackageImport() {
    if (! nonPackageImportAdded) {
      addPart(Package.ImportStatement)
      nonPackageImportAdded = true
    }
  }

  protected val matchers = List(Package, Class, LocalFunction)
}

package com.tinylabproductions.as3stacktracer.parser.scope

import com.tinylabproductions.as3stacktracer.parser.Scope

/**
 * Created by IntelliJ IDEA.
 * User: arturas
 * Date: 2/18/12
 * Time: 11:58 AM
 * To change this template use File | Settings | File Templates.
 */

private[parser] abstract class File(name: String) extends Scope(name, None) {
  File.resetCatchIndex()

  def qualifiedName = name
}

private[parser] object File {
  def apply(filename: String): File = {
    if (filename.endsWith(".as")) return new AsFile(filename)
    else if (filename.endsWith(".mxml")) return new MxmlFile(filename)
    else throw new IllegalArgumentException(
      "Unsupported file: %s. Supported extensions: as, mxml.".format(filename)
    )
  }

  private[this] var catchIndex = 0
  private[File] def resetCatchIndex() { catchIndex = 0 }
  // Returns index number of catch clause in this file.
  def nextCatchIndex: Int = {
    catchIndex += 1
    catchIndex
  }

  class AsFile(name: String) extends File(name) {
    protected[this] val scopeType = "AsFile"
    protected val matchers = List(Comment, Package, Class, LocalFunction)

    private[this] var nonPackageImportAdded = false
    // Add import statement for file inner classes.
    private[scope] def addNonPackageImport() {
      if (! nonPackageImportAdded) {
        addPart(Package.ImportStatement)
        nonPackageImportAdded = true
      }
    }
  }

  class MxmlFile(name: String) extends File(name) {
    protected[this] val scopeType = "MxmlFile"
    protected val matchers = List(Script)
  }
}
package com.tinylabproductions.as3stacktracer.parser.scope

import com.tinylabproductions.as3stacktracer.parser.{LineCounter, Scope}


/**
 * Created by IntelliJ IDEA.
 * User: arturas
 * Date: 2/18/12
 * Time: 11:58 AM
 * To change this template use File | Settings | File Templates.
 */

private[parser] abstract class File(name: String, _lineCounter: LineCounter)
extends Scope(name, None) {
  File.resetCatchIndex()

  def qualifiedName = name

  override protected def lineCounter = _lineCounter
}

private[parser] object File {
  def apply(filename: String, lineCounter: LineCounter): File = {
    if (filename.endsWith(".as"))
      return new AsFile(filename, lineCounter)
    else if (filename.endsWith(".mxml"))
      return new MxmlFile(filename, lineCounter)
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

  class AsFile(name: String, lineCounter: LineCounter)
  extends File(name, lineCounter) {
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

  class MxmlFile(name: String, lineCounter: LineCounter)
  extends File(name, lineCounter) {
    protected[this] val scopeType = "MxmlFile"
    protected val matchers = List(Script)
  }
}
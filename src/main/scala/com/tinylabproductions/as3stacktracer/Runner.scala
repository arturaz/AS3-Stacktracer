package com.tinylabproductions.as3stacktracer

import parser.AS3
import scalax.io._
import scalax.file.Path
import scalax.file.Path._
import org.streum.configrity.Configuration

/**
 * Created by IntelliJ IDEA.
 * User: arturas
 * Date: 2/17/12
 * Time: 10:52 AM
 * To change this template use File | Settings | File Templates.
 */

object Runner {
  private[this] val WinDS = '\\'
  private[this] val UnixDS = '/'
  private[this] val / = System.getProperty("file.separator")
  private[this] val ConfigName = "as3stacktracer.conf"

  private[this] val configPath = Path(ConfigName)
  private[this] val config = if (configPath.exists) {
    Configuration.load(configPath.path)
  }
  else {
    println(
      "WARNING: Configuration file ./%s cannot be found!".format(ConfigName)
    )
    new Configuration(Map.empty)
  }
  // Always force processing.
  private[this] val force = config[Boolean]("force", false)
  private[this] val verbose = config[Boolean]("verbose", false)

  def main(args: Array[String]) {
    if (args.size == 2) {
      val source = Path.fromString(args(0))
      val target = Path.fromString(args(1))

      if (source.isDirectory && (target.isDirectory || ! target.exists))
        processDirs(source, target)
      else
        processFiles(args)
    }
    else if (args.size == 0) help()
    else processFiles(args)
    
    println("\nDone.")
  }

  private[this] def help() {
    println("""AS3Stacktracer

Usage:
- java -jar as3st.jar source_dir target_dir

  Process whole source tree and write it to target dir. Unrecognized files
  are just copied.

- java -jar as3st.jar files.as to.as transform.mxml

  Transform given files IN-PLACE.""")
  }

  private[this] def processDirs(srcDir: Path, dstDir: Path) {
    println("About to process %s -> %s".format(srcDir, dstDir))
    
    val srcDirAbs = srcDir.toAbsolute.path + /
    val skippedFiles = config[List[String]]("skipped", List.empty).map(_.r)

    def shouldSkip(path: String) = {
      // Change path separator, because in config we use unix separators.
      val processed = path.replace(WinDS, UnixDS)

      skippedFiles.exists { re =>
        //println("%s ~ %s".format(path, re))
        re.findFirstIn(processed).isDefined
      }
    }
    
    def copy(src: Path, dst: Path) {
      dst.openOutput(out => src.copyDataTo(out))
    }
    
    def paths(src: Path) = {
      val srcAbs = src.toAbsolute.path
      val srcRel = srcAbs.replace(srcDirAbs, "")
      val dst = dstDir resolve srcRel
      
      (srcAbs, srcRel, dst)
    }

    def process(src: Path) {
      val (srcAbs, srcRel, dst) = paths(src)
      
      if (srcAbs.endsWith(".as") || srcAbs.endsWith(".mxml")) {
        if (shouldSkip(srcRel)) {
          run('s', "Skipping (copying): "+srcRel)
            { () => copy(src, dst) }
        }
        else {
          run('p', "Processing: "+srcRel) { () =>
            dst.parent.map { _.createDirectory(failIfExists = false) }
            convert(src, srcRel, dst)
          }
        }
      }
      else {
        run('c', "Copying: %s".format(srcRel)) { () => copy(src, dst) }
      }
    }

    cleanup(dstDir, srcDir)

    srcDir.descendants().filter { src =>
      // Filter files that don't need updating to try to ensure that both threads
      // get equal work.
      val (srcAbs, srcRel, dst) = paths(src)

      if (src.isDirectory) false
      else if (needsUpdate(src, dst)) true
      else {
        report('.', "Skipping: %s (same modification time)".format(srcRel))
        false
      }
    }.par.foreach { file => process(file) }
  }

  private[this] def cleanup(dstDir: Path, srcDir: Path) {
    if (dstDir.exists) {
      val dstAbsDir = dstDir.toAbsolute.path + /

      dstDir.descendants().par.foreach { file =>
        val dstAbsFile = file.toAbsolute.path
        val dstRelFile = dstAbsFile.replace(dstAbsDir, "")

        val srcFile = srcDir resolve dstRelFile
        if (! srcFile.exists) {
          println("Removing %s which does not exist in %s".format(
            file.path, srcDir.path
          ))
          file.deleteRecursively(force = true)
        }
      }
    }
  }
  
  private[this] def processFiles(filenames: Array[String]) {
    println("About to process %d files.".format(filenames.length))

    filenames.foreach { filename =>
      val file = Path.fromString(filename)
      if (file.exists) {
        run('p', "Processing: %s".format(filename))
          { () => convert(file, filename, file) }
      }
      else {
        println("WARNING: %s does not exist!".format(filename))
      }
    }
  }
  
  private[this] def convert(src: Path, relSrcName: String, dst: Path) {
    val srcLastMod = src.lastModified // In case src == dst
    val unixifiedName = relSrcName.replace(WinDS, UnixDS)
    val transformed = AS3.convert(unixifiedName, src.chars(Codec.UTF8))
    dst.deleteIfExists()
    dst.write(transformed)(Codec.UTF8)
    dst.lastModified = srcLastMod
  }
  
  private[this] def run[T](short: => Char, long: => String)(func: () => T): T = {
    if (verbose)
      println(long)
    val res = func()
    if (! verbose)
      print(short)

    res
  }

  private[this] def report(short: => Char, long: => String) {
    if (verbose)
      println(long)
    else
      print(short)
  }

  private[this] def needsUpdate(src: Path, dst: Path): Boolean = {
    force || ! dst.exists || src.isFresher(dst)
  }
}

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
    
    println("Done.")
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
    val skippedFiles = config[List[String]]("skipped", List.empty)

    def shouldSkip(path: String) = {
      skippedFiles.exists { reStr =>
        reStr.r.findFirstIn(path).isDefined
      }
    }
    
    def copy(src: Path, dst: Path) {
      dst.openOutput(out => src.copyDataTo(out))
    }

    def process(src: Path) {
      val srcAbs = src.toAbsolute.path
      val srcRel = srcAbs.replace(srcDirAbs, "")
      val dst = dstDir resolve srcRel
      
      if (needsUpdate(src, dst)) {
        if (srcAbs.endsWith(".as") || srcAbs.endsWith(".mxml")) {
          if (shouldSkip(srcRel)) {
            benchmark("Skipping (copying): %s".format(srcRel))
              { () => copy(src, dst) }
          }
          benchmark("Processing: %s".format(srcRel)) { () =>
            dst.parent.map { _.createDirectory(failIfExists = false) }
            convert(src, srcRel, dst)
          }
        }
        else {
          benchmark("Copying: %s".format(srcRel)) { () => copy(src, dst) }
        }
      }
      else {
        println("Skipping: %s (same modification time)".format(srcRel))
      }
    }

    srcDir.descendants().par.foreach { file =>
      if (! file.isDirectory) process(file)
    }
  }
  
  private[this] def processFiles(filenames: Array[String]) {
    println("About to process %d files.".format(filenames.length))

    filenames.foreach { filename =>
      val file = Path.fromString(filename)
      if (file.exists) {
        benchmark("Processing: %s".format(filename)) { () =>
          convert(file, filename, file)
        }
      }
      else {
        println("WARNING: %s does not exist!".format(filename))
      }
    }
  }
  
  private[this] def convert(src: Path, relSrcName: String, dst: Path) {
    val srcLastMod = src.lastModified // In case src == dst
    val transformed = AS3.convert(relSrcName, src.chars(Codec.UTF8))
    dst.deleteIfExists()
    dst.write(transformed)(Codec.UTF8)
    dst.lastModified = srcLastMod
  }
  
  private[this] def benchmark[T](msg: String)(func: () => T): T = {
    val start = System.currentTimeMillis()
    val res = func()
    val end = System.currentTimeMillis()
    println("%s (%3.2fs)".format(msg, (end - start).toFloat / 1000))
    res
  }

  private[this] def needsUpdate(src: Path, dst: Path): Boolean = {
    force || ! dst.exists || src.isFresher(dst)
  }
}

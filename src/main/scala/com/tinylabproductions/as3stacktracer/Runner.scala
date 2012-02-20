package com.tinylabproductions.as3stacktracer

import parser.AS3
import java.io.File
import scalax.io._

/**
 * Created by IntelliJ IDEA.
 * User: arturas
 * Date: 2/17/12
 * Time: 10:52 AM
 * To change this template use File | Settings | File Templates.
 */

object Runner {
  def main(args: Array[String]) {
    args.foreach { filename =>
      val file = new File(filename)
      if (file.exists()) {
        println("Processing: %s".format(filename))
        val input: Input = Resource.fromFile(file)
  
        val transformed = AS3.convert(filename, input.chars(Codec.UTF8))
  
        val output: Output = Resource.fromFile(file)
        output.write(transformed)(Codec.UTF8)
      }
      else {
        println("WARNING: %s does not exist!".format(filename))
      }
    }

    println("Done.")
  }
}

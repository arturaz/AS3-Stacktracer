package com.tinylabproductions.as3stacktracer

import parser.AS3
import util.parsing.input.CharSequenceReader

/**
 * Created by IntelliJ IDEA.
 * User: arturas
 * Date: 2/17/12
 * Time: 10:52 AM
 * To change this template use File | Settings | File Templates.
 */

object Runner {
  def main(args: Array[String]) {
    val input = """package {
   import flash.display.*;
   import flash.errors.IllegalOperationError;

   public class Trycatch2 extends Sprite {

      /**
       *   Application entry point
       */
      public function Trycatch2() {
         test(1,2,3);
      }

      private function test (a, b, c):void {
         var a: int = 3;

         for (var b: int = 10; b < 20; b++) {
            var c: int = -10 - b;
            test2();
         }
      }

      private function test2():void {
         throw new IllegalOperationError("I Suck!");
      }
   }
}

import flash.errors.IllegalOperationError;

class X {
   function Y() {
      throw new IllegalOperationError("I Suck!");
   }
}
"""
    val parser = new AS3("test.as")
    parser.parse(input)
    println(parser.toString())
  }
}

package com.tinylabproductions.stacktracer {
   public class StacktraceEntry {
      private var _currentFunction: String;
      private var _variables: Object;

      public function StacktraceEntry(
         currentFunction: String, variables: Object
      ) {
         _currentFunction = currentFunction;
         _variables = variables;
      }

      public function get currentFunction(): String {
         return _currentFunction;
      }

      public function get variables(): Object {
         return _variables;
      }
   }
}

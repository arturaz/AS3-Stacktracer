package com.tinylabproductions.stacktracer {
   import flash.utils.getQualifiedClassName;

   public class StacktraceError extends Error {
      private var _cause: Error;
      private var _stacktrace: Vector.<StacktraceEntry> =
         new Vector.<StacktraceEntry>();
      
      public function StacktraceError(cause: Error, entry: StacktraceEntry) {
         _cause = cause;
         _stacktrace.push(entry);
         super(cause.message, cause.errorID);
      }

      public function get cause(): Error {
         return _cause;
      }

      public function get stacktrace(): Vector.<StacktraceEntry> {
         return _stacktrace;
      }

      public function toString(): String {
         return getQualifiedClassName(_cause) +
            " (error id " + _cause.errorID + "): " + _cause.message + "\n\n" +
            "Stacktrace (without variables):\n" + generateStacktrace(false) +
            "\nStacktrace (with variables):\n" + generateStacktrace(true) +
            "\n";
      }
      
      private function generateStacktrace(showVars: Boolean): String {
         var msg: String = "";
         
         var length: uint = _stacktrace.length;
         // index must be int, because otherwise it would never turn to -1.
         for (var index: int = length - 1; index >= 0; index--) {
            var entry: StacktraceEntry = _stacktrace[length - index - 1];

            msg += "- [" + index + "] @ " + entry.currentFunction + "\n";
            if (showVars) {
               msg += "  Variables:";

               // entry.variables.length does not return 0 even if it has no
               // variables...
               var hadVariables: Boolean = false;
               if (entry.variables != null) {
                  for (var name: String in entry.variables) {
                     msg += "\n    " + name + ": " +
                        String(entry.variables[name]);
                     hadVariables = true;
                  }
               }

               if (! hadVariables) msg += " no variables registered";
               msg += "\n"
            }
         }
         
         return msg;
      }

      public static function trace(
         e: Error, currentFunction: String, variables: Object
      ): Error {
         var entry: StacktraceEntry =
            new StacktraceEntry(currentFunction, variables);

         if (e is StacktraceError) {
            var ste: StacktraceError = e as StacktraceError;
            ste.stacktrace.push(entry);
            return ste;
         }
         else {
            return new StacktraceError(e, entry);
         }
      }
   }
}

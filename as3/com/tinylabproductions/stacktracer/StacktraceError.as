package com.tinylabproductions.stacktracer {
   import flash.utils.getQualifiedClassName;

   public class StacktraceError extends Error {
      private var cause: Error;
      private var stacktrace: Vector.<StacktraceEntry> =
         new Vector.<StacktraceEntry>();
      
      public function StacktraceError(cause: Error, entry: StacktraceEntry) {
         this.cause = cause;
         
         message = cause.message;
         name = cause.name;
         
         stacktrace.push(entry);
      }

      override public function get errorID(): int {
         return cause.errorID;
      }

      override public function getStackTrace(): String {
         return "Stacktrace (without variables):\n" +
            generateStacktrace(false) +
            "\nStacktrace (with variables):\n" + generateStacktrace(true) +
            "\n";
      }

      public function toString(): String {
         return "<StacktraceError " + name + " (error id " + errorID + "): " +
            message + " (" + stacktrace.length + " entries)>";
      }
      
      public function generateStacktrace(showVars: Boolean): String {
         var msg: String = "";
         
         var length: uint = stacktrace.length;
         // index must be int, not uint, because otherwise it would never turn
         // to -1.
         for (var index: int = length - 1; index >= 0; index--) {
            var entry: StacktraceEntry = stacktrace[length - index - 1];

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

      private function push(entry: StacktraceEntry): void {
         stacktrace.push(entry);
      }

      public static function trace(
         e: Error, currentFunction: String, variables: Object
      ): Error {
         var entry: StacktraceEntry =
            new StacktraceEntry(currentFunction, variables);

         if (e is StacktraceError) {
            var ste: StacktraceError = e as StacktraceError;
            ste.push(entry);
            return ste;
         }
         else {
            return new StacktraceError(e, entry);
         }
      }

      public static function mergeVars(vars1: Object, vars2: Object): Object {
         if (vars1 == null) return vars2;
         if (vars2 == null) return vars1;
         
         var result: Object = new Object();
         var name: String;
         for (name in vars1) { result[name] = vars1[name]; }
         for (name in vars2) { result[name] = vars2[name]; }
         return result;
      }
   }
}

class StacktraceEntry {
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

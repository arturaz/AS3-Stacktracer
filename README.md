# Actionscript 3 Stacktracer

This tool preprocesses AS3 code and wraps every method in try/catch statements.
This provides at least rudimentary stacktrace support on non-debug flash 
players.

# Compiling

Get SBT from <http://www.scala-sbt.org/>, enter this directory and launch `sbt assembly`.
Having JDK is required. Result will be in `target/as3_st.jar`.

# Running

To precompile one directory and output source to another:

    java -jar as3_st.jar src_dir dst_dir

Unsupported files will be skipped.

To transform files in place:

    java -jar as3_st.jar file1.as file2.as

This tool supports .as and .mxml files.

When running, it outputs one character for each file:

* `p` - file has been preprocessed.
* `.` - no update needed.
* `c` - file type not supported, file is copied instead.
* `s` - file preprocessing was skipped, file is copied instead.

To compile your precompiled source you will need to include files from `as3`
directory into your project. You may modify them to suit your project needs.

# Configuration

Configuration is placed in `as3stacktracer.conf` file in working directory.

Example configuration:

    force = false
    verbose = false
    skipped = [
      "^com/tinylabproductions/stacktracer/.+?\.as$",
      "^com/adobe/.+?\.as$",
      "^utils/bkde/as3/.+?\.as$",
      "^utils/GlobalErrorHandler\.as$",
      "^assets/.+?\.as$"
    ]

* `force` - precompile files even if we can't detect any change in them.
* `verbose` - show full messages instead of symbols.
* `skipped` - array of regexps which files should not be preprocessed.
Windows dir separators are converted to unix, so always use / here.
Java regexp modifier flags (i.e. "(?i)foo") can be used here.

# Features missing / not supported

* All statements must be terminated with semicolon

    // not supported: must have ; at the end of statement
    a++

* Line numbers are supported but only inside functions for now and they
are not always exact.

* Cannot have string literals as default values for function arguments

    // Unsupported. Extract to a constant
    function foo(name: String = "Unknown"): void {}

    // OK
    private static const DEFAULT_NAME = "Unknown";
    function foo(name: String = DEFAULT_NAME): void {}

* Cannot have comments

    - between package/class/function declaration and body.

        package foo 
        // This is not supported
        {
          ...
        }

    - between if and (...), else and if, else and it's body

        if /* not supported */ (...) {}
        else /* not supported */ if (...) {}
        else // not supported
        {...}

        // Legal comment
        if (...) {}
        // Legal comment
        else if (...) {}
        // Legal comment
        else
        {...}

    - between for and each, loop keyword and (...)

        for /* not supported */ each /* not supported */ (...) {}
        while /* not supported */ (...) {}

* Variables declared without var are not tracked.

        var a: String, b: String; // b will not be tracked.

* Regular expressions that have quotes (single or double) in their body will
cause errors after preprocessing.

    // Not OK. Use:
    // new RegExp("abc['\"]", "g")
    var r: RegExp = /abc['"]/g;

    // OK. No quotes in the body.
    var r: RegExp = /abc/g

* Braces are still optional after control flow statements if their body contains
only one statement, but there is one caveat: you must use braces if control flow
body contains if else block

    // Not OK. Preprocessor will enclose the inner if statement inside braces
    // but will leave out the else part.
    for (i: int = 0; i < 10; i++)
        if (i < 5) foo();
        else bar();

    // This is OK.
    for (i: int = 0; i < 10; i++) {
        if (i < 5) foo();
        else bar();
    }

* Multiple nested try/catch blocks are not supported.
        
        // This is not supported.
        function func1() {
          try { foo(); }
          catch (err1: Error) {
            try { foo2(); }
            catch (err2: Error) {
              errorHandling();
            }
          }
        }

        // Recommended workaround is:
        function func1() {
          try { foo(); }
          catch (err1: Error) { func2(); }
        }
        function func2() {
          try { foo2(); }
          catch (err2: Error) {
            errorHandling();
          }
        }

# Author

Artūras Šlajus (<arturas.slajus@gmail.com>)

# License

This work is licensed under a Creative Commons Attribution-ShareAlike 3.0 
Unported License.

<http://creativecommons.org/licenses/by-sa/3.0/>

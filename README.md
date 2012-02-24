# Actionscript 3 Stacktracer

This tool preprocesses AS3 code and wraps every method in try/catch statements.
This provides at least rudimentary stacktrace support on non-debug flash 
players.

# Compiling

Get SBT <http://www.scala-sbt.org/>, enter this directory and launch `sbt assembly`.
Having JDK is required. Result will be in `target/as3_st.jar`.

# Running

To precompile one directory and output source to another:

    java -jar as3_st.jar src_dir dst_dir

Unsupported files will be skipped.

To transform files in place:

    java -jar as3_st.jar file1.as file2.as

This tool supports .as and .mxml files.

When running, it outputs one character for each file:

* _p_ - file has been preprocessed.
* _._ - no update needed.
* _c_ - file type not supported, file is copied instead.
* _s_ - file preprocessing was skipped, file is copied instead.

To compile your precompiled source you will need to include files from _as3_
directory into your project. You may modify them to suit your project needs.

# Configuration

Configuration is placed in _as3stacktracer.conf_ file in working directory.

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

* _force_ - precompile files even if we can't detect any change in them.
* _verbose_ - show full messages instead of symbols.
* _skipped_ - array of regexps which files should not be preprocessed.
Windows dir separators are converted to unix, so always use / here.
Java regexp modifier flags (i.e. "(?i)foo") can be used here.

# Features missing/not supported

* No line numbers in stacktraces yet. This would be most welcome patch.
* Cannot have comments between package/class/function declaration and body.

    package foo 
    // This is not supported
    {
      ...
    }

* Variables declared without var are not tracked.

    var a: String, b: String; // b will not be tracked.

# Author

Artūras Šlajus <arturas.slajus@gmail.com>

# License

This work is licensed under a Creative Commons Attribution-ShareAlike 3.0 
Unported License.

<http://creativecommons.org/licenses/by-sa/3.0/>

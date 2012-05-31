package com.tinylabproductions.as3stacktracer.parser.scope

import util.matching.Regex.MatchData
import com.tinylabproductions.as3stacktracer.parser.Scope

/**
 * Created by IntelliJ IDEA.
 * User: arturas
 * Date: 2/18/12
 * Time: 12:04 PM
 * To change this template use File | Settings | File Templates.
 */

private[scope] object Class extends Matcher {
  protected[scope] val VarsMethodName = "__st_vars__"

  protected[this] val matcher =
    """(?x)
    class
    (\s|\n)+ # whitespace
    ([a-zA-Z_]\w*) # class name
    (\s|\n)* # whitespace
    .*? # extends/implements
    (\s|\n)* # whitespace
    \{""".r

  protected[this] def createScope(
    matchData: MatchData, parent: Scope
  ) = {
    val body = matchData.group(0)
    val name = matchData.group(2)
    Some(new Class(body, name, parent))
  }
}

private[scope] class Class(body: String, name: String, parent: Scope)
  extends Block(name, parent) with CurlyBlock with HasVariables
{
  parent match {
    case f: File.AsFile => f.addNonPackageImport()
    case _ => ()
  }
  addPart("%s // %s\n".format(body, fullName))

  protected[this] val scopeType = "Class"
  def qualifiedName = name

  protected val matchers = List(
    Comment, StaticVariable, InstanceVariable,
    StaticFunction, InstanceFunction, LocalFunction, ASString
  )

  override private[scope] def addVariable(variable: Variable) = variable match {
    case v: ClassVariable => super.addVariable(v)
    case _ => throw new IllegalArgumentException(
      "I only support static and instance variables!"
    )
  }

  override protected def variablesString = throw new UnsupportedOperationException(
    "Try variableStrings() instead!"
  )

  private[this] def variableStrings: (String, String) = {
    val (static, instance) = variables.partition { variable =>
      variable match {
        case sv: StaticVariable => true
        case iv: InstanceVariable => false
        case v => throw new RuntimeException(
          "'%s' is not supposed to be here!".format(v)
        )
      }
    }

    (HasVariables.toString(static), HasVariables.toString(static ++ instance))
  }

  protected[this] def onClose() {
    val (static, instance) = variableStrings

    // Both instance/static method have same name: mxmlc can distinguish which one to
    // use in a function.
    addPart("""
private static function %s(localVars: Object): Object {
  return StacktraceError.mergeVars(localVars, %s);
}

private function %s(localVars: Object): Object {
  return StacktraceError.mergeVars(localVars, %s);
}
    """.format(
      Class.VarsMethodName, static,
      Class.VarsMethodName, instance
    ))
  }
}
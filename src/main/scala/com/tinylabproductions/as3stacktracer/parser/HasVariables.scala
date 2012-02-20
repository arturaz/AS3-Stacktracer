package com.tinylabproductions.as3stacktracer.parser.scope

import com.tinylabproductions.as3stacktracer.parser.Scope

/**
 * Created by IntelliJ IDEA.
 * User: arturas
 * Date: 2/18/12
 * Time: 12:52 PM
 * To change this template use File | Settings | File Templates.
 */

private[scope] object HasVariables {
  def toString(vars: Traversable[Variable]): String = {
    if (vars.isEmpty)
      "null"
    else
      "{%s}".format(
        vars.map { variable =>
          val name = variable.qualifiedName
          """"%s": %s""".format(name, name)
        }.mkString(", ")
      )
  }
}

private[scope] trait HasVariables { self: Scope =>
  protected[this] var _variables = Set.empty[Variable]
  private[scope] def variables = _variables

  private[scope] def addVariable(variable: Variable) {
    _variables = _variables + variable
  }
  protected def variablesString: String = HasVariables.toString(_variables)
}

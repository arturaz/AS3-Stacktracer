package com.tinylabproductions.as3stacktracer.parser.scope

import com.tinylabproductions.as3stacktracer.parser.Scope

/**
 * Created by IntelliJ IDEA.
 * User: arturas
 * Date: 2/18/12
 * Time: 12:52 PM
 * To change this template use File | Settings | File Templates.
 */

private[scope] trait HasVariables { self: Scope =>
  protected[this] var _variables = Set.empty[String]
  private[scope] def variables = _variables

  private[scope] def addVariable(variable: String) {
    _variables = _variables + variable
  }
  protected[this] def variablesString: String = {
    // Get variables from parent
    val variables = parent match {
      case None => _variables
      case Some(p) => p match {
        case clazz: Class => _variables ++ clazz.variables
        case _ => _variables
      }
    }
    
    if (variables.isEmpty)
      "null"
    else
      "{%s}".format(
        variables.map { name => """"%s": %s""".format(name, name) }.
          mkString(", ")
      )
  }
}

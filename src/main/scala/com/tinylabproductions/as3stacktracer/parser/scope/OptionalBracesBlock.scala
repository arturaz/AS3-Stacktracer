package com.tinylabproductions.as3stacktracer.parser.scope

import com.tinylabproductions.as3stacktracer.parser.Scope

private[scope] object OptionalBracesBlock {
  val allMatchers = List(If, Else, For, While, Do)
}

private[scope] abstract class OptionalBracesBlock(
    body: String,
    name: String,
    parent: Scope,
    private[this] val lineNumAfterBlock: Boolean = true,
    private[this] var seekForBraces: Boolean = true,
    private[this] var usesBraces: Boolean = false)
  extends Block(body, name, parent)
          with SemicolonTracker
          with HasVariables
{

  protected[this] val matchers = List(Comment, Catch, LocalVariable, LocalFunction)
    .union(OptionalBracesBlock.allMatchers)
    .union(StringLike.allMatchers)

  // if we do not use braces we don't need to keep track of "pure" object
  // blocks inside as doing so would cause such a block not to have a closing
  // character
  protected def OpeningChar = if (usesBraces) '{' else '\0'
  protected def ClosingChar = if (usesBraces) '}' else ';'

  private[this] var enteredInnerBlock = false

  override def append(char: Char): Scope = {
    // we came here after an inner block has been fully processed
    // so end this block as it does not use braces and append the char to
    // the parent
    if (enteredInnerBlock) {
      afterClose()
      return parent.append(char)
    }

    if (seekForBraces) {
      // skip whitespace
      if (char.isWhitespace) {
        return this
      }
      // this statement uses braces after all
      else if (char == '{') {
        usesBraces = true
        seekForBraces = false
        return this
      }
      // the statement does not use braces
      else {
        seekForBraces = false
      }
    }

    val newScope = semicolonAppend(char) { () => super.append(char) }
    if (!usesBraces &&
        newScope != this &&
        newScope != parent &&
        newScope.isInstanceOf[Block]) {
      enteredInnerBlock = true
    }
    newScope
  }

  override def afterClose() {
    if (!usesBraces)
      addPart("}")
    if (lineNumAfterBlock)
      addPart(SemicolonTracker.lineNumAssignWithSemi(lineNum + 1))
  }
}

private[scope] class Condition(parent: Scope)
  extends Block("", "Condition", parent)
{
  protected[this] def OpeningChar = '('
  protected[this] def ClosingChar = ')'

  protected[this] val matchers = List(Comment).union(StringLike.allMatchers)
  protected[this] val scopeType = "Condition"
  def qualifiedName = "Condition"
}

private[scope] class ConditionBeforeBody(parent: Scope)
  extends Condition(parent)
{
  override protected[this] def afterClose() {
    addPart(" {\n" + SemicolonTracker.lineNumAssignWithSemi(lineNum))
  }
}

private[scope] trait BlockWithCondition extends OptionalBracesBlock
{
  def createCondition(): Condition = new ConditionBeforeBody(this)

  private[this] var processingCondition: Boolean = true
  private[this] val condition: Condition = createCondition()
  addPart(condition)

  override def append(char: Char) = {
    if (processingCondition) {
      processingCondition = false
      condition.append(char)
    }
    else {
      super.append(char)
    }
  }
}
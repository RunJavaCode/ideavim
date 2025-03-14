/*
 * IdeaVim - Vim emulator for IDEs based on the IntelliJ platform
 * Copyright (C) 2003-2021 The IdeaVim authors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.maddyhome.idea.vim.vimscript.model.functions

import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.LogicalPosition
import com.maddyhome.idea.vim.VimPlugin
import com.maddyhome.idea.vim.ex.ExException
import com.maddyhome.idea.vim.ex.FinishException
import com.maddyhome.idea.vim.ex.ranges.LineNumberRange
import com.maddyhome.idea.vim.ex.ranges.Ranges
import com.maddyhome.idea.vim.vimscript.model.Executable
import com.maddyhome.idea.vim.vimscript.model.ExecutionResult
import com.maddyhome.idea.vim.vimscript.model.datatypes.VimDataType
import com.maddyhome.idea.vim.vimscript.model.datatypes.VimInt
import com.maddyhome.idea.vim.vimscript.model.datatypes.VimList
import com.maddyhome.idea.vim.vimscript.model.expressions.Expression
import com.maddyhome.idea.vim.vimscript.model.expressions.Scope
import com.maddyhome.idea.vim.vimscript.model.expressions.Variable
import com.maddyhome.idea.vim.vimscript.model.statements.FunctionDeclaration
import com.maddyhome.idea.vim.vimscript.model.statements.FunctionFlag

data class DefinedFunctionHandler(val function: FunctionDeclaration) : FunctionHandler() {

  private val logger = logger<DefinedFunctionHandler>()
  override val name = function.name
  override val scope = function.scope
  override val minimumNumberOfArguments = function.args.size
  override val maximumNumberOfArguments get() = if (function.hasOptionalArguments) null else function.args.size + function.defaultArgs.size

  override fun doFunction(argumentValues: List<Expression>, editor: Editor, context: DataContext, parent: Executable): VimDataType {
    var returnValue: VimDataType? = null
    val exceptionsCaught = mutableListOf<ExException>()
    val isRangeGiven = (ranges?.size() ?: 0) > 0

    if (!isRangeGiven) {
      val currentLine = editor.caretModel.currentCaret.logicalPosition.line
      ranges = Ranges()
      ranges!!.addRange(
        arrayOf(
          LineNumberRange(currentLine, 0, false),
          LineNumberRange(currentLine, 0, false)
        )
      )
    }
    initializeFunctionVariables(argumentValues, editor, context)

    if (function.flags.contains(FunctionFlag.RANGE)) {
      val line = (VimPlugin.getVariableService().getNonNullVariableValue(Variable(Scope.FUNCTION_VARIABLE, "firstline"), editor, context, function) as VimInt).value
      returnValue = executeBodyForLine(line, isRangeGiven, exceptionsCaught, editor, context)
    } else {
      val firstLine = (VimPlugin.getVariableService().getNonNullVariableValue(Variable(Scope.FUNCTION_VARIABLE, "firstline"), editor, context, function) as VimInt).value
      val lastLine = (VimPlugin.getVariableService().getNonNullVariableValue(Variable(Scope.FUNCTION_VARIABLE, "lastline"), editor, context, function) as VimInt).value
      for (line in firstLine..lastLine) {
        returnValue = executeBodyForLine(line, isRangeGiven, exceptionsCaught, editor, context)
      }
    }

    if (exceptionsCaught.isNotEmpty()) {
      VimPlugin.indicateError()
      VimPlugin.showMessage(exceptionsCaught.last().message)
    }
    return returnValue ?: VimInt(0)
  }

  private fun executeBodyForLine(line: Int, isRangeGiven: Boolean, exceptionsCaught: MutableList<ExException>, editor: Editor, context: DataContext): VimDataType? {
    var returnValue: VimDataType? = null
    if (isRangeGiven) {
      editor.caretModel.moveToLogicalPosition(LogicalPosition(line - 1, 0))
    }
    var result: ExecutionResult = ExecutionResult.Success
    if (function.flags.contains(FunctionFlag.ABORT)) {
      for (statement in function.body) {
        statement.parent = function
        if (result is ExecutionResult.Success) {
          result = statement.execute(editor, context)
        }
      }
      // todo in release 1.9. we should return value AND throw exception
      when (result) {
        is ExecutionResult.Break -> exceptionsCaught.add(ExException("E587: :break without :while or :for: break"))
        is ExecutionResult.Continue -> exceptionsCaught.add(ExException("E586: :continue without :while or :for: continue"))
        is ExecutionResult.Error -> exceptionsCaught.add(ExException("unknown error occurred")) // todo
        is ExecutionResult.Return -> returnValue = result.value
        is ExecutionResult.Success -> {}
      }
    } else {
      // todo in release 1.9. in this case multiple exceptions can be thrown at once but we don't support it
      for (statement in function.body) {
        statement.parent = function
        try {
          result = statement.execute(editor, context)
          when (result) {
            is ExecutionResult.Break -> exceptionsCaught.add(ExException("E587: :break without :while or :for: break"))
            is ExecutionResult.Continue -> exceptionsCaught.add(ExException("E586: :continue without :while or :for: continue"))
            is ExecutionResult.Error -> exceptionsCaught.add(ExException("unknown error occurred")) // todo
            is ExecutionResult.Return -> {
              returnValue = result.value
              break
            }
            is ExecutionResult.Success -> {}
          }
        } catch (e: ExException) {
          if (e is FinishException) {
            // todo in 1.9: also throw all caught exceptions
            throw FinishException()
          }
          exceptionsCaught.add(e)
          logger.warn("Caught exception during execution of function with [abort] flag. Exception: ${e.message}")
        }
      }
    }
    return returnValue
  }

  private fun initializeFunctionVariables(argumentValues: List<Expression>, editor: Editor, context: DataContext) {
    // non-optional function arguments
    for ((index, name) in function.args.withIndex()) {
      VimPlugin.getVariableService().storeVariable(
        Variable(Scope.FUNCTION_VARIABLE, name),
        argumentValues[index].evaluate(editor, context, function.parent),
        editor,
        context,
        function
      )
    }
    // optional function arguments with default values
    for (index in 0 until function.defaultArgs.size) {
      val expressionToStore = if (index + function.args.size < argumentValues.size) argumentValues[index + function.args.size] else function.defaultArgs[index].second
      VimPlugin.getVariableService().storeVariable(
        Variable(Scope.FUNCTION_VARIABLE, function.defaultArgs[index].first),
        expressionToStore.evaluate(editor, context, function.parent),
        editor,
        context,
        function
      )
    }
    // all the other optional arguments passed to function are stored in a:000 variable
    if (function.hasOptionalArguments) {
      val remainingArgs = if (function.args.size + function.defaultArgs.size < argumentValues.size) {
        VimList(
          argumentValues.subList(function.args.size + function.defaultArgs.size, argumentValues.size)
            .map { it.evaluate(editor, context, function.parent) }.toMutableList()
        )
      } else {
        VimList(mutableListOf())
      }
      VimPlugin.getVariableService().storeVariable(
        Variable(Scope.FUNCTION_VARIABLE, "000"),
        remainingArgs,
        editor,
        context,
        function
      )
    }
    VimPlugin.getVariableService().storeVariable(
      Variable(Scope.FUNCTION_VARIABLE, "firstline"),
      VimInt(ranges!!.getFirstLine(editor, editor.caretModel.currentCaret) + 1), editor, context, function
    )
    VimPlugin.getVariableService().storeVariable(
      Variable(Scope.FUNCTION_VARIABLE, "lastline"),
      VimInt(ranges!!.getLine(editor, editor.caretModel.currentCaret) + 1), editor, context, function
    )
  }
}

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

package com.maddyhome.idea.vim.vimscript.model.commands

import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Editor
import com.maddyhome.idea.vim.VimPlugin
import com.maddyhome.idea.vim.ex.ExOutputModel
import com.maddyhome.idea.vim.ex.ranges.Ranges
import com.maddyhome.idea.vim.helper.EditorHelper
import com.maddyhome.idea.vim.helper.StringHelper.stringToKeys
import com.maddyhome.idea.vim.helper.StringHelper.toPrintableCharacters
import com.maddyhome.idea.vim.vimscript.model.ExecutionResult
import kotlin.math.absoluteValue

/**
 * see "h :jumps"
 */
data class JumpsCommand(val ranges: Ranges, val argument: String) : Command.SingleExecution(ranges, argument) {
  override val argFlags = flags(RangeFlag.RANGE_OPTIONAL, ArgumentFlag.ARGUMENT_FORBIDDEN, Access.READ_ONLY)
  override fun processCommand(editor: Editor, context: DataContext): ExecutionResult {
    val jumps = VimPlugin.getMark().jumps
    val spot = VimPlugin.getMark().jumpSpot

    val text = StringBuilder(" jump line  col file/text\n")
    jumps.forEachIndexed { idx, jump ->
      val jumpSizeMinusSpot = jumps.size - idx - spot - 1
      text.append(if (jumpSizeMinusSpot == 0) ">" else " ")
      text.append(jumpSizeMinusSpot.absoluteValue.toString().padStart(3))
      text.append(" ")
      text.append((jump.logicalLine + 1).toString().padStart(5))

      text.append("  ")
      text.append(jump.col.toString().padStart(3))

      text.append(" ")
      val vf = EditorHelper.getVirtualFile(editor)
      if (vf != null && vf.path == jump.filepath) {
        val line = EditorHelper.getLineText(editor, jump.logicalLine).trim().take(200)
        val keys = stringToKeys(line)
        text.append(toPrintableCharacters(keys).take(200))
      } else {
        text.append(jump.filepath)
      }

      text.append("\n")
    }

    if (spot == -1) {
      text.append(">\n")
    }

    ExOutputModel.getInstance(editor).output(text.toString())

    return ExecutionResult.Success
  }
}

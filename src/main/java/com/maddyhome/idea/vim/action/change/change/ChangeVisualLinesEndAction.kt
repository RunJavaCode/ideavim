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
package com.maddyhome.idea.vim.action.change.change

import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Editor
import com.maddyhome.idea.vim.VimPlugin
import com.maddyhome.idea.vim.command.Command
import com.maddyhome.idea.vim.command.CommandFlags
import com.maddyhome.idea.vim.command.CommandFlags.FLAG_EXIT_VISUAL
import com.maddyhome.idea.vim.command.CommandFlags.FLAG_MOT_LINEWISE
import com.maddyhome.idea.vim.command.CommandFlags.FLAG_MULTIKEY_UNDO
import com.maddyhome.idea.vim.command.OperatorArguments
import com.maddyhome.idea.vim.command.SelectionType
import com.maddyhome.idea.vim.common.TextRange
import com.maddyhome.idea.vim.group.visual.VimSelection
import com.maddyhome.idea.vim.handler.VisualOperatorActionHandler
import com.maddyhome.idea.vim.helper.EditorHelper
import com.maddyhome.idea.vim.helper.enumSetOf
import com.maddyhome.idea.vim.helper.fileSize
import java.util.*

/**
 * @author vlan
 */
class ChangeVisualLinesEndAction : VisualOperatorActionHandler.ForEachCaret() {
  override val type: Command.Type = Command.Type.CHANGE

  override val flags: EnumSet<CommandFlags> = enumSetOf(FLAG_MOT_LINEWISE, FLAG_MULTIKEY_UNDO, FLAG_EXIT_VISUAL)

  override fun executeAction(
    editor: Editor,
    caret: Caret,
    context: DataContext,
    cmd: Command,
    range: VimSelection,
    operatorArguments: OperatorArguments,
  ): Boolean {
    val vimTextRange = range.toVimTextRange(true)
    return if (range.type == SelectionType.BLOCK_WISE && vimTextRange.isMultiple) {
      val starts = vimTextRange.startOffsets
      val ends = vimTextRange.endOffsets
      for (i in starts.indices) {
        if (ends[i] > starts[i]) {
          ends[i] = EditorHelper.getLineEndForOffset(editor, starts[i])
        }
      }
      val blockRange = TextRange(starts, ends)
      VimPlugin.getChange().changeRange(editor, caret, blockRange, SelectionType.BLOCK_WISE, context)
    } else {
      val lineEndForOffset = EditorHelper.getLineEndForOffset(editor, vimTextRange.endOffset)
      val endsWithNewLine = if (lineEndForOffset == editor.fileSize) 0 else 1
      val lineRange = TextRange(
        EditorHelper.getLineStartForOffset(editor, vimTextRange.startOffset),
        lineEndForOffset + endsWithNewLine
      )
      VimPlugin.getChange().changeRange(editor, caret, lineRange, SelectionType.LINE_WISE, context)
    }
  }
}

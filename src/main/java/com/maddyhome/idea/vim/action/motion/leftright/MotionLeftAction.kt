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

package com.maddyhome.idea.vim.action.motion.leftright

import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Editor
import com.maddyhome.idea.vim.VimPlugin
import com.maddyhome.idea.vim.action.ComplicatedKeysAction
import com.maddyhome.idea.vim.command.Argument
import com.maddyhome.idea.vim.command.MotionType
import com.maddyhome.idea.vim.command.OperatorArguments
import com.maddyhome.idea.vim.handler.Motion
import com.maddyhome.idea.vim.handler.MotionActionHandler
import java.awt.event.KeyEvent
import javax.swing.KeyStroke

class MotionLeftAction : MotionActionHandler.ForEachCaret() {
  override val motionType: MotionType = MotionType.EXCLUSIVE

  override fun getOffset(
    editor: Editor,
    caret: Caret,
    context: DataContext,
    argument: Argument?,
    operatorArguments: OperatorArguments,
  ): Motion {
    val offsetOfHorizontalMotion = VimPlugin.getMotion().getOffsetOfHorizontalMotion(editor, caret, -operatorArguments.count1, false)
    return if (offsetOfHorizontalMotion < 0) Motion.Error else Motion.AbsoluteOffset(offsetOfHorizontalMotion)
  }
}

class MotionLeftInsertModeAction : MotionActionHandler.ForEachCaret(), ComplicatedKeysAction {
  override val motionType: MotionType = MotionType.EXCLUSIVE

  override val keyStrokesSet: Set<List<KeyStroke>> = setOf(
    listOf(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0)),
    listOf(KeyStroke.getKeyStroke(KeyEvent.VK_KP_LEFT, 0))
  )

  override fun getOffset(
    editor: Editor,
    caret: Caret,
    context: DataContext,
    argument: Argument?,
    operatorArguments: OperatorArguments,
  ): Motion {
    val offsetOfHorizontalMotion = VimPlugin.getMotion().getOffsetOfHorizontalMotion(editor, caret, -operatorArguments.count1, false)
    return if (offsetOfHorizontalMotion < 0) Motion.Error else Motion.AbsoluteOffset(offsetOfHorizontalMotion)
  }
}

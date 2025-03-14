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

package com.maddyhome.idea.vim.listener

import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.IdeActions
import com.intellij.openapi.actionSystem.ex.AnActionListener
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.util.Key
import com.maddyhome.idea.vim.KeyHandler
import com.maddyhome.idea.vim.VimPlugin
import com.maddyhome.idea.vim.action.motion.select.SelectToggleVisualMode
import com.maddyhome.idea.vim.group.visual.VimVisualTimer
import com.maddyhome.idea.vim.helper.fileSize
import com.maddyhome.idea.vim.helper.inVisualMode
import org.jetbrains.annotations.NotNull

/**
 * A collection of hacks to improve the interaction with fancy AppCode templates
 */
object AppCodeTemplates {
  private val facedAppCodeTemplate = Key.create<IntRange>("FacedAppCodeTemplate")

  private const val TEMPLATE_START = "<#T##"
  private const val TEMPLATE_END = "#>"

  class ActionListener : AnActionListener {

    private var editor: Editor? = null

    override fun beforeActionPerformed(action: AnAction, dataContext: DataContext, event: AnActionEvent) {
      if (!VimPlugin.isEnabled()) return

      val hostEditor = dataContext.getData(CommonDataKeys.HOST_EDITOR)
      if (hostEditor != null) {
        editor = hostEditor
      }
    }

    override fun afterActionPerformed(action: AnAction, dataContext: DataContext, event: AnActionEvent) {
      if (!VimPlugin.isEnabled()) return

      if (ActionManager.getInstance().getId(action) == IdeActions.ACTION_CHOOSE_LOOKUP_ITEM) {
        val myEditor = editor
        if (myEditor != null) {
          VimVisualTimer.doNow()
          if (myEditor.inVisualMode) {
            SelectToggleVisualMode.toggleMode(myEditor)
            KeyHandler.getInstance().partialReset(myEditor)
          }
        }
      }
    }
  }

  @JvmStatic
  fun onMovement(
    editor: @NotNull Editor,
    caret: @NotNull Caret,
    toRight: Boolean,
  ) {
    val offset = caret.offset
    val offsetRightEnd = offset + TEMPLATE_START.length
    val offsetLeftEnd = offset - 1
    val templateRange = caret.getUserData(facedAppCodeTemplate)
    if (templateRange == null) {
      if (offsetRightEnd < editor.fileSize &&
        editor.document.charsSequence.subSequence(offset, offsetRightEnd).toString() == TEMPLATE_START
      ) {
        caret.shake()

        val templateEnd = editor.findTemplateEnd(offset) ?: return

        caret.putUserData(facedAppCodeTemplate, offset..templateEnd)
      }
      if (offsetLeftEnd >= 0 &&
        offset + 1 <= editor.fileSize &&
        editor.document.charsSequence.subSequence(offsetLeftEnd, offset + 1).toString() == TEMPLATE_END
      ) {
        caret.shake()

        val templateStart = editor.findTemplateStart(offsetLeftEnd) ?: return

        caret.putUserData(facedAppCodeTemplate, templateStart..offset)
      }
    } else {
      if (offset in templateRange) {
        if (toRight) {
          caret.moveToOffset(templateRange.last + 1)
        } else {
          caret.moveToOffset(templateRange.first)
        }
      }
      caret.putUserData(facedAppCodeTemplate, null)
      caret.shake()
    }
  }

  fun Editor.appCodeTemplateCaptured(): Boolean {
    return this.caretModel.allCarets.any { it.getUserData(facedAppCodeTemplate) != null }
  }

  private fun Caret.shake() {
    moveCaretRelatively(1, 0, false, false)
    moveCaretRelatively(-1, 0, false, false)
  }

  private fun Editor.findTemplateEnd(start: Int): Int? {
    val charSequence = this.document.charsSequence
    val length = charSequence.length
    for (i in start until length - 1) {
      if (charSequence[i] == TEMPLATE_END[0] && charSequence[i + 1] == TEMPLATE_END[1]) {
        return i + 1
      }
    }
    return null
  }

  private fun Editor.findTemplateStart(start: Int): Int? {
    val charSequence = this.document.charsSequence
    val templateLastIndex = TEMPLATE_START.length
    for (i in start downTo templateLastIndex) {
      if (charSequence.subSequence(i - templateLastIndex, i).toString() == TEMPLATE_START) {
        return i - templateLastIndex
      }
    }
    return null
  }
}

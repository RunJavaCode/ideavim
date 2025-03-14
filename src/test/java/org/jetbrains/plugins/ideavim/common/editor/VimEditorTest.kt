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

package org.jetbrains.plugins.ideavim.common.editor

import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.command.WriteCommandAction
import com.maddyhome.idea.vim.common.editor.IjVimEditor
import com.maddyhome.idea.vim.common.editor.excl
import com.maddyhome.idea.vim.common.editor.incl
import org.jetbrains.plugins.ideavim.VimTestCase

class VimEditorTest : VimTestCase() {
  fun `test delete string`() {
    configureByText("01234567890")
    val vimEditor = IjVimEditor(myFixture.editor)
    WriteCommandAction.runWriteCommandAction(myFixture.project) {
      runWriteAction {
        vimEditor.deleteRange(0.incl, 5.excl)
      }
    }
    assertState("567890")
  }
}

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

package com.maddyhome.idea.vim.vimscript.model.expressions

import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Editor
import com.maddyhome.idea.vim.VimPlugin
import com.maddyhome.idea.vim.vimscript.model.Executable
import com.maddyhome.idea.vim.vimscript.model.datatypes.VimDataType

data class Variable(val scope: Scope?, val name: CurlyBracesName) : Expression() {
  constructor(scope: Scope?, name: String) : this(scope, CurlyBracesName(listOf(SimpleExpression(name))))

  override fun evaluate(editor: Editor, context: DataContext, parent: Executable): VimDataType {
    return VimPlugin.getVariableService().getNonNullVariableValue(this, editor, context, parent)
  }

  fun toString(editor: Editor, context: DataContext, parent: Executable): String {
    return (scope?.toString() ?: "") + name.evaluate(editor, context, parent)
  }
}

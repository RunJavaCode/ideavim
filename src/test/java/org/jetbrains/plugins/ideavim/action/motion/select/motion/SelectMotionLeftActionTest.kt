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

@file:Suppress("RemoveCurlyBracesFromTemplate")

package org.jetbrains.plugins.ideavim.action.motion.select.motion

import com.maddyhome.idea.vim.command.CommandState
import com.maddyhome.idea.vim.option.KeyModelOptionData
import org.jetbrains.plugins.ideavim.OptionValueType
import org.jetbrains.plugins.ideavim.SkipNeovimReason
import org.jetbrains.plugins.ideavim.TestWithoutNeovim
import org.jetbrains.plugins.ideavim.VimOptionTestCase
import org.jetbrains.plugins.ideavim.VimOptionTestConfiguration
import org.jetbrains.plugins.ideavim.VimTestOption

class SelectMotionLeftActionTest : VimOptionTestCase(KeyModelOptionData.name) {
  @TestWithoutNeovim(SkipNeovimReason.OPTION)
  @VimOptionTestConfiguration(
    VimTestOption(
      KeyModelOptionData.name,
      OptionValueType.STRING,
      KeyModelOptionData.stopselect
    )
  )
  fun `test char select simple move`() {
    doTest(
      listOf("viw", "<C-G>", "<Left>"),
      """
                A Discovery

                I ${c}found it in a legendary land
                all rocks and lavender and tufted grass,
                where it was settled on some sodden sand
                hard by the torrent of a mountain pass.
      """.trimIndent(),
      """
                A Discovery

                I foun${c}d it in a legendary land
                all rocks and lavender and tufted grass,
                where it was settled on some sodden sand
                hard by the torrent of a mountain pass.
      """.trimIndent(),
      CommandState.Mode.COMMAND,
      CommandState.SubMode.NONE
    )
  }

  @TestWithoutNeovim(SkipNeovimReason.OPTION)
  @VimOptionTestConfiguration(
    VimTestOption(
      KeyModelOptionData.name,
      OptionValueType.STRING,
      KeyModelOptionData.stopselect
    )
  )
  fun `test select multiple carets`() {
    doTest(
      listOf("viwo", "<C-G>", "<Left>"),
      """
                A Discovery

                I ${c}found it in a legendary land
                all rocks and lavender and tufted grass,
                ${c}where it was settled on some sodden sand
                hard by the torrent of a mountain pass.
      """.trimIndent(),
      """
                A Discovery

                I${c} found it in a legendary land
                all rocks and lavender and tufted grass,
                ${c}where it was settled on some sodden sand
                hard by the torrent of a mountain pass.
      """.trimIndent(),
      CommandState.Mode.COMMAND,
      CommandState.SubMode.NONE
    )
  }

  @TestWithoutNeovim(SkipNeovimReason.OPTION)
  @VimOptionTestConfiguration(VimTestOption(KeyModelOptionData.name, OptionValueType.STRING, ""))
  fun `test without stopsel`() {
    doTest(
      listOf("viw", "<C-G>", "<Left>"),
      """
                A Discovery

                I ${c}found it in a legendary land
                all rocks and lavender and tufted grass,
                where it was settled on some sodden sand
                hard by the torrent of a mountain pass.
      """.trimIndent(),
      """
                A Discovery

                I ${s}foun${c}${se}d it in a legendary land
                all rocks and lavender and tufted grass,
                where it was settled on some sodden sand
                hard by the torrent of a mountain pass.
      """.trimIndent(),
      CommandState.Mode.SELECT,
      CommandState.SubMode.VISUAL_CHARACTER
    )
  }
}

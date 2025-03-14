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

package org.jetbrains.plugins.ideavim.action.change.shift

import com.maddyhome.idea.vim.VimPlugin
import com.maddyhome.idea.vim.helper.StringHelper
import com.maddyhome.idea.vim.vimscript.services.OptionService
import org.jetbrains.plugins.ideavim.SkipNeovimReason
import org.jetbrains.plugins.ideavim.TestWithoutNeovim
import org.jetbrains.plugins.ideavim.VimTestCase

class ShiftLeftTest : VimTestCase() {
  @TestWithoutNeovim(SkipNeovimReason.DIFFERENT)
  fun `test shift till new line`() {
    val file = """
            A Discovery

              I found it in a legendary l${c}and
              all rocks and lavender and tufted grass,
              where it was settled on some sodden sand
              hard by the torrent of a mountain pass.
    """.trimIndent()
    typeTextInFile(StringHelper.parseKeys("<W"), file)
    assertState(
      """
            A Discovery

            ${c}I found it in a legendary land
              all rocks and lavender and tufted grass,
              where it was settled on some sodden sand
              hard by the torrent of a mountain pass.
      """.trimIndent()
    )
  }

  @TestWithoutNeovim(SkipNeovimReason.OPTION)
  fun `test shift left positions caret at first non-blank char`() {
    val file = """
      |A Discovery
      |
      |       I found it in a legendary l${c}and
      |       all rocks and lavender and tufted grass,
      |       where it was settled on some sodden sand
      |       hard by the torrent of a mountain pass.
    """.trimMargin()
    typeTextInFile(StringHelper.parseKeys("<<"), file)
    assertState(
      """
      |A Discovery

      |   ${c}I found it in a legendary land
      |       all rocks and lavender and tufted grass,
      |       where it was settled on some sodden sand
      |       hard by the torrent of a mountain pass.
      """.trimMargin()
    )
  }

  @TestWithoutNeovim(SkipNeovimReason.OPTION)
  fun `test shift left does not move caret with nostartofline`() {
    VimPlugin.getOptionService().unsetOption(OptionService.Scope.GLOBAL, "startofline")
    val file = """
      |A Discovery
      |
      |       I found it in a ${c}legendary land
      |       all rocks and lavender and tufted grass,
      |       where it was settled on some sodden sand
      |       hard by the torrent of a mountain pass.
    """.trimMargin()
    typeTextInFile(StringHelper.parseKeys("<<"), file)
    assertState(
      """
      |A Discovery

      |   I found it in a lege${c}ndary land
      |       all rocks and lavender and tufted grass,
      |       where it was settled on some sodden sand
      |       hard by the torrent of a mountain pass.
      """.trimMargin()
    )
  }

  @TestWithoutNeovim(SkipNeovimReason.OPTION)
  fun `test shift left positions caret at end of line with nostartofline`() {
    VimPlugin.getOptionService().unsetOption(OptionService.Scope.GLOBAL, "startofline")
    val file = """
      |A Discovery
      |
      |       I found it in a legendary la${c}nd
      |       all rocks and lavender and tufted grass,
      |       where it was settled on some sodden sand
      |       hard by the torrent of a mountain pass.
    """.trimMargin()
    typeTextInFile(StringHelper.parseKeys("<<"), file)
    assertState(
      """
      |A Discovery

      |   I found it in a legendary lan${c}d
      |       all rocks and lavender and tufted grass,
      |       where it was settled on some sodden sand
      |       hard by the torrent of a mountain pass.
      """.trimMargin()
    )
  }

  fun `test shift ctrl-D`() {
    val file = """
            A Discovery

              I found it in a legendary l${c}and
              all rocks and lavender and tufted grass,
              where it was settled on some sodden sand
              hard by the torrent of a mountain pass.
    """.trimIndent()
    typeTextInFile(StringHelper.parseKeys("i<C-D>"), file)
    assertState(
      """
            A Discovery

            I found it in a legendary land
              all rocks and lavender and tufted grass,
              where it was settled on some sodden sand
              hard by the torrent of a mountain pass.
      """.trimIndent()
    )
  }
}

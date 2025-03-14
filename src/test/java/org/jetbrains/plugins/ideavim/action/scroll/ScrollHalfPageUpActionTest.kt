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

package org.jetbrains.plugins.ideavim.action.scroll

import com.maddyhome.idea.vim.VimPlugin
import com.maddyhome.idea.vim.helper.StringHelper.parseKeys
import com.maddyhome.idea.vim.vimscript.model.datatypes.VimInt
import com.maddyhome.idea.vim.vimscript.services.OptionService
import org.jetbrains.plugins.ideavim.SkipNeovimReason
import org.jetbrains.plugins.ideavim.TestWithoutNeovim
import org.jetbrains.plugins.ideavim.VimTestCase

/*
                                                       *CTRL-U*
CTRL-U                  Scroll window Upwards in the buffer.  The number of
                        lines comes from the 'scroll' option (default: half a
                        screen).  If [count] given, first set the 'scroll'
                        option to [count].  The cursor is moved the same
                        number of lines up in the file (if possible; when
                        lines wrap and when hitting the end of the file there
                        may be a difference).  When the cursor is on the first
                        line of the buffer nothing happens and a beep is
                        produced.  See also 'startofline' option.
 */
class ScrollHalfPageUpActionTest : VimTestCase() {
  @TestWithoutNeovim(SkipNeovimReason.SCROLL)
  fun`test scroll half window upwards keeps cursor on same relative line`() {
    configureByPages(5)
    setPositionAndScroll(50, 60)
    typeText(parseKeys("<C-U>"))
    assertPosition(43, 0)
    assertVisibleArea(33, 67)
  }

  @TestWithoutNeovim(SkipNeovimReason.SCROLL)
  fun`test scroll upwards on first line causes beep`() {
    configureByPages(5)
    setPositionAndScroll(0, 0)
    typeText(parseKeys("<C-U>"))
    assertPosition(0, 0)
    assertVisibleArea(0, 34)
    assertTrue(VimPlugin.isError())
  }

  @TestWithoutNeovim(SkipNeovimReason.SCROLL)
  fun`test scroll upwards in first half of first page moves to first line`() {
    configureByPages(5)
    setPositionAndScroll(5, 10)
    typeText(parseKeys("<C-U>"))
    assertPosition(0, 0)
    assertVisibleArea(0, 34)
  }

  @TestWithoutNeovim(SkipNeovimReason.SCROLL)
  fun`test scroll upwards in first half of first page moves to first line with scrolloff`() {
    VimPlugin.getOptionService().setOptionValue(OptionService.Scope.GLOBAL, "scrolloff", VimInt(10))
    configureByPages(5)
    setPositionAndScroll(5, 15)
    typeText(parseKeys("<C-U>"))
    assertPosition(0, 0)
    assertVisibleArea(0, 34)
  }

  @TestWithoutNeovim(SkipNeovimReason.SCROLL)
  fun`test scroll count lines upwards`() {
    configureByPages(5)
    setPositionAndScroll(50, 53)
    typeText(parseKeys("10<C-U>"))
    assertPosition(43, 0)
    assertVisibleArea(40, 74)
  }

  @TestWithoutNeovim(SkipNeovimReason.SCROLL)
  fun`test scroll count modifies scroll option`() {
    configureByPages(5)
    setPositionAndScroll(50, 53)
    typeText(parseKeys("10<C-U>"))
    assertEquals((VimPlugin.getOptionService().getOptionValue(OptionService.Scope.GLOBAL, "scroll") as VimInt).value, 10)
  }

  @TestWithoutNeovim(SkipNeovimReason.SCROLL)
  fun`test scroll upwards uses scroll option`() {
    VimPlugin.getOptionService().setOptionValue(OptionService.Scope.GLOBAL, "scroll", VimInt(10))
    configureByPages(5)
    setPositionAndScroll(50, 53)
    typeText(parseKeys("<C-U>"))
    assertPosition(43, 0)
    assertVisibleArea(40, 74)
  }

  @TestWithoutNeovim(SkipNeovimReason.SCROLL)
  fun`test count scroll upwards is limited to a single page`() {
    configureByPages(5)
    setPositionAndScroll(100, 134)
    typeText(parseKeys("50<C-U>"))
    assertPosition(99, 0)
    assertVisibleArea(65, 99)
  }

  @TestWithoutNeovim(SkipNeovimReason.SCROLL)
  fun`test scroll up puts cursor on first non-blank column`() {
    configureByLines(100, "    I found it in a legendary land")
    setPositionAndScroll(50, 60, 14)
    typeText(parseKeys("<C-U>"))
    assertPosition(43, 4)
    assertVisibleArea(33, 67)
  }

  @TestWithoutNeovim(SkipNeovimReason.SCROLL)
  fun`test scroll upwards keeps same column with nostartofline`() {
    VimPlugin.getOptionService().unsetOption(OptionService.Scope.GLOBAL, "startofline")
    configureByLines(100, "    I found it in a legendary land")
    setPositionAndScroll(50, 60, 14)
    typeText(parseKeys("<C-U>"))
    assertPosition(43, 14)
    assertVisibleArea(33, 67)
  }
}

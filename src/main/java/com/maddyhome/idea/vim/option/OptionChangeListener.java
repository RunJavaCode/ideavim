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

package com.maddyhome.idea.vim.option;

import org.jetbrains.annotations.ApiStatus;
/**
 * This interface is used for classes that wish to be notified whenever the value of an option has changed
 * @deprecated use {@link com.maddyhome.idea.vim.vimscript.model.options.OptionChangeListener} instead
 */
@Deprecated
@ApiStatus.ScheduledForRemoval(inVersion = "1.11")
public interface OptionChangeListener<T> {
  void valueChange(T oldValue, T newValue);
}

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

package org.jetbrains.plugins.ideavim

import com.maddyhome.idea.vim.VimPlugin
import com.maddyhome.idea.vim.vimscript.model.datatypes.VimInt
import com.maddyhome.idea.vim.vimscript.model.datatypes.VimString
import com.maddyhome.idea.vim.vimscript.services.OptionService

/**
 * @author Alex Plate
 *
 * This test case helps you to test IdeaVim options
 *
 * While inheriting from this class you should specify (via constructor), which options you are going to test.
 *   After that each test method in this class should contains [VimOptionTestConfiguration] annotation with
 *   description of which values of option should be set before starting test.
 *
 * e.g.
 * ```
 * @VimOptionTestConfiguration(VimTestOption("keymodel", LIST, ["startsel"]), VimTestOption("selectmode", LIST, ["key"]))
 * ```
 *
 * If you want to keep default configuration, you can put [VimOptionDefaultAll] annotation
 */
abstract class VimOptionTestCase(option: String, vararg otherOptions: String) : VimTestCase() {
  private val options: Set<String> = setOf(option, *otherOptions)

  override fun setUp() {
    super.setUp()
    val testMethod = this.javaClass.getMethod(this.name)
    if (!testMethod.isAnnotationPresent(VimOptionDefaultAll::class.java)) {
      if (!testMethod.isAnnotationPresent(VimOptionTestConfiguration::class.java)) kotlin.test.fail("You should add VimOptionTestConfiguration with options for this method")

      val annotationValues = testMethod.getDeclaredAnnotation(VimOptionTestConfiguration::class.java) ?: run {
        kotlin.test.fail("You should have at least one VimOptionTestConfiguration annotation. Or you can use VimOptionDefaultAll")
      }
      val defaultOptions = testMethod.getDeclaredAnnotation(VimOptionDefault::class.java)?.values ?: emptyArray()

      val annotationsValueList = annotationValues.value.map { it.optionName } + defaultOptions
      val annotationsValueSet = annotationsValueList.toSet()
      if (annotationsValueSet.size < annotationsValueList.size) kotlin.test.fail("You have duplicated options")
      if (annotationsValueSet != options) kotlin.test.fail("You should present all options in annotations")

      annotationValues.value.forEach {
        when (it.valueType) {
          OptionValueType.STRING -> VimPlugin.getOptionService().setOptionValue(OptionService.Scope.GLOBAL, it.optionName, VimString(it.value))
          OptionValueType.NUMBER -> VimPlugin.getOptionService().setOptionValue(OptionService.Scope.GLOBAL, it.optionName, VimInt(it.value))
        }
      }
    }
  }
}

@Target(AnnotationTarget.FUNCTION)
annotation class VimOptionDefaultAll

@Target(AnnotationTarget.FUNCTION)
annotation class VimOptionDefault(vararg val values: String)

@Target(AnnotationTarget.FUNCTION)
annotation class VimOptionTestConfiguration(vararg val value: VimTestOption)

@Target(AnnotationTarget.PROPERTY)
annotation class VimTestOption(
  val optionName: String,
  val valueType: OptionValueType,
  val value: String,
)

enum class OptionValueType {
  STRING,
  NUMBER,
}

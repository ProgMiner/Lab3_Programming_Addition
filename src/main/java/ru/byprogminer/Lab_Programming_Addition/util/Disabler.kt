/* MIT License

Copyright (c) 2018 Eridan Domoratskiy

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE. */

package ru.byprogminer.Lab_Programming_Addition.util

import java.lang.reflect.Field

import javax.swing.JComponent

class Disabler <in C: Any> {

    private lateinit var components: Map<Field, Boolean>

    fun disableAll(obj: C) {
        val components = mutableMapOf<Field, Boolean>()

        for (field in obj::class.java.declaredFields) {
            if (JComponent::class.java.isAssignableFrom(field.type)) {
                field.isAccessible = true
                components[field] = (field.get(obj) as JComponent).isEnabled
                (field.get(obj) as JComponent).isEnabled = false
                field.isAccessible = false
            }
        }

        this.components = components
    }

    fun revert(obj: C) {
        if (!this::components.isInitialized) {
            throw RuntimeException("Components isn't initialized")
        }

        for ((field, value) in components) {
            field.isAccessible = true
            (field.get(obj) as JComponent).isEnabled = value
            field.isAccessible = false
        }

    }
}

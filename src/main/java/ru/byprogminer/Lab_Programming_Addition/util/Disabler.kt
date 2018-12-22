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

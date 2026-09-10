package org.polyfrost.waveycapes.config

import net.ornithemc.osl.config.api.config.option.BaseOption

class EnumOption<E : Enum<E>>(
    name: String,
    description: String,
    defaultValue: E,
    val values: Array<E>,
) : BaseOption<E>(name, description, defaultValue) {
    fun setByName(name: String) {
        values.firstOrNull { it.name == name }?.let(::set)
    }
}

inline fun <reified E : Enum<E>> enumOption(name: String, description: String, defaultValue: E) =
    EnumOption(name, description, defaultValue, enumValues<E>())

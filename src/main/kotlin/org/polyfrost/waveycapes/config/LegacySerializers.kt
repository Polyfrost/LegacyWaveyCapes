package org.polyfrost.waveycapes.config

import net.ornithemc.osl.config.api.serdes.FileSerializerType
import net.ornithemc.osl.config.api.serdes.SerializerTypes
import net.ornithemc.osl.config.api.serdes.config.ConfigSerializers
import net.ornithemc.osl.core.api.json.JsonFile
import org.polyfrost.waveycapes.WaveyCapes

object LegacySerializers {
    val FLAT_JSON: FileSerializerType<JsonFile> =
        SerializerTypes.register("${WaveyCapes.MOD_ID}:flat_json", FileSerializerType { JsonFile(it) })

    fun register() {
        ConfigSerializers.register(FLAT_JSON, LegacyJsonConfigSerializer())
    }
}

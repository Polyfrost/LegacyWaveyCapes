package org.polyfrost.waveycapes.config

import net.ornithemc.osl.config.api.config.Config
import net.ornithemc.osl.config.api.config.option.IntegerOption
import net.ornithemc.osl.config.api.config.option.Option
import net.ornithemc.osl.config.api.serdes.SerializationSettings
import net.ornithemc.osl.config.api.serdes.config.ConfigSerializer
import net.ornithemc.osl.core.api.json.JsonFile

class LegacyJsonConfigSerializer : ConfigSerializer<JsonFile> {

    override fun serialize(config: Config, settings: SerializationSettings, json: JsonFile) {
        json.use {
            it.write()
            it.writeNumber(VERSION_KEY, config.version)
            for ((key, option) in entriesOf(config)) {
                when (option) {
                    is IntegerOption -> it.writeNumber(key, option.get())
                    is EnumOption<*> -> it.writeString(key, option.get().name)
                }
            }
        }
    }

    override fun deserialize(config: Config, settings: SerializationSettings, json: JsonFile) {
        val options = entriesOf(config).toMap()
        var fileVersion = config.version

        json.use {
            it.read()
            while (it.hasNext()) {
                val key = it.readName()
                if (key == VERSION_KEY) {
                    fileVersion = it.readNumber().toInt()
                    continue
                }
                when (val option = options[key]) {
                    is IntegerOption -> option.set(it.readNumber().toInt())
                    is EnumOption<*> -> option.setByName(it.readString())
                    else -> it.skipValue()
                }
            }
        }

        (config as WaveyCapesConfig).upgrade(fileVersion)
    }

    private fun entriesOf(config: Config): List<Pair<String, Option>> =
        (config as WaveyCapesConfig).legacyEntries

    private companion object {
        const val VERSION_KEY = "configVersion"
    }
}

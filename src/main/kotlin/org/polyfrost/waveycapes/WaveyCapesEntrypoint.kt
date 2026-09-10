package org.polyfrost.waveycapes

import net.ornithemc.osl.config.api.ConfigManager
import net.ornithemc.osl.entrypoints.api.client.ClientModInitializer
import org.polyfrost.waveycapes.config.LegacySerializers
import org.polyfrost.waveycapes.config.WaveyCapesConfig

class WaveyCapesEntrypoint : ClientModInitializer {
    override fun initClient() {
        LegacySerializers.register()
        ConfigManager.register(WaveyCapesConfig)
    }
}

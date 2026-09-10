package org.polyfrost.waveycapes

import net.minecraft.client.network.AbstractClientPlayerEntity
import net.minecraft.util.Identifier

internal val AbstractClientPlayerEntity.capeTexture: Identifier?
    get() = if (canRenderCapeTexture()) skinId else null

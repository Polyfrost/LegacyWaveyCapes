package org.polyfrost.waveycapes

import com.mojang.blaze3d.platform.GlStateManager
import net.minecraft.client.MinecraftClient
import net.minecraft.client.network.AbstractClientPlayerEntity
import net.minecraft.client.render.entity.PlayerModelPart
import org.polyfrost.waveycapes.config.WaveyCapesConfig
import org.polyfrost.waveycapes.render.CapeMesh
import org.polyfrost.waveycapes.render.CapePoses

object CapeLayer {
    private val poses = CapePoses()
    private val mesh = CapeMesh()

    @JvmStatic
    fun render(player: AbstractClientPlayerEntity, delta: Float) {
        if (player.isInvisible || !player.isPartVisible(PlayerModelPart.CAPE)) return
        val cape = player.capeTexture ?: return

        poses.build(player, (player as CapeHolder).capeSimulation, delta)

        GlStateManager.color(1f, 1f, 1f, 1f)
        MinecraftClient.getInstance().textureManager.bindTexture(cape)
        mesh.draw(poses.poses, WaveyCapesConfig.capeStyle.get() == CapeStyle.SMOOTH)
    }
}

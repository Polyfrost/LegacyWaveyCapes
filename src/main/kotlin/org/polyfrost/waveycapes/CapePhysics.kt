package org.polyfrost.waveycapes

import net.minecraft.block.material.Material
import net.minecraft.client.network.AbstractClientPlayerEntity
import org.polyfrost.waveycapes.config.WaveyCapesConfig
import org.polyfrost.waveycapes.sim.CapeSimulation
import kotlin.math.cos
import kotlin.math.sin

object CapePhysics {
    @JvmStatic
    fun tick(player: AbstractClientPlayerEntity) {
        val holder = player as CapeHolder
        val movement = WaveyCapesConfig.capeMovement.get()

        if (movement == CapeMovement.VANILLA || player.capeTexture == null) {
            holder.capeSimulation = null
            return
        }

        var simulation = holder.capeSimulation
        if (simulation == null || simulation.movement != movement) {
            simulation = movement.createSimulation() ?: return
            holder.capeSimulation = simulation
            simulation.applyMovement(1f, 1f, 0f)
            repeat(SETTLE_STEPS) { step(simulation, player) }
        }

        step(simulation, player)
    }

    private fun step(simulation: CapeSimulation, player: AbstractClientPlayerEntity) {
        val underwater = player.isSubmergedIn(Material.WATER)

        val driftX = player.prevCapeX - player.x
        val driftZ = player.prevCapeZ - player.z
        val bodyYaw = player.bodyYaw * RADIANS_PER_DEGREE
        val forward = sin(bodyYaw).toDouble()
        val sideways = -cos(bodyYaw).toDouble()

        val height = WaveyCapesConfig.heightMultiplier.get() * if (underwater) 2 else 1
        simulation.gravity = WaveyCapesConfig.gravity.get().toFloat() / if (underwater) 10f else 1f

        val fall = ((player.prevY - player.y) * 10.0).coerceIn(0.0, 1.0)
        val crouching = player.isSneaking
        val startedCrouching = crouching && !simulation.sneaking

        val yaw = player.yaw * RADIANS_PER_DEGREE
        val strafe = cos(yaw) * (player.x - player.prevX).toFloat() +
            sin(yaw) * (player.z - player.prevZ).toFloat()

        simulation.sneaking = crouching
        simulation.applyMovement(
            (driftX * forward + driftZ * sideways + fall + if (startedCrouching) 3.0 else 0.0).toFloat(),
            ((player.y - player.prevY) * height + if (startedCrouching) 1.0 else 0.0).toFloat(),
            -strafe * WaveyCapesConfig.straveMultiplier.get(),
        )
        simulation.simulate()
    }

    private const val SETTLE_STEPS = 5
    private const val RADIANS_PER_DEGREE = 0.017453292f
}

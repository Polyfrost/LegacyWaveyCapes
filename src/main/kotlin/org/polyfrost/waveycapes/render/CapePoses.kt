package org.polyfrost.waveycapes.render

import net.minecraft.block.material.Material
import net.minecraft.client.network.AbstractClientPlayerEntity
import net.minecraft.util.math.MathHelper
import org.polyfrost.waveycapes.WaveyCapes.CAPE_PARTS
import org.polyfrost.waveycapes.WindMode
import org.polyfrost.waveycapes.config.WaveyCapesConfig
import org.polyfrost.waveycapes.sim.CapeSimulation
import kotlin.math.atan2
import kotlin.math.sin

class CapePoses {
    val poses: Array<Pose> = Array(CAPE_PARTS) { Pose() }

    private var windPhase = 0.0
    private var windy = false
    private var sneaking = false

    fun build(player: AbstractClientPlayerEntity, simulation: CapeSimulation?, delta: Float) {
        val underwater = player.isSubmergedIn(Material.WATER)
        windy = WaveyCapesConfig.windMode.get() == WindMode.WAVES
        windPhase = (System.currentTimeMillis() / (if (underwater) 9 else 3) % 360).toDouble()
        sneaking = player.isSneaking

        if (simulation == null) vanilla(player, delta) else simulated(simulation, delta)
    }

    private fun applySneak(pose: Pose) {
        if (sneaking) pose.translate(0f, SNEAK_DROP, 0f)
    }

    private fun vanilla(player: AbstractClientPlayerEntity, delta: Float) {
        val dx = lerp(player.capeX, player.prevCapeX, delta) - lerp(player.prevX, player.x, delta)
        val dy = lerp(player.capeY, player.prevCapeY, delta) - lerp(player.prevY, player.y, delta)
        val dz = lerp(player.capeZ, player.prevCapeZ, delta) - lerp(player.prevZ, player.z, delta)

        val bodyYaw = player.prevBodyYaw + (player.bodyYaw - player.prevBodyYaw) * delta
        val forward = MathHelper.sin(bodyYaw * RADIANS_PER_DEGREE).toDouble()
        val sideways = -MathHelper.cos(bodyYaw * RADIANS_PER_DEGREE).toDouble()

        val swing = ((dx * forward + dz * sideways) * 100.0).toFloat()
        val lean = MathHelper.clamp(((dx * sideways - dz * forward) * 100.0).toFloat(), -20f, 20f)

        var height = MathHelper.clamp((dy * 10.0).toFloat(), -6f, 32f)
        val bob = lerp(player.prevStrideDistance, player.strideDistance, delta)
        val walked = lerp(player.prevHorizontalSpeed, player.horizontalSpeed, delta)
        height += MathHelper.sin(walked * 6f) * 32f * bob
        if (sneaking) height += SNEAK_ANGLE

        for (part in 0 until CAPE_PARTS) {
            val ease = easeOutSine(part.toFloat() / CAPE_PARTS)
            val partSwing = MathHelper.clamp(swing * ease, 0f, 150f * ease)

            val pose = poses[part]
            pose.identity()
            pose.translate(0f, 0f, SHOULDER_OFFSET)
            applySneak(pose)
            pose.rotateX(REST_ANGLE + partSwing / 2f + height + windSwing(part))
            pose.rotateZ(lean / 2f)
            pose.rotateY(180f - lean / 2f)
        }
    }

    private fun simulated(simulation: CapeSimulation, delta: Float) {
        val baseX = simulation.lerpX(0, delta)
        val baseY = simulation.lerpY(0, delta)
        val baseZ = simulation.lerpZ(0, delta)

        for (part in 0 until CAPE_PARTS) {
            val offsetX = (simulation.lerpX(part, delta) - baseX).coerceAtMost(0f)
            val offsetY = baseY - part - simulation.lerpY(part, delta)
            val offsetZ = baseZ - simulation.lerpZ(part, delta)

            val pivotY = (part + PIVOT_INSET) / PIXELS_PER_BLOCK
            val pivotZ = -PIVOT_INSET / PIXELS_PER_BLOCK

            val pose = poses[part]
            pose.identity()
            pose.translate(0f, 0f, SHOULDER_OFFSET)
            applySneak(pose)
            pose.rotateX(REST_ANGLE + sneakAngle() + windSwing(part))
            pose.rotateY(180f)
            pose.translate(-offsetZ / CAPE_PARTS, offsetY / CAPE_PARTS, offsetX / CAPE_PARTS)
            pose.translate(0f, pivotY, pivotZ)
            pose.rotateX(-segmentAngle(simulation, part, delta))
            pose.translate(0f, -pivotY, -pivotZ)
        }
    }

    private fun segmentAngle(simulation: CapeSimulation, part: Int, delta: Float): Float {
        val from = if (part == CAPE_PARTS - 1) part - 1 else part
        val dx = simulation.lerpX(from + 1, delta) - simulation.lerpX(from, delta)
        val dy = simulation.lerpY(from + 1, delta) - simulation.lerpY(from, delta)
        return (Math.toDegrees(atan2(dx.toDouble(), dy.toDouble())) + 180.0).toFloat()
    }

    private fun windSwing(part: Int): Float {
        if (!windy) return 0f
        val reach = (part + 1).toDouble() / CAPE_PARTS
        return (sin(Math.toRadians(reach * 360.0 - windPhase)) * WIND_STRENGTH).toFloat()
    }

    private fun sneakAngle() = if (sneaking) SNEAK_ANGLE else 0f

    private fun easeOutSine(progress: Float) = MathHelper.sin(progress * QUARTER_TURN)

    private fun lerp(from: Double, to: Double, delta: Float) = from + (to - from) * delta

    private fun lerp(from: Float, to: Float, delta: Float) = from + (to - from) * delta

    private companion object {
        const val RADIANS_PER_DEGREE = 0.017453292f
        const val PIXELS_PER_BLOCK = 16f

        const val QUARTER_TURN = 1.5707964f
        const val SHOULDER_OFFSET = 0.125f
        const val REST_ANGLE = 6f

        const val SNEAK_ANGLE = 25f
        const val SNEAK_DROP = 0.15f

        const val PIVOT_INSET = 0.48f
        const val WIND_STRENGTH = 3.0
    }
}

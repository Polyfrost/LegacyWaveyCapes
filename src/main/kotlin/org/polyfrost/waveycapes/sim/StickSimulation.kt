package org.polyfrost.waveycapes.sim

import org.polyfrost.waveycapes.CapeMovement
import org.polyfrost.waveycapes.WaveyCapes.CAPE_PARTS
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

/**
 * Based on WaveyCapes, itself a derivative of Sebastian Lague's rope simulation.
 */
class StickSimulation : CapeSimulation(CapeMovement.BASIC_SIMULATION, ITERATIONS) {
    override fun applyMovement(dx: Float, dy: Float, dz: Float) = super.applyMovement(dx, dy, 0f)

    override fun simulate() {
        applyGravity()
        preventClipping()
        preventHardBends()
        applyMotion()
        limitLength()
    }

    /** Stops the cape folding back onto itself by capping the angle at each joint. */
    private fun preventHardBends() {
        for (i in CAPE_PARTS - 2 downTo 1) {
            var angle = Math.toDegrees(
                atan2((y[i + 1] - y[i]).toDouble(), (x[i + 1] - x[i]).toDouble()) -
                    atan2((y[i - 1] - y[i]).toDouble(), (x[i - 1] - x[i]).toDouble())
            )
            if (angle > 360.0) angle -= 360.0
            if (angle < -360.0) angle += 360.0

            val bend = abs(angle)
            when {
                bend < STRAIGHT - MAX_BEND -> straighten(i, angle, STRAIGHT - MAX_BEND + 1.0)
                bend > STRAIGHT + MAX_BEND -> straighten(i, angle, STRAIGHT + MAX_BEND - 1.0)
            }
        }
    }

    private fun straighten(index: Int, angle: Double, target: Double) {
        val theta = Math.toRadians(if (angle < 0.0) -target else target)
        val dx = (x[index - 1] - x[index]).toDouble()
        val dy = (y[index - 1] - y[index]).toDouble()
        val cos = cos(theta)
        val sin = sin(theta)

        x[index + 1] = (dx * cos - dy * sin).toFloat() + x[index]
        y[index + 1] = (dx * sin + dy * cos).toFloat() + y[index]
    }

    private companion object {
        const val ITERATIONS = 3
        const val MAX_BEND = 5.0
        const val STRAIGHT = 180.0
    }
}

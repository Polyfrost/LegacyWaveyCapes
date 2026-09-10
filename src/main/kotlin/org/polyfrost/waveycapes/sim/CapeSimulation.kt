package org.polyfrost.waveycapes.sim

import org.polyfrost.waveycapes.CapeMovement
import org.polyfrost.waveycapes.WaveyCapes.CAPE_PARTS
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

abstract class CapeSimulation(val movement: CapeMovement, private val iterations: Int) {
    protected val x = FloatArray(CAPE_PARTS)
    protected val y = FloatArray(CAPE_PARTS)
    protected val z = FloatArray(CAPE_PARTS)

    private val prevX = FloatArray(CAPE_PARTS)
    private val prevY = FloatArray(CAPE_PARTS)
    private val prevZ = FloatArray(CAPE_PARTS)

    private val unit = FloatArray(3)

    var gravity: Float = 0f
    var sneaking: Boolean = false

    init {
        for (i in 0 until CAPE_PARTS) {
            y[i] = -i.toFloat()
        }
    }

    abstract fun simulate()

    open fun applyMovement(dx: Float, dy: Float, dz: Float) {
        prevX[0] = x[0]
        prevY[0] = y[0]
        prevZ[0] = z[0]
        x[0] += dx
        y[0] += dy
        z[0] += dz
    }

    fun lerpX(index: Int, delta: Float): Float = prevX[index] + (x[index] - prevX[index]) * delta

    fun lerpY(index: Int, delta: Float): Float = prevY[index] + (y[index] - prevY[index]) * delta

    fun lerpZ(index: Int, delta: Float): Float = prevZ[index] + (z[index] - prevZ[index]) * delta

    protected fun applyGravity() {
        val fall = gravity * TICK_SECONDS
        for (i in 1 until CAPE_PARTS) {
            prevX[i] = x[i]
            prevY[i] = y[i]
            prevZ[i] = z[i]
            y[i] -= fall
        }
    }

    protected fun preventClipping() {
        for (i in 1 until CAPE_PARTS) {
            if (x[i] > x[0]) {
                x[i] = x[0]
            }
            val reach = i.toFloat() / CAPE_PARTS
            val maxZ = reach * reach * SIDEWAYS_REACH
            val offset = z[0] - z[i]
            if (offset > maxZ) {
                z[i] = z[0] - maxZ
            } else if (offset < -maxZ) {
                z[i] = z[0] + maxZ
            }
        }
    }

    protected fun applyMotion() {
        repeat(iterations) {
            for (i in CAPE_PARTS - 2 downTo 0) {
                separate(i, i + 1, keepFirst = i == 0)
            }
        }
    }

    protected fun limitLength() {
        for (i in 0 until CAPE_PARTS - 1) {
            val next = i + 1
            direction(x[i] - x[next], y[i] - y[next], z[i] - z[next])
            x[next] = x[i] - unit[0]
            y[next] = y[i] - unit[1]
            z[next] = z[i] - unit[2]
        }
    }

    protected fun preventHardBend(index: Int, maxBend: Double) {
        val ax = (x[index - 1] - x[index]).toDouble()
        val ay = (y[index - 1] - y[index]).toDouble()
        val bx = (x[index - 1] - x[index + 1]).toDouble()
        val by = (y[index - 1] - y[index + 1]).toDouble()
        val bend = Math.toDegrees(atan2(ax * by - ay * bx, ax * bx + ay * by))

        if (bend < -maxBend) swing(index, -maxBend * 2)
        if (bend > maxBend) swing(index, maxBend * 2)
    }

    private fun swing(index: Int, degrees: Double) {
        val dx = (x[index] - x[index - 1]).toDouble()
        val dy = (y[index] - y[index - 1]).toDouble()
        val theta = Math.toRadians(degrees)
        val cos = cos(theta)
        val sin = sin(theta)

        x[index + 1] = (dx * cos - dy * sin).toFloat() + x[index]
        y[index + 1] = (dx * sin + dy * cos).toFloat() + y[index]
        z[index + 1] = z[index] * 2f - z[index - 1]
    }

    protected fun preventSelfClipping(maxFixes: Int) {
        var fixes = 0
        var clipped: Boolean
        do {
            clipped = false
            for (a in 0 until CAPE_PARTS) {
                for (b in a + 1 until CAPE_PARTS) {
                    val dx = x[a] - x[b]
                    val dy = y[a] - y[b]
                    val dz = z[a] - z[b]
                    if (dx * dx + dy * dy + dz * dz >= MIN_SEPARATION_SQUARED) continue

                    clipped = true
                    fixes++
                    separate(a, b, keepFirst = a == 0)
                }
            }
        } while (clipped && fixes < maxFixes)
    }

    private fun separate(first: Int, second: Int, keepFirst: Boolean) {
        direction(x[first] - x[second], y[first] - y[second], z[first] - z[second])
        val centreX = (x[first] + x[second]) / 2f
        val centreY = (y[first] + y[second]) / 2f
        val centreZ = (z[first] + z[second]) / 2f

        if (!keepFirst) {
            x[first] = centreX + unit[0] / 2f
            y[first] = centreY + unit[1] / 2f
            z[first] = centreZ + unit[2] / 2f
        }
        x[second] = centreX - unit[0] / 2f
        y[second] = centreY - unit[1] / 2f
        z[second] = centreZ - unit[2] / 2f
    }

    private fun direction(dx: Float, dy: Float, dz: Float) {
        val length = sqrt(dx * dx + dy * dy + dz * dz)
        if (length < EPSILON) {
            unit.fill(0f)
        } else {
            unit[0] = dx / length
            unit[1] = dy / length
            unit[2] = dz / length
        }
    }

    protected companion object {
        const val TICK_SECONDS = 50f / 1000f
        const val MIN_SEPARATION_SQUARED = 0.99f
        const val SIDEWAYS_REACH = 5f
        const val EPSILON = 1.0E-4f
    }
}

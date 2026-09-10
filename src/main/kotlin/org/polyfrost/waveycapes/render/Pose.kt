package org.polyfrost.waveycapes.render

import kotlin.math.cos
import kotlin.math.sin

class Pose {
    private val m = FloatArray(12)

    fun identity() {
        m.fill(0f)
        m[0] = 1f
        m[5] = 1f
        m[10] = 1f
    }

    fun translate(x: Float, y: Float, z: Float) {
        m[3] += m[0] * x + m[1] * y + m[2] * z
        m[7] += m[4] * x + m[5] * y + m[6] * z
        m[11] += m[8] * x + m[9] * y + m[10] * z
    }

    fun rotateX(degrees: Float) {
        val cos = cos(degrees * RADIANS_PER_DEGREE)
        val sin = sin(degrees * RADIANS_PER_DEGREE)
        for (row in 0..2) {
            val i = row * 4
            val a = m[i + 1]
            val b = m[i + 2]
            m[i + 1] = a * cos + b * sin
            m[i + 2] = b * cos - a * sin
        }
    }

    fun rotateY(degrees: Float) {
        val cos = cos(degrees * RADIANS_PER_DEGREE)
        val sin = sin(degrees * RADIANS_PER_DEGREE)
        for (row in 0..2) {
            val i = row * 4
            val a = m[i]
            val b = m[i + 2]
            m[i] = a * cos - b * sin
            m[i + 2] = a * sin + b * cos
        }
    }

    fun rotateZ(degrees: Float) {
        val cos = cos(degrees * RADIANS_PER_DEGREE)
        val sin = sin(degrees * RADIANS_PER_DEGREE)
        for (row in 0..2) {
            val i = row * 4
            val a = m[i]
            val b = m[i + 1]
            m[i] = a * cos + b * sin
            m[i + 1] = b * cos - a * sin
        }
    }

    fun x(px: Float, py: Float, pz: Float): Float = m[0] * px + m[1] * py + m[2] * pz + m[3]

    fun y(px: Float, py: Float, pz: Float): Float = m[4] * px + m[5] * py + m[6] * pz + m[7]

    fun z(px: Float, py: Float, pz: Float): Float = m[8] * px + m[9] * py + m[10] * pz + m[11]

    private companion object {
        const val RADIANS_PER_DEGREE = (Math.PI / 180.0).toFloat()
    }
}

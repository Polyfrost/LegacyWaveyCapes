package org.polyfrost.waveycapes.render

import net.minecraft.client.render.BufferBuilder
import net.minecraft.client.render.Tessellator
import net.minecraft.client.render.VertexFormats
import org.lwjgl.opengl.GL11
import org.polyfrost.waveycapes.WaveyCapes.CAPE_PARTS
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

class CapeMesh {
    private val vertices = FloatArray(4 * VERTEX_SIZE)

    private val frontNormals = FloatArray(CAPE_PARTS * 3)
    private val backNormals = FloatArray(CAPE_PARTS * 3)

    fun draw(poses: Array<Pose>, smooth: Boolean) {
        if (smooth) collectSeamNormals(poses)

        val tessellator = Tessellator.getInstance()
        val builder = tessellator.buffer
        builder.begin(GL11.GL_QUADS, VertexFormats.POSITION_TEXTURE_NORMAL)

        for (part in 0 until CAPE_PARTS) {
            val top = if (smooth) poses[max(part - 1, 0)] else poses[part]
            val bottom = poses[part]

            west(top, bottom, part)
            flatNormal()
            emit(builder)

            east(top, bottom, part)
            flatNormal()
            emit(builder)

            front(top, bottom, part)
            if (smooth) blendNormals(frontNormals, part) else flatNormal()
            emit(builder)

            back(top, bottom, part)
            if (smooth) blendNormals(backNormals, part) else flatNormal()
            emit(builder)

            if (!smooth) {
                topCap(bottom, part, part)
                flatNormal()
                emit(builder)

                bottomCap(bottom, part, part)
                flatNormal()
                emit(builder)
            }
        }

        if (smooth) {
            topCap(poses[0], 0, 0)
            flatNormal()
            emit(builder)

            bottomCap(poses[CAPE_PARTS - 1], CAPE_PARTS - 1, 0)
            flatNormal()
            emit(builder)
        }

        tessellator.draw()
    }

    private fun west(top: Pose, bottom: Pose, part: Int) {
        val yTop = part * PART_HEIGHT
        val yBottom = yTop + PART_HEIGHT
        val vTop = sideV(part)
        val vBottom = vTop + PIXEL_V
        corner(0, top, -HALF_WIDTH, yTop, -DEPTH, 1 * PIXEL_U, vTop)
        corner(1, top, -HALF_WIDTH, yTop, 0f, 0 * PIXEL_U, vTop)
        corner(2, bottom, -HALF_WIDTH, yBottom, 0f, 0 * PIXEL_U, vBottom)
        corner(3, bottom, -HALF_WIDTH, yBottom, -DEPTH, 1 * PIXEL_U, vBottom)
    }

    private fun east(top: Pose, bottom: Pose, part: Int) {
        val yTop = part * PART_HEIGHT
        val yBottom = yTop + PART_HEIGHT
        val vTop = sideV(part)
        val vBottom = vTop + PIXEL_V
        corner(0, top, HALF_WIDTH, yTop, 0f, 12 * PIXEL_U, vTop)
        corner(1, top, HALF_WIDTH, yTop, -DEPTH, 11 * PIXEL_U, vTop)
        corner(2, bottom, HALF_WIDTH, yBottom, -DEPTH, 11 * PIXEL_U, vBottom)
        corner(3, bottom, HALF_WIDTH, yBottom, 0f, 12 * PIXEL_U, vBottom)
    }

    private fun front(top: Pose, bottom: Pose, part: Int) {
        val yTop = part * PART_HEIGHT
        val yBottom = yTop + PART_HEIGHT
        val vTop = sideV(part)
        val vBottom = vTop + PIXEL_V
        corner(0, top, HALF_WIDTH, yTop, -DEPTH, 11 * PIXEL_U, vTop)
        corner(1, top, -HALF_WIDTH, yTop, -DEPTH, 1 * PIXEL_U, vTop)
        corner(2, bottom, -HALF_WIDTH, yBottom, -DEPTH, 1 * PIXEL_U, vBottom)
        corner(3, bottom, HALF_WIDTH, yBottom, -DEPTH, 11 * PIXEL_U, vBottom)
    }

    private fun back(top: Pose, bottom: Pose, part: Int) {
        val yTop = part * PART_HEIGHT
        val yBottom = yTop + PART_HEIGHT
        val vTop = sideV(part)
        val vBottom = vTop + PIXEL_V
        corner(0, top, -HALF_WIDTH, yTop, 0f, 22 * PIXEL_U, vTop)
        corner(1, top, HALF_WIDTH, yTop, 0f, 12 * PIXEL_U, vTop)
        corner(2, bottom, HALF_WIDTH, yBottom, 0f, 12 * PIXEL_U, vBottom)
        corner(3, bottom, -HALF_WIDTH, yBottom, 0f, 22 * PIXEL_U, vBottom)
    }

    private fun topCap(pose: Pose, part: Int, textureRow: Int) {
        val y = part * PART_HEIGHT
        val vMin = textureRow * PIXEL_V
        val vMax = vMin + PIXEL_V
        corner(0, pose, HALF_WIDTH, y, 0f, 11 * PIXEL_U, vMin)
        corner(1, pose, -HALF_WIDTH, y, 0f, 1 * PIXEL_U, vMin)
        corner(2, pose, -HALF_WIDTH, y, -DEPTH, 1 * PIXEL_U, vMax)
        corner(3, pose, HALF_WIDTH, y, -DEPTH, 11 * PIXEL_U, vMax)
    }

    private fun bottomCap(pose: Pose, part: Int, textureRow: Int) {
        val y = (part + 1) * PART_HEIGHT
        val vMin = textureRow * PIXEL_V
        val vMax = vMin + PIXEL_V
        corner(0, pose, HALF_WIDTH, y, -DEPTH, 21 * PIXEL_U, vMin)
        corner(1, pose, -HALF_WIDTH, y, -DEPTH, 11 * PIXEL_U, vMin)
        corner(2, pose, -HALF_WIDTH, y, 0f, 11 * PIXEL_U, vMax)
        corner(3, pose, HALF_WIDTH, y, 0f, 21 * PIXEL_U, vMax)
    }

    private fun sideV(part: Int) = (part + 1) * PIXEL_V

    private fun collectSeamNormals(poses: Array<Pose>) {
        for (part in 0 until CAPE_PARTS) {
            val top = poses[max(part - 1, 0)]
            val bottom = poses[part]

            front(top, bottom, part)
            storeNormal(frontNormals, part)

            back(top, bottom, part)
            storeNormal(backNormals, part)
        }
    }

    private fun storeNormal(normals: FloatArray, part: Int) {
        flatNormal()
        val offset = part * 3
        normals[offset] = vertices[NORMAL]
        normals[offset + 1] = vertices[NORMAL + 1]
        normals[offset + 2] = vertices[NORMAL + 2]
    }

    private fun blendNormals(normals: FloatArray, part: Int) {
        average(normals, part, max(part - 1, 0), 0, 1)
        average(normals, part, min(part + 1, CAPE_PARTS - 1), 2, 3)
    }

    private fun average(normals: FloatArray, a: Int, b: Int, first: Int, second: Int) {
        var x = normals[a * 3] + normals[b * 3]
        var y = normals[a * 3 + 1] + normals[b * 3 + 1]
        var z = normals[a * 3 + 2] + normals[b * 3 + 2]
        val length = sqrt(x * x + y * y + z * z)
        if (length > 0f) {
            x /= length
            y /= length
            z /= length
        }
        setNormal(first, x, y, z)
        setNormal(second, x, y, z)
    }

    private fun flatNormal() {
        val ax = vertices[VERTEX_SIZE * 2] - vertices[VERTEX_SIZE]
        val ay = vertices[VERTEX_SIZE * 2 + 1] - vertices[VERTEX_SIZE + 1]
        val az = vertices[VERTEX_SIZE * 2 + 2] - vertices[VERTEX_SIZE + 2]
        val bx = vertices[0] - vertices[VERTEX_SIZE]
        val by = vertices[1] - vertices[VERTEX_SIZE + 1]
        val bz = vertices[2] - vertices[VERTEX_SIZE + 2]

        var x = ay * bz - az * by
        var y = az * bx - ax * bz
        var z = ax * by - ay * bx
        val length = sqrt(x * x + y * y + z * z)
        if (length > 0f) {
            x /= length
            y /= length
            z /= length
        }

        for (vertex in 0..3) {
            setNormal(vertex, x, y, z)
        }
    }

    private fun corner(index: Int, pose: Pose, x: Float, y: Float, z: Float, u: Float, v: Float) {
        val offset = index * VERTEX_SIZE
        vertices[offset] = pose.x(x, y, z)
        vertices[offset + 1] = pose.y(x, y, z)
        vertices[offset + 2] = pose.z(x, y, z)
        vertices[offset + 3] = u
        vertices[offset + 4] = v
    }

    private fun setNormal(index: Int, x: Float, y: Float, z: Float) {
        val offset = index * VERTEX_SIZE + NORMAL
        vertices[offset] = x
        vertices[offset + 1] = y
        vertices[offset + 2] = z
    }

    private fun emit(builder: BufferBuilder) {
        for (vertex in 0..3) {
            val offset = vertex * VERTEX_SIZE
            builder
                .vertex(
                    vertices[offset].toDouble(),
                    vertices[offset + 1].toDouble(),
                    vertices[offset + 2].toDouble(),
                )
                .texture(vertices[offset + 3].toDouble(), vertices[offset + 4].toDouble())
                .normal(vertices[offset + 5], vertices[offset + 6], vertices[offset + 7])
                .next()
        }
    }

    private companion object {
        const val VERTEX_SIZE = 8
        const val NORMAL = 5

        const val HALF_WIDTH = 5f / 16f
        const val DEPTH = 1f / 16f
        const val PART_HEIGHT = 1f / CAPE_PARTS
        const val PIXEL_U = 1f / 64f
        const val PIXEL_V = 1f / 32f
    }
}

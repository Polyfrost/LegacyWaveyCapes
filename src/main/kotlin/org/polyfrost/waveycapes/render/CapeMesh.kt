package org.polyfrost.waveycapes.render

import net.minecraft.client.render.BufferBuilder
import net.minecraft.client.render.Tessellator
import net.minecraft.client.render.VertexFormats
import org.lwjgl.opengl.GL11
import org.polyfrost.waveycapes.WaveyCapes.CAPE_PARTS
import kotlin.math.max
import kotlin.math.sqrt

class CapeMesh {
    private val vertices = FloatArray(4 * VERTEX_SIZE)

    fun draw(poses: Array<Pose>, smooth: Boolean) {
        val tessellator = Tessellator.getInstance()
        val builder = tessellator.buffer
        builder.begin(GL11.GL_QUADS, VertexFormats.POSITION_TEXTURE_NORMAL)

        for (part in 0 until CAPE_PARTS) {
            val top = if (smooth) poses[max(part - 1, 0)] else poses[part]
            val bottom = poses[part]

            west(top, bottom, part)
            emit(builder)

            east(top, bottom, part)
            emit(builder)

            front(top, bottom, part)
            emit(builder)

            back(top, bottom, part)
            emit(builder)

            if (!smooth) {
                topCap(bottom, part, part)
                emit(builder)

                bottomCap(bottom, part, part)
                emit(builder)
            }
        }

        if (smooth) {
            topCap(poses[0], 0, 0)
            emit(builder)

            bottomCap(poses[CAPE_PARTS - 1], CAPE_PARTS - 1, 0)
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

    private fun corner(index: Int, pose: Pose, x: Float, y: Float, z: Float, u: Float, v: Float) {
        val offset = index * VERTEX_SIZE
        vertices[offset] = pose.x(x, y, z)
        vertices[offset + 1] = pose.y(x, y, z)
        vertices[offset + 2] = pose.z(x, y, z)
        vertices[offset + 3] = u
        vertices[offset + 4] = v
    }

    private fun emit(builder: BufferBuilder) {
        val ax = vertices[0] - vertices[VERTEX_SIZE]
        val ay = vertices[1] - vertices[VERTEX_SIZE + 1]
        val az = vertices[2] - vertices[VERTEX_SIZE + 2]
        val bx = vertices[2 * VERTEX_SIZE] - vertices[VERTEX_SIZE]
        val by = vertices[2 * VERTEX_SIZE + 1] - vertices[VERTEX_SIZE + 1]
        val bz = vertices[2 * VERTEX_SIZE + 2] - vertices[VERTEX_SIZE + 2]
        var nx = by * az - bz * ay
        var ny = bz * ax - bx * az
        var nz = bx * ay - by * ax
        val length = sqrt(nx * nx + ny * ny + nz * nz)
        if (length > 0f) {
            nx /= length
            ny /= length
            nz /= length
        }

        for (vertex in 0..3) {
            val offset = vertex * VERTEX_SIZE
            builder
                .vertex(
                    vertices[offset].toDouble(),
                    vertices[offset + 1].toDouble(),
                    vertices[offset + 2].toDouble(),
                )
                .texture(vertices[offset + 3].toDouble(), vertices[offset + 4].toDouble())
                .normal(nx, ny, nz)
                .next()
        }
    }

    private companion object {
        const val VERTEX_SIZE = 5

        const val HALF_WIDTH = 5f / 16f
        const val DEPTH = 1f / 16f
        const val PART_HEIGHT = 1f / CAPE_PARTS
        const val PIXEL_U = 1f / 64f
        const val PIXEL_V = 1f / 32f
    }
}

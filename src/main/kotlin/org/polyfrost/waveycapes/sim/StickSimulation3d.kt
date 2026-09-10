package org.polyfrost.waveycapes.sim

import org.polyfrost.waveycapes.CapeMovement
import org.polyfrost.waveycapes.WaveyCapes.CAPE_PARTS

class StickSimulation3d : CapeSimulation(CapeMovement.BASIC_SIMULATION_3D, ITERATIONS) {
    init {
        for (i in 0 until CAPE_PARTS) {
            x[i] = -i.toFloat()
        }
    }

    override fun simulate() {
        applyGravity()
        preventClipping()
        preventSelfClipping(MAX_FIXES)
        applyMotion()
        preventSelfClipping(MAX_FIXES)
        preventHardBends()
        limitLength()
    }

    private fun preventHardBends() {
        for (i in 1 until CAPE_PARTS - 2) {
            preventHardBend(i, MAX_BEND)
        }
    }

    private companion object {
        const val ITERATIONS = 3
        const val MAX_BEND = 20.0
        const val MAX_FIXES = 32
    }
}

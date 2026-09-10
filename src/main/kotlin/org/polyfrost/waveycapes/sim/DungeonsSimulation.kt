package org.polyfrost.waveycapes.sim

import org.polyfrost.waveycapes.CapeMovement
import org.polyfrost.waveycapes.WaveyCapes.CAPE_PARTS

class DungeonsSimulation : CapeSimulation(CapeMovement.DUNGEONS, ITERATIONS) {
    override fun simulate() {
        applyGravity()
        preventClipping()
        applyMotion()
        preventHardBends()
        preventSelfClipping(MAX_FIXES)
        limitLength()
    }

    private fun preventHardBends() {
        for (i in 1 until CAPE_PARTS - 1) {
            preventHardBend(i, if (i == CAPE_PARTS / 2) MAX_BEND else 0.0)
        }
    }

    private companion object {
        const val ITERATIONS = 30
        const val MAX_BEND = 20.0
        const val MAX_FIXES = 10
    }
}

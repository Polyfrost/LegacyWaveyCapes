package org.polyfrost.waveycapes

import org.polyfrost.waveycapes.sim.CapeSimulation
import org.polyfrost.waveycapes.sim.DungeonsSimulation
import org.polyfrost.waveycapes.sim.StickSimulation
import org.polyfrost.waveycapes.sim.StickSimulation3d

enum class CapeMovement {
    VANILLA,
    BASIC_SIMULATION,
    BASIC_SIMULATION_3D,
    DUNGEONS;

    fun createSimulation(): CapeSimulation? = when (this) {
        VANILLA -> null
        BASIC_SIMULATION -> StickSimulation()
        BASIC_SIMULATION_3D -> StickSimulation3d()
        DUNGEONS -> DungeonsSimulation()
    }
}

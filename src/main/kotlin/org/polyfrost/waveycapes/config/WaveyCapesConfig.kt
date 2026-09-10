package org.polyfrost.waveycapes.config

import net.ornithemc.osl.config.api.ConfigScope
import net.ornithemc.osl.config.api.LoadingPhase
import net.ornithemc.osl.config.api.config.BaseConfig
import net.ornithemc.osl.config.api.config.option.IntegerOption
import net.ornithemc.osl.config.api.config.option.Option
import net.ornithemc.osl.config.api.serdes.FileSerializerType
import org.polyfrost.waveycapes.CapeMovement
import org.polyfrost.waveycapes.CapeStyle
import org.polyfrost.waveycapes.WindMode

object WaveyCapesConfig : BaseConfig() {

    private const val APPEARANCE = "Appearance"
    private const val MOVEMENT = "Movement"

    val capeStyle = enumOption("Cape Style", "Whether the cape bends as one surface or steps between parts.", CapeStyle.SMOOTH)
    val windMode = enumOption("Wind", "Adds a wave travelling down the cape.", WindMode.NONE)

    val capeMovement = enumOption("Cape Movement", "The physics used to swing the cape.", CapeMovement.BASIC_SIMULATION_3D)
    val gravity = IntegerOption("Gravity", "How strongly the cape is pulled down.", 25)
    val heightMultiplier = IntegerOption("Height Multiplier", "How much vertical movement lifts the cape.", 6)
    val straveMultiplier = IntegerOption("Strafe Multiplier", "How much sideways movement swings the cape.", 2)

    val legacyEntries: List<Pair<String, Option>> = listOf(
        "windMode" to windMode,
        "capeStyle" to capeStyle,
        "capeMovement" to capeMovement,
        "gravity" to gravity,
        "heightMultiplier" to heightMultiplier,
        "straveMultiplier" to straveMultiplier,
    )

    /** Applies upstream's config upgrades to values just read from an older file. */
    fun upgrade(fileVersion: Int) {
        if (fileVersion < 2 && gravity.get() < 0) {
            gravity.set(-gravity.get())
        }
    }

    override fun getNamespace(): String? = null

    override fun getName(): String = "Legacy WaveyCapes"

    override fun getSaveName(): String = "waveycapes.json"

    override fun getScope(): ConfigScope = ConfigScope.GLOBAL

    override fun getLoadingPhase(): LoadingPhase = LoadingPhase.START

    override fun getType(): FileSerializerType<*> = LegacySerializers.FLAT_JSON

    override fun getVersion(): Int = 2

    override fun init() {
        registerOptions(APPEARANCE, capeStyle, windMode)
        registerOptions(MOVEMENT, capeMovement, gravity, heightMultiplier, straveMultiplier)
    }
}

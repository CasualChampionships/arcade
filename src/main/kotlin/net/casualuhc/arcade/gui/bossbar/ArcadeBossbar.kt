package net.casualuhc.arcade.gui.bossbar

import net.casualuhc.arcade.gui.suppliers.*
import net.casualuhc.arcade.utils.BossbarUtils.bossbars
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.BossEvent

class ArcadeBossbar(
    private var title: ComponentSupplier,
    private var progress: ProgressSupplier,
    private var colour: ColourSupplier,
    private var overlay: OverlaySupplier
): CustomBossBar() {
    private var dark = BooleanSupplier.alwaysFalse()
    private var music = BooleanSupplier.alwaysFalse()
    private var fog = BooleanSupplier.alwaysFalse()

    fun setTitle(title: ComponentSupplier) {
        this.title = title

        for (player in this.getPlayers()) {
            player.bossbars.updateTitle(this)
        }
    }

    fun setProgress(progress: ProgressSupplier) {
        this.progress = progress

        for (player in this.getPlayers()) {
            player.bossbars.updateProgress(this)
        }
    }

    fun setStyle(
        colour: ColourSupplier? = null,
        overlay: OverlaySupplier? = null
    ) {
        if (colour != null) {
            this.colour = colour
        }
        if (overlay != null) {
            this.overlay = overlay
        }

        for (player in this.getPlayers()) {
            player.bossbars.updateStyle(this)
        }
    }

    fun setProperties(
        dark: BooleanSupplier? = null,
        music: BooleanSupplier? = null,
        fog: BooleanSupplier? = null
    ) {
        if (dark != null) {
            this.dark = dark
        }
        if (music != null) {
            this.music = music
        }
        if (fog != null) {
            this.fog = fog
        }

        for (player in this.getPlayers()) {
            player.bossbars.updateProperties(this)
        }
    }

    override fun getTitle(player: ServerPlayer): Component {
        return this.title.getComponent(player)
    }

    override fun getProgress(player: ServerPlayer): Float {
        return this.progress.getProgress(player)
    }

    override fun getColour(player: ServerPlayer): BossEvent.BossBarColor {
        return this.colour.getColour(player)
    }

    override fun getOverlay(player: ServerPlayer): BossEvent.BossBarOverlay {
        return this.overlay.getOverlay(player)
    }

    override fun isDark(player: ServerPlayer): Boolean {
        return this.dark.get(player)
    }

    override fun hasMusic(player: ServerPlayer): Boolean {
        return this.music.get(player)
    }

    override fun hasFog(player: ServerPlayer): Boolean {
        return this.fog.get(player)
    }
}
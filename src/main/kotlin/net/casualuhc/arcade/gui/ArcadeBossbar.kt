package net.casualuhc.arcade.gui

import net.casualuhc.arcade.gui.suppliers.*
import net.casualuhc.arcade.utils.BossbarUtils.bossbars
import net.minecraft.server.level.ServerPlayer
import net.minecraft.util.Mth
import java.util.*

class ArcadeBossbar(
    title: ComponentSupplier,
    progress: ProgressSupplier,
    colour: ColourSupplier,
    style: OverlaySupplier
) {
    private val players = HashSet<ServerPlayer>()
    val uuid: UUID = Mth.createInsecureUUID()

    var title = title
        private set
    var progress = progress
        private set
    var colour = colour
        private set
    var overlay = style
        private set
    var dark = BooleanSupplier.alwaysFalse()
        private set
    var music = BooleanSupplier.alwaysFalse()
        private set
    var fog = BooleanSupplier.alwaysFalse()
        private set

    var interval = 1
        private set

    fun setTitle(title: ComponentSupplier) {
        this.title = title

        for (player in this.players) {
            player.bossbars.updateTitle(this)
        }
    }

    fun setProgress(progress: ProgressSupplier) {
        this.progress = progress

        for (player in this.players) {
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

        for (player in this.players) {
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

        for (player in this.players) {
            player.bossbars.updateProperties(this)
        }
    }

    fun setInterval(interval: Int) {
        this.interval = interval.coerceAtLeast(1)
    }

    fun addPlayer(player: ServerPlayer) {
        if (this.players.add(player)) {
            player.bossbars.add(this)
        }
    }

    fun removePlayer(player: ServerPlayer) {
        if (this.players.remove(player)) {
            player.bossbars.remove(this)
        }
    }

    fun clearPlayers() {
        for (player in this.getPlayers()) {
            this.removePlayer(player)
        }
    }

    fun getPlayers(): List<ServerPlayer> {
        return LinkedList(this.players)
    }
}
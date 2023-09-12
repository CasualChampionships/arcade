package net.casual.arcade.gui.bossbar

import net.casual.arcade.gui.suppliers.*
import net.casual.arcade.utils.BossbarUtils.bossbars
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.BossEvent.BossBarColor
import net.minecraft.world.BossEvent.BossBarOverlay

public class ArcadeBossbar(
    private var title: ComponentSupplier,
    private var progress: ProgressSupplier,
    private var colour: ColourSupplier,
    private var overlay: OverlaySupplier
): CustomBossBar() {
    private var dark = BooleanSupplier.alwaysFalse()
    private var music = BooleanSupplier.alwaysFalse()
    private var fog = BooleanSupplier.alwaysFalse()

    public fun setTitle(title: ComponentSupplier) {
        this.title = title

        for (player in this.getPlayers()) {
            player.bossbars.updateTitle(this)
        }
    }

    public fun setProgress(progress: ProgressSupplier) {
        this.progress = progress

        for (player in this.getPlayers()) {
            player.bossbars.updateProgress(this)
        }
    }

    public fun setStyle(
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

    public fun setProperties(
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

    /**
     * This gets the title of the [CustomBossBar] which will be
     * displayed to the given [player].
     *
     * @param player The player being displayed the component.
     * @return The [Component] to display as the title of the [CustomBossBar].
     */
    override fun getTitle(player: ServerPlayer): Component {
        return this.title.getComponent(player)
    }

    /**
     * This gets the progress of the [CustomBossBar] which will be
     * displayed to the given [player].
     *
     * @param player The player being displayed the progress.
     * @return The progress to display the bossbar as having.
     */
    override fun getProgress(player: ServerPlayer): Float {
        return this.progress.getProgress(player)
    }

    /**
     * This gets the colour of the [CustomBossBar] which will be
     * displayed to the given [player].
     *
     * @param player The player being displayed the colour.
     * @return The [BossBarColor] to set the bossbar to.
     */
    override fun getColour(player: ServerPlayer): BossBarColor {
        return this.colour.getColour(player)
    }

    /**
     * This gets the overlay of the [CustomBossBar] which will be
     * displayed to the given [player].
     *
     * @param player The player being displayed the overlay.
     * @return The [BossBarOverlay] to set the bossbar to.
     */
    override fun getOverlay(player: ServerPlayer): BossBarOverlay {
        return this.overlay.getOverlay(player)
    }

    /**
     * This sets whether the player's world is dark or not.
     *
     * @param player The player being displayed the bar.
     * @return Whether the player's world should be dark.
     */
    override fun isDark(player: ServerPlayer): Boolean {
        return this.dark.get(player)
    }

    /**
     * This sets whether the player's should be played boss
     * music in the end dimension.
     *
     * @param player The player being displayed the bar.
     * @return Whether the player should play boss music.
     */
    override fun hasMusic(player: ServerPlayer): Boolean {
        return this.music.get(player)
    }

    /**
     * This sets whether the player's world has fog or not.
     *
     * @param player The player being displayed the bar.
     * @return Whether the player's world should have fog.
     */
    override fun hasFog(player: ServerPlayer): Boolean {
        return this.fog.get(player)
    }
}
package net.casual.arcade.visuals.bossbar

import net.casual.arcade.visuals.core.TickableUI
import net.casual.arcade.visuals.elements.*
import net.casual.arcade.visuals.extensions.PlayerBossbarsExtension.Companion.bossbars
import net.minecraft.network.chat.Component
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.BossEvent.BossBarColor
import net.minecraft.world.BossEvent.BossBarOverlay

public class SuppliedBossbar(
    private var title: PlayerSpecificElement<Component> = ComponentElements.empty(),
    private var progress: PlayerSpecificElement<Float> = UniversalElement.constant(0.0F),
    private var colour: PlayerSpecificElement<BossBarColor> = BossbarColorElements.white(),
    private var overlay: PlayerSpecificElement<BossBarOverlay> = BossbarOverlayElements.progress()
): CustomBossbar(), TickableUI {
    private var dark = BooleanElements.alwaysFalse()
    private var music = BooleanElements.alwaysFalse()
    private var fog = BooleanElements.alwaysFalse()

    public fun setTitle(title: PlayerSpecificElement<Component>) {
        this.title = title

        for (player in this.getPlayers()) {
            player.bossbars.updateTitle(this)
        }
    }

    public fun setProgress(progress: PlayerSpecificElement<Float>) {
        this.progress = progress

        for (player in this.getPlayers()) {
            player.bossbars.updateProgress(this)
        }
    }

    public fun setStyle(
        colour: PlayerSpecificElement<BossBarColor>? = null,
        overlay: PlayerSpecificElement<BossBarOverlay>? = null
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
        dark: PlayerSpecificElement<Boolean>? = null,
        music: PlayerSpecificElement<Boolean>? = null,
        fog: PlayerSpecificElement<Boolean>? = null
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
     * This gets the title of the [CustomBossbar] which will be
     * displayed to the given [player].
     *
     * @param player The player being displayed the component.
     * @return The [Component] to display as the title of the [CustomBossbar].
     */
    override fun getTitle(player: ServerPlayer): Component {
        return this.title.get(player)
    }

    /**
     * This gets the progress of the [CustomBossbar] which will be
     * displayed to the given [player].
     *
     * @param player The player being displayed the progress.
     * @return The progress to display the bossbar as having.
     */
    override fun getProgress(player: ServerPlayer): Float {
        return this.progress.get(player)
    }

    /**
     * This gets the colour of the [CustomBossbar] which will be
     * displayed to the given [player].
     *
     * @param player The player being displayed the colour.
     * @return The [BossBarColor] to set the bossbar to.
     */
    override fun getColour(player: ServerPlayer): BossBarColor {
        return this.colour.get(player)
    }

    /**
     * This gets the overlay of the [CustomBossbar] which will be
     * displayed to the given [player].
     *
     * @param player The player being displayed the overlay.
     * @return The [BossBarOverlay] to set the bossbar to.
     */
    override fun getOverlay(player: ServerPlayer): BossBarOverlay {
        return this.overlay.get(player)
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

    override fun tick(server: MinecraftServer) {
        this.title.tick(server)
        this.progress.tick(server)
        this.colour.tick(server)
        this.overlay.tick(server)
        this.dark.tick(server)
        this.music.tick(server)
        this.fog.tick(server)
    }
}
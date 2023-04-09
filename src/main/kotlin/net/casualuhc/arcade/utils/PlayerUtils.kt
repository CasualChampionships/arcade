package net.casualuhc.arcade.utils

import net.casualuhc.arcade.Arcade
import net.casualuhc.arcade.extensions.Extension
import net.casualuhc.arcade.extensions.ExtensionHolder
import net.casualuhc.arcade.utils.ExtensionUtils.addExtension
import net.casualuhc.arcade.utils.ExtensionUtils.getExtension
import net.casualuhc.arcade.utils.ExtensionUtils.getExtensions
import net.minecraft.advancements.Advancement
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.GameType
import net.minecraft.world.phys.Vec2
import java.util.function.Consumer

@Suppress("unused")
object PlayerUtils {
    @JvmStatic
    fun players(): Collection<ServerPlayer> {
        return Arcade.server.playerList.players
    }

    @JvmStatic
    fun forEveryPlayer(consumer: Consumer<ServerPlayer>) {
        for (player in this.players()) {
            consumer.accept(player)
        }
    }

    @JvmStatic
    fun spread(
        level: ServerLevel,
        center: Vec2,
        distance: Double,
        range: Double,
        teams: Boolean,
        targets: Collection<ServerPlayer>
    ) {
        SpreadPlayers.run(level, center, distance, range, level.maxBuildHeight, teams, targets)
    }

    @JvmStatic
    fun ServerPlayer.clearPlayerInventory() {
        this.inventory.clearContent()
        this.inventoryMenu.clearCraftingContent()
        this.inventoryMenu.carried = ItemStack.EMPTY
    }

    @JvmStatic
    fun ServerPlayer.isSurvival(): Boolean {
        return this.isGameMode(GameType.SURVIVAL)
    }

    @JvmStatic
    fun ServerPlayer.isGameMode(mode: GameType): Boolean {
        return this.gameMode.gameModeForPlayer == mode
    }

    @JvmStatic
    fun ServerPlayer.grantAdvancement(advancement: Advancement) {
        val progress = this.advancements.getOrStartProgress(advancement)
        if (!progress.isDone) {
            for (string in progress.remainingCriteria) {
                this.advancements.award(advancement, string)
            }
        }
    }

    @JvmStatic
    fun ServerPlayer.revokeAdvancement(advancement: Advancement) {
        val progress = this.advancements.getOrStartProgress(advancement)
        if (!progress.isDone) {
            for (string in progress.completedCriteria) {
                this.advancements.revoke(advancement, string)
            }
        }
    }

    @JvmStatic
    fun ServerPlayer.addExtension(extension: Extension) {
        (this as ExtensionHolder).addExtension(extension)
    }

    @JvmStatic
    fun <T: Extension> ServerPlayer.getExtension(type: Class<T>): T? {
        return (this as ExtensionHolder).getExtension(type)
    }

    @JvmStatic
    fun ServerPlayer.getExtensions(): Collection<Extension> {
        return (this as ExtensionHolder).getExtensions()
    }
}
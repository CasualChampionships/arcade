package net.casualuhc.arcade.utils

import net.casualuhc.arcade.Arcade
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
    fun clearPlayerInventory(player: ServerPlayer) {
        player.inventory.clearContent()
        player.inventoryMenu.clearCraftingContent()
        player.inventoryMenu.carried = ItemStack.EMPTY
    }

    @JvmStatic
    fun spread(level: ServerLevel, center: Vec2, distance: Double, range: Double, teams: Boolean, targets: Collection<ServerPlayer>) {
        SpreadPlayers.run(level, center, distance, range, level.maxBuildHeight, teams, targets)
    }

    @JvmStatic
    fun ServerPlayer.isGameMode(mode: GameType): Boolean {
        return this.gameMode.gameModeForPlayer == mode
    }
}
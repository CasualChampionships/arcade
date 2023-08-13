package net.casualuhc.arcade.utils

import net.casualuhc.arcade.Arcade
import net.casualuhc.arcade.border.LevelSpecificBorderBroadcaster
import net.casualuhc.arcade.events.EventHandler
import net.casualuhc.arcade.events.GlobalEventHandler
import net.casualuhc.arcade.events.player.PlayerDimensionChangeEvent
import net.minecraft.network.protocol.game.ClientboundInitializeBorderPacket
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.border.BorderChangeListener
import net.minecraft.world.level.border.BorderChangeListener.DelegateBorderChangeListener

object BorderUtils {
    private val handler = EventHandler()
    private var synced = true

    private val original = HashMap<ServerLevel, BorderChangeListener>()
    private val replacement = HashMap<ServerLevel, BorderChangeListener>()

    init {
        this.handler.register<PlayerDimensionChangeEvent> { (player, destination) ->
            player.connection.send(ClientboundInitializeBorderPacket(destination.worldBorder))
        }
    }

    fun joinWorldBorders() {
        if (this.synced) {
            return
        }
        this.synced = true

        for ((level, listener) in this.replacement) {
            level.worldBorder.removeListener(listener)
        }

        GlobalEventHandler.removeHandler(this.handler)
        val border = LevelUtils.overworld().worldBorder
        for (level in Arcade.server.allLevels) {
            border.addListener(this.original.getOrPut(level) { DelegateBorderChangeListener(level.worldBorder) })
        }
    }

    fun isolateWorldBorders() {
        if (!this.synced) {
            return
        }
        this.synced = false

        val border = LevelUtils.overworld().worldBorder
        for (listener in this.original.values) {
            border.removeListener(listener)
        }

        GlobalEventHandler.addHandler(this.handler)
        for (level in Arcade.server.allLevels) {
            val broadcaster = this.replacement.getOrPut(level) { LevelSpecificBorderBroadcaster(level) }
            level.worldBorder.addListener(broadcaster)
        }
    }

    @JvmStatic
    fun addOriginalListener(level: ServerLevel, listener: BorderChangeListener) {
        this.original[level] = listener
    }
}
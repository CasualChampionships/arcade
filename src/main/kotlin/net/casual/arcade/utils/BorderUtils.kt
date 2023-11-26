package net.casual.arcade.utils

import net.casual.arcade.border.LevelSpecificBorderBroadcaster
import net.casual.arcade.border.extensions.BorderSerializerExtension
import net.casual.arcade.config.ArcadeConfig
import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.level.LevelCreatedEvent
import net.casual.arcade.events.server.ServerLoadedEvent
import net.casual.arcade.utils.LevelUtils.addExtension
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.border.BorderChangeListener
import net.minecraft.world.level.border.BorderChangeListener.DelegateBorderChangeListener
import org.jetbrains.annotations.ApiStatus.Internal

public object BorderUtils {
    private val original = HashMap<ServerLevel, BorderChangeListener>()
    private val replacement = HashMap<ServerLevel, BorderChangeListener>()

    private var currentlySynced = true

    @JvmStatic
    public var synced: Boolean by ArcadeConfig.boolean("border_synced", false)
        private set

    @JvmStatic
    public fun joinWorldBorders(): Boolean {
        if (this.currentlySynced) {
            return false
        }
        this.currentlySynced = true
        this.synced = true

        for ((level, listener) in this.replacement) {
            level.worldBorder.removeListener(listener)
        }

        val border = LevelUtils.overworld().worldBorder
        for (level in LevelUtils.levels()) {
            border.addListener(this.original.getOrPut(level) { DelegateBorderChangeListener(level.worldBorder) })
        }
        return true
    }

    @JvmStatic
    public fun isolateWorldBorders(): Boolean {
        if (!this.currentlySynced) {
            return false
        }
        this.currentlySynced = false
        this.synced = false

        val border = LevelUtils.overworld().worldBorder
        for (listener in this.original.values) {
            border.removeListener(listener)
        }

        for (level in LevelUtils.levels()) {
            val broadcaster = this.replacement.getOrPut(level) { LevelSpecificBorderBroadcaster(level) }
            level.worldBorder.addListener(broadcaster)
        }
        return true
    }

    @Internal
    @JvmStatic
    public fun addOriginalListener(level: ServerLevel, listener: BorderChangeListener) {
        this.original[level] = listener
    }

    internal fun registerEvents() {
        GlobalEventHandler.register<LevelCreatedEvent> { (level) ->
            level.addExtension(BorderSerializerExtension(level))
        }
        GlobalEventHandler.register<ServerLoadedEvent> {
            if (this.synced) {
                this.isolateWorldBorders()
            }
        }
    }
}
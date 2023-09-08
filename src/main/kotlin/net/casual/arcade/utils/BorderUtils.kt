package net.casual.arcade.utils

import net.casual.arcade.border.LevelSpecificBorderBroadcaster
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.border.BorderChangeListener
import net.minecraft.world.level.border.BorderChangeListener.DelegateBorderChangeListener

object BorderUtils {
    @JvmStatic
    var synced = true
        private set

    private val original = HashMap<ServerLevel, BorderChangeListener>()
    private val replacement = HashMap<ServerLevel, BorderChangeListener>()

    @JvmStatic
    fun joinWorldBorders() {
        if (this.synced) {
            return
        }
        this.synced = true

        for ((level, listener) in this.replacement) {
            level.worldBorder.removeListener(listener)
        }

        val border = LevelUtils.overworld().worldBorder
        for (level in LevelUtils.levels()) {
            border.addListener(this.original.getOrPut(level) { DelegateBorderChangeListener(level.worldBorder) })
        }
    }

    @JvmStatic
    fun isolateWorldBorders() {
        if (!this.synced) {
            return
        }
        this.synced = false

        val border = LevelUtils.overworld().worldBorder
        for (listener in this.original.values) {
            border.removeListener(listener)
        }

        for (level in LevelUtils.levels()) {
            val broadcaster = this.replacement.getOrPut(level) { LevelSpecificBorderBroadcaster(level) }
            level.worldBorder.addListener(broadcaster)
        }
    }

    @JvmStatic
    internal fun addOriginalListener(level: ServerLevel, listener: BorderChangeListener) {
        this.original[level] = listener
    }
}
/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.utils.component.event

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import net.casual.arcade.utils.time.MinecraftTimeDuration
import net.minecraft.nbt.Tag
import net.minecraft.resources.Identifier
import net.minecraft.server.level.ServerPlayer
import org.jetbrains.annotations.ApiStatus.Internal

public object CustomClickEventRegistry {
    private val registered = HashMap<Identifier, ClickEventCallback>()
    private val deletion = Int2ObjectOpenHashMap<ArrayList<Identifier>>()

    private var ticks = 0

    public fun register(identifier: Identifier, callback: ClickEventCallback) {
        this.registered[identifier] = callback
    }

    public fun registerTemporary(identifier: Identifier, timeout: MinecraftTimeDuration, callback: ClickEventCallback) {
        this.register(identifier, callback)
        this.deletion.getOrPut(this.ticks + timeout.ticks, ::ArrayList).add(identifier)
    }

    @Internal
    @JvmStatic
    public fun onServerTick() {
        val temps = this.deletion.remove(this.ticks++) ?: return
        for (command in temps) {
            this.registered.remove(command)
        }
    }

    @Internal
    @JvmStatic
    public fun onPlayerCustomClickAction(player: ServerPlayer, identifier: Identifier, payload: Tag?): Boolean {
        val callback = this.registered[identifier] ?: return false
        when (callback.click(player, payload)) {
            ClickEventCallback.Result.Consume -> this.registered.remove(identifier)
            ClickEventCallback.Result.Success -> { }
            ClickEventCallback.Result.Fail -> return false
        }
        return true
    }
}
/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.commands.hidden

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.ListenerRegistry.Companion.register
import net.casual.arcade.events.server.ServerTickEvent
import net.casual.arcade.events.server.player.PlayerCustomClickActionEvent
import net.casual.arcade.utils.ResourceUtils
import net.casual.arcade.utils.time.MinecraftTimeDuration
import net.minecraft.network.chat.ClickEvent
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import java.util.*

public object HiddenCommandManager {
    private val commands = Object2ObjectOpenHashMap<ResourceLocation, HiddenCommand>()

    private val deletion = Int2ObjectOpenHashMap<ArrayList<ResourceLocation>>()
    private var ticks = 0

    public fun register(timeout: MinecraftTimeDuration, command: HiddenCommand): ClickEvent.Custom {
        val id = ResourceUtils.random()
        this.commands[id] = command
        this.deletion.getOrPut(this.ticks + timeout.ticks, ::ArrayList).add(id)
        return ClickEvent.Custom(id, Optional.empty())
    }

    @Suppress("UNUSED_PARAMETER")
    private fun onServerTick(event: ServerTickEvent) {
        val commands = this.deletion.remove(this.ticks++) ?: return
        for (command in commands) {
            this.commands.remove(command)
        }
    }

    private fun onPlayerCustomClickAction(event: PlayerCustomClickActionEvent) {
        val (player, id) = event
        val command = this.commands[id] ?: return
        if (event.consume()) {
            val context = HiddenCommandContext(player)
            command.run(context)
            if (context.removed()) {
                this.commands.remove(id)
            }
        }
    }

    internal fun registerEvents() {
        GlobalEventHandler.Server.register<PlayerCustomClickActionEvent>(::onPlayerCustomClickAction)
        GlobalEventHandler.Server.register<ServerTickEvent>(
            phase = ServerTickEvent.PHASE_POST, listener = ::onServerTick
        )
    }
}
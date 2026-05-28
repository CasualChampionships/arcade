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
import net.casual.arcade.utils.IdentifierUtils
import net.casual.arcade.utils.time.MinecraftTimeDuration
import net.minecraft.network.chat.ClickEvent
import net.minecraft.resources.Identifier
import java.util.*

// TODO: This probably shouldn't be in this module anymore?
public object HiddenCommandManager {
    private val commands = Object2ObjectOpenHashMap<Identifier, HiddenCommand>()

    private val deletion = Int2ObjectOpenHashMap<ArrayList<Identifier>>()
    private var ticks = 0

    public fun register(timeout: MinecraftTimeDuration, command: HiddenCommand): ClickEvent.Custom {
        val id = IdentifierUtils.random()
        this.commands[id] = command
        this.deletion.getOrPut(this.ticks + timeout.ticks, ::ArrayList).add(id)
        return ClickEvent.Custom(id, Optional.empty())
    }

    private fun onServerTick(@Suppress("UnusedParameter") event: ServerTickEvent) {
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
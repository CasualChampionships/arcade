/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.guis.utils.dialog

import com.mojang.serialization.Decoder
import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.server.player.PlayerCustomClickActionEvent
import net.casual.arcade.events.utils.register
import net.casual.arcade.utils.ArcadeUtils
import net.casual.arcade.utils.player.username
import net.minecraft.core.RegistryAccess
import net.minecraft.nbt.NbtOps
import net.minecraft.nbt.Tag
import net.minecraft.resources.Identifier
import net.minecraft.resources.RegistryOps
import net.minecraft.server.level.ServerPlayer

public class DialogCustomActionRegistry(
    private val access: RegistryAccess = RegistryAccess.EMPTY
) {
    private val actions = HashMap<Identifier, (ServerPlayer, Tag?) -> Unit>()

    public fun <A: Any> register(
        id: Identifier,
        decoder: Decoder<A>,
        fallback: A,
        success: (ServerPlayer, A) -> Unit
    ): Identifier {
        return this.register(id, decoder, success, { player, _ -> success.invoke(player, fallback) })
    }

    public fun <A: Any> register(
        id: Identifier,
        decoder: Decoder<A>,
        success: (ServerPlayer, A) -> Unit,
        error: (ServerPlayer, String) -> Unit = { p, m -> ArcadeUtils.logger.warn("Failed to decode action $id for ${p.username}: $m") },
        nullable: (ServerPlayer) -> Unit = { p -> error.invoke(p, "Payload was null") }
    ): Identifier {
        return this.register(id) { player, tag ->
            if (tag != null) {
                val parsed = decoder.parse(this.createOps(), tag)
                parsed.ifSuccess { v -> success.invoke(player, v) }
                parsed.ifError { e -> error.invoke(player, e.message()) }
            } else {
                nullable.invoke(player)
            }
        }
    }

    public fun register(
        id: Identifier,
        callback: (ServerPlayer, Tag?) -> Unit
    ): Identifier {
        this.actions[id] = callback
        return id
    }

    private fun onPlayerCustomClickAction(event: PlayerCustomClickActionEvent) {
        if (!event.consumed()) {
            val (player, id, tag) = event
            val callback = this.actions[id] ?: return
            event.consume()
            callback.invoke(player, tag)
        }
    }

    private fun createOps(): RegistryOps<Tag> {
        return this.access.createSerializationContext(NbtOps.INSTANCE)
    }

    public companion object {
        public fun register(registry: DialogCustomActionRegistry, priority: Int = 1_000) {
            GlobalEventHandler.Server.register<PlayerCustomClickActionEvent>(
                priority = priority, listener = registry::onPlayerCustomClickAction
            )
        }
    }
}
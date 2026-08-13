/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.npc.extensions

import it.unimi.dsi.fastutil.objects.ReferenceLinkedOpenHashSet
import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.utils.register
import net.casual.arcade.extensions.Extension
import net.casual.arcade.extensions.event.LevelExtensionEvent
import net.casual.arcade.extensions.utils.getExtension
import net.casual.arcade.npc.FakePlayer
import net.minecraft.server.level.ServerLevel

internal class LevelNavigatingPlayersExtension: Extension {
    private val players = ReferenceLinkedOpenHashSet<FakePlayer>()

    fun add(player: FakePlayer) {
        this.players.add(player)
    }

    fun remove(player: FakePlayer) {
        this.players.remove(player)
    }

    fun empty(): Boolean {
        return this.players.isEmpty()
    }

    inline fun forEachPlayer(consumer: (FakePlayer) -> Unit) {
        val iterator = this.players.iterator()
        while (iterator.hasNext()) {
            val player = iterator.next()
            if (player.isRemoved) {
                iterator.remove()
                continue
            }
            consumer.invoke(player)
        }
    }

    companion object {
        val ServerLevel.navigatingPlayersExtension: LevelNavigatingPlayersExtension
            get() = this.getExtension()

        fun registerEvents() {
            GlobalEventHandler.Server.register<LevelExtensionEvent> {
                it.addExtension(LevelNavigatingPlayersExtension())
            }
        }
    }
}

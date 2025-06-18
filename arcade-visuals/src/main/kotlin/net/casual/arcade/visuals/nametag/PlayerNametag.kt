/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.visuals.nametag

import net.casual.arcade.nametags.Nametag
import net.casual.arcade.nametags.extensions.EntityNametagExtension.Companion.addNametag
import net.casual.arcade.nametags.extensions.EntityNametagExtension.Companion.removeNametag
import net.casual.arcade.utils.TimeUtils.Ticks
import net.casual.arcade.utils.time.MinecraftTimeDuration
import net.casual.arcade.visuals.core.TrackedPlayerUI
import net.casual.arcade.visuals.elements.ComponentElements
import net.casual.arcade.visuals.elements.PlayerSpecificElement
import net.casual.arcade.visuals.predicate.PlayerObserverPredicate
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientGamePacketListener
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity
import java.util.function.Consumer

/**
 * This class represents a custom player name tag.
 *
 * This implementation of a custom name tag is completely
 * server-sided, and you can set the contents of the name
 * tag to whatever you wish.
 *
 * This name tag can be added to any player, it will
 * automatically remove their default name tag, furthermore,
 * you can add multiple name tags to each player which
 * all have a [observable] which determines which
 * other players can see the player's nametag.
 *
 * This may be useful, for example, if you want to display
 * a player's health to their teammates and spectators but
 * not to enemies.
 *
 * @param tag The [PlayerSpecificElement] to get the player's nametag.
 * @param observable The predicate to determine which
 * players can see the player's nametag.
 * @see TrackedPlayerUI
 */
public class PlayerNametag(
    /**
     * The [ComponentElements] to get the player's nametag.
     */
    public var tag: PlayerSpecificElement<Component>,
    /**
     * The predicate to determine which players can see
     * the player's nametag.
     */
    private val observable: PlayerObserverPredicate = PlayerObserverPredicate { _, _ -> true },
): TrackedPlayerUI(), Nametag {
    override val updateInterval: MinecraftTimeDuration
        get() = this.interval.Ticks

    override fun getComponent(observee: Entity): Component {
        if (observee !is ServerPlayer) {
            throw IllegalArgumentException("Cannot get PlayerNametag component for non-player!")
        }
        return this.tag.get(observee)
    }

    override fun isObservable(observee: Entity, observer: ServerPlayer): Boolean {
        return this.observable.observable(observee, observer)
    }

    override fun onAddPlayer(player: ServerPlayer) {
        player.addNametag(this)
    }

    override fun onRemovePlayer(player: ServerPlayer) {
        player.removeNametag(this)
    }

    override fun resendTo(player: ServerPlayer, sender: Consumer<Packet<ClientGamePacketListener>>) {

    }
}
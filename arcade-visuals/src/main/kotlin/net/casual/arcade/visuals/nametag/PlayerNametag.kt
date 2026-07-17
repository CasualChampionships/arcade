/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.visuals.nametag

import net.casual.arcade.nametags.Nametag
import net.casual.arcade.nametags.extensions.EntityNametagExtension.Companion.nametagExtension
import net.casual.arcade.observer.Observer
import net.casual.arcade.visuals.core.TrackingVisualElement
import net.casual.arcade.visuals.elements.PlayerSpecificElement
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
 * automatically remove their default name tag.
 *
 * This may be useful, for example, if you want to display
 * a player's health to their teammates and spectators but
 * not to enemies.
 *
 * @param nametag The nametag.
 * @see TrackingVisualElement
 */
public class PlayerNametag(
    /**
     * The nametag to give the player(s).
     */
    private val nametag: Nametag
): TrackingVisualElement() {
    override fun onAddPlayer(player: ServerPlayer) {
        player.nametagExtension.add(this.nametag)
    }

    override fun onRemovePlayer(player: ServerPlayer) {
        player.nametagExtension.remove(this.nametag)
    }

    override fun resendTo(player: ServerPlayer, sender: Consumer<Packet<ClientGamePacketListener>>) {

    }

    public companion object {
        public fun simple(
            component: PlayerSpecificElement<Component>,
            predicate: (ServerPlayer, Observer) -> Boolean = { _, _ -> true }
        ): PlayerNametag {
            val nametag = object: Nametag {
                override fun getComponent(observee: Entity): Component {
                    require(observee is ServerPlayer) { "Player nametag cannot be applied to non-player entity" }
                    return component.get(observee)
                }

                override fun isObservable(observee: Entity, observer: Observer): Boolean {
                    require(observee is ServerPlayer) { "Player nametag cannot be applied to non-player entity" }
                    return predicate.invoke(observee, observer)
                }
            }
            return PlayerNametag(nametag)
        }
    }
}
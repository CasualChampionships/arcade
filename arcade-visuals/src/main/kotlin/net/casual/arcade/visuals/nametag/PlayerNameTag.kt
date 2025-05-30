/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.visuals.nametag

import me.senseiwells.nametag.api.NameTag
import me.senseiwells.nametag.impl.NameTagUtils.addNameTag
import me.senseiwells.nametag.impl.NameTagUtils.removeNameTag
import me.senseiwells.nametag.impl.ShiftHeight
import net.casual.arcade.visuals.core.PlayerUI
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
 * @param tag The [ComponentElements] to get the player's nametag.
 * @param observable The predicate to determine which
 * players can see the player's nametag.
 * @see PlayerUI
 */
public class PlayerNameTag(
    /**
     * The [ComponentElements] to get the player's nametag.
     */
    public var tag: PlayerSpecificElement<Component>,
    /**
     * The predicate to determine which players can see
     * the player's nametag.
     */
    public val observable: PlayerObserverPredicate = PlayerObserverPredicate { _, _ -> true },
): PlayerUI(), NameTag {
    override val updateInterval: Int
        get() = this.interval

    override fun getComponent(entity: Entity): Component {
        if (entity !is ServerPlayer) {
            throw IllegalArgumentException("Cannot get ArcadeNameTag component for non-player!")
        }
        return this.tag.get(entity)
    }

    override fun getShift(): ShiftHeight {
        return ShiftHeight.SMALL
    }

    override fun isObservable(observee: Entity, observer: ServerPlayer): Boolean {
        return this.observable.observable(observee, observer)
    }

    override fun onAddPlayer(player: ServerPlayer) {
        player.addNameTag(this)
    }

    override fun onRemovePlayer(player: ServerPlayer) {
        player.removeNameTag(this)
    }

    override fun resendTo(player: ServerPlayer, sender: Consumer<Packet<ClientGamePacketListener>>) {
        // We do not need to handle this, CustomNameTags already handles it for us :)
    }
}
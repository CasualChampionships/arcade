package net.casual.arcade.gui.nametag

import me.senseiwells.nametag.api.NameTag
import me.senseiwells.nametag.impl.NameTagUtils.addNameTag
import me.senseiwells.nametag.impl.NameTagUtils.removeNameTag
import me.senseiwells.nametag.impl.ShiftHeight
import net.casual.arcade.gui.PlayerUI
import net.casual.arcade.gui.elements.ComponentElements
import net.casual.arcade.gui.elements.PlayerSpecificElement
import net.casual.arcade.gui.predicate.PlayerObserverPredicate
import net.casual.arcade.minigame.managers.MinigameUIManager
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientGamePacketListener
import net.minecraft.server.level.ServerPlayer
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
 * @see MinigameUIManager.addNameTag
 * @see PlayerUI
 */
public class ArcadeNameTag(
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

    override fun getComponent(player: ServerPlayer): Component {
        return this.tag.get(player)
    }

    override fun getShift(): ShiftHeight {
        return ShiftHeight.Medium
    }

    override fun isObservable(observee: ServerPlayer, observer: ServerPlayer): Boolean {
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
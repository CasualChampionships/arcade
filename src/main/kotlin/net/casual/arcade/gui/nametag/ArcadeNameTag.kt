package net.casual.arcade.gui.nametag

import net.casual.arcade.gui.PlayerUI
import net.casual.arcade.gui.suppliers.ComponentSupplier
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.utils.NameTagUtils.nameTags
import net.minecraft.server.level.ServerPlayer

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
 * @param tag The [ComponentSupplier] to get the player's nametag.
 * @param observable The predicate to determine which
 * players can see the player's nametag.
 * @see Minigame.addNameTag
 * @see PlayerUI
 */
public class ArcadeNameTag(
    /**
     * The [ComponentSupplier] to get the player's nametag.
     */
    public var tag: ComponentSupplier,
    /**
     * The predicate to determine which players can see
     * the player's nametag.
     */
    public val observable: ObserverPredicate = ObserverPredicate { _, _ -> true }
): PlayerUI() {
    override fun onAddPlayer(player: ServerPlayer) {
        player.nameTags.addNameTag(this)
    }

    override fun onRemovePlayer(player: ServerPlayer) {
        player.nameTags.removeNameTag(this)
    }
}
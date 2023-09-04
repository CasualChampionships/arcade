package net.casual.arcade.gui.nametag

import com.google.common.base.Predicates
import net.casual.arcade.gui.PlayerUI
import net.casual.arcade.gui.suppliers.ComponentSupplier
import net.casual.arcade.utils.NameTagUtils.nameTags
import net.minecraft.server.level.ServerPlayer
import java.util.function.Predicate

class ArcadeNameTag(
    var tag: ComponentSupplier,
    private var watcherPredicate: Predicate<ServerPlayer> = Predicates.alwaysTrue()
): PlayerUI() {
    val predicate = Predicate<ServerPlayer> { this.watcherPredicate.test(it) }

    fun setPredicate(predicate: Predicate<ServerPlayer>) {
        this.watcherPredicate = predicate
    }

    override fun onAddPlayer(player: ServerPlayer) {
        player.nameTags.addNameTag(this)
    }

    override fun onRemovePlayer(player: ServerPlayer) {
        player.nameTags.removeNameTag(this)
    }
}
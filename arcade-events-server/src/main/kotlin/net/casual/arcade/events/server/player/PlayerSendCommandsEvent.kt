package net.casual.arcade.events.server.player

import com.mojang.brigadier.tree.RootCommandNode
import net.minecraft.commands.CommandSourceStack
import net.minecraft.server.level.ServerPlayer
import java.util.*

public class PlayerSendCommandsEvent(
    override val player: ServerPlayer
): PlayerEvent {
    private val nodes = LinkedList<RootCommandNode<CommandSourceStack>>()

    public fun addCustomCommandNode(root: RootCommandNode<CommandSourceStack>) {
        this.nodes.add(root)
    }

    public fun getCustomCommandNodes(): List<RootCommandNode<CommandSourceStack>> {
        return this.nodes
    }
}
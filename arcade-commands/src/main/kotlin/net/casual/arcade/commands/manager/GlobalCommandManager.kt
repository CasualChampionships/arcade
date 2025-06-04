package net.casual.arcade.commands.manager

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import net.casual.arcade.commands.CommandTree
import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.ListenerRegistry
import net.casual.arcade.events.ListenerRegistry.Companion.register
import net.casual.arcade.events.SimpleListenerRegistry
import net.casual.arcade.events.server.ServerLoadedEvent
import net.minecraft.commands.CommandBuildContext
import net.minecraft.commands.CommandSourceStack

public object GlobalCommandManager: CommandRegistry {
    private val delayed = ArrayList<CommandTree>()
    private val managers = HashMap<CommandManager, ListenerRegistry>()

    private lateinit var global: CommandManager

    override fun register(literal: LiteralArgumentBuilder<CommandSourceStack>) {
        this.register(object: CommandTree {
            override fun create(buildContext: CommandBuildContext): LiteralArgumentBuilder<CommandSourceStack> {
                return literal
            }
        })
    }

    override fun register(tree: CommandTree) {
        if (this::global.isInitialized) {
            this.global.register(tree)
        } else {
            this.delayed.add(tree)
        }
    }

    public fun addManager(manager: CommandManager) {
        if (!this.managers.containsKey(manager)) {
            val registry = SimpleListenerRegistry()
            GlobalEventHandler.Server.addProvider(registry)
            this.managers[manager] = registry
            manager.initialize(registry)
        }
    }

    public fun removeManager(manager: CommandManager) {
        val registry = this.managers.remove(manager)
        if (registry != null) {
            GlobalEventHandler.Server.removeProvider(registry)
            manager.close()
        }
    }

    internal fun registerEvents() {
        GlobalEventHandler.Server.register<ServerLoadedEvent> {
            this.global = CommandManager(it.server)
            this.addManager(this.global)
            for (tree in this.delayed) {
                this.global.register(tree)
            }
            this.delayed.clear()
        }
    }
}
/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.commands.manager

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import net.casual.arcade.commands.CommandTree
import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.ListenerRegistry
import net.casual.arcade.events.SimpleListenerRegistry
import net.casual.arcade.events.common.ServerSideEvent
import net.casual.arcade.events.server.ServerStartEvent
import net.casual.arcade.events.utils.register
import net.minecraft.commands.CommandBuildContext
import net.minecraft.commands.CommandSourceStack

public object GlobalCommandManager: CommandRegistry<CommandSourceStack> {
    private val delayed = ArrayList<CommandTree<CommandSourceStack>>()
    private val managers = HashMap<ServerCommandManager, ListenerRegistry<ServerSideEvent>>()

    private lateinit var global: ServerCommandManager

    override fun register(literal: LiteralArgumentBuilder<CommandSourceStack>) {
        this.register(object: CommandTree<CommandSourceStack> {
            override fun create(buildContext: CommandBuildContext): LiteralArgumentBuilder<CommandSourceStack> {
                return literal
            }
        })
    }

    override fun register(tree: CommandTree<CommandSourceStack>) {
        if (this::global.isInitialized) {
            this.global.register(tree)
        } else {
            this.delayed.add(tree)
        }
    }

    public fun addManager(manager: ServerCommandManager) {
        if (!this.managers.containsKey(manager)) {
            val registry = SimpleListenerRegistry<ServerSideEvent>()
            GlobalEventHandler.Server.addProvider(registry)
            this.managers[manager] = registry
            manager.initialize(registry)
        }
    }

    public fun removeManager(manager: ServerCommandManager) {
        val registry = this.managers.remove(manager)
        if (registry != null) {
            GlobalEventHandler.Server.removeProvider(registry)
            manager.close()
        }
    }

    internal fun registerEvents() {
        GlobalEventHandler.Server.register<ServerStartEvent> {
            this.global = ServerCommandManager(it.server)
            this.addManager(this.global)
            for (tree in this.delayed) {
                this.global.register(tree)
            }
            this.delayed.clear()
        }
    }
}
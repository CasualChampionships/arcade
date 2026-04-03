/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.dimensions.level.extensions

import net.casual.arcade.dimensions.level.spawner.CustomMobSpawningRules
import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.ListenerRegistry.Companion.register
import net.casual.arcade.extensions.SerializableExtension
import net.casual.arcade.extensions.event.LevelExtensionEvent
import net.casual.arcade.utils.arcade
import net.minecraft.resources.Identifier
import net.minecraft.world.level.storage.ValueInput
import net.minecraft.world.level.storage.ValueOutput
import org.jetbrains.annotations.ApiStatus
import kotlin.jvm.optionals.getOrNull

@ApiStatus.Internal
public class LevelCustomMobSpawningExtension: SerializableExtension {
    public var rules: CustomMobSpawningRules? = null

    override fun id(): Identifier {
        return arcade("custom_mob_spawning")
    }

    override fun serialize(output: ValueOutput) {
        val rules = this.rules ?: return
        output.store("rules", CustomMobSpawningRules.CODEC, rules)
    }

    override fun deserialize(input: ValueInput) {
        this.rules = input.read("rules", CustomMobSpawningRules.CODEC).getOrNull()
    }

    public companion object {
        internal fun registerEvents() {
            GlobalEventHandler.Server.register<LevelExtensionEvent> { event ->
                event.addExtension(LevelCustomMobSpawningExtension())
            }
        }
    }
}
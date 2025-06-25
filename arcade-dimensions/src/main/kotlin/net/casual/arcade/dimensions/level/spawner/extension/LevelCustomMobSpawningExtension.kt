/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.dimensions.level.spawner.extension

import net.casual.arcade.dimensions.level.spawner.CustomMobSpawningRules
import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.ListenerRegistry.Companion.register
import net.casual.arcade.extensions.DataExtension
import net.casual.arcade.extensions.event.LevelExtensionEvent
import net.casual.arcade.utils.ArcadeUtils
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.storage.ValueInput
import net.minecraft.world.level.storage.ValueOutput
import org.jetbrains.annotations.ApiStatus.Internal
import kotlin.jvm.optionals.getOrNull

@Internal
public class LevelCustomMobSpawningExtension: DataExtension {
    public var rules: CustomMobSpawningRules? = null

    override fun getId(): ResourceLocation {
        return ArcadeUtils.id("custom_mob_spawning")
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
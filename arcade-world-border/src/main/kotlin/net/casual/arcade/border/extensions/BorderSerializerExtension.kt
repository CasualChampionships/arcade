/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.border.extensions

import net.casual.arcade.border.ducks.SerializableBorder
import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.ListenerRegistry.Companion.register
import net.casual.arcade.extensions.DataExtension
import net.casual.arcade.extensions.event.LevelExtensionEvent
import net.casual.arcade.scheduler.GlobalTickedScheduler
import net.casual.arcade.utils.ArcadeUtils
import net.casual.arcade.utils.ResourceUtils
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.storage.ValueInput
import net.minecraft.world.level.storage.ValueOutput

@Deprecated("This package is deprecated. Use 'net.casual.arcade.boundary' instead.")
internal class BorderSerializerExtension(
    private val level: ServerLevel
): DataExtension {
    override fun getId(): ResourceLocation {
        return ArcadeUtils.id("border_serializer")
    }

    override fun serialize(output: ValueOutput) {
        (this.level.worldBorder as SerializableBorder).`arcade$serialize`(output)
    }

    override fun deserialize(input: ValueInput) {
        GlobalTickedScheduler.later {
            (this.level.worldBorder as SerializableBorder).`arcade$deserialize`(input)
        }
    }

    companion object {
        fun registerEvents() {
            GlobalEventHandler.Server.register<LevelExtensionEvent> { event ->
                event.addExtension(::BorderSerializerExtension)
            }
        }
    }
}
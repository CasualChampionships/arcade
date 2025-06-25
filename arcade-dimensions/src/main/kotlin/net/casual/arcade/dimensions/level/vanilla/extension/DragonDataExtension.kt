/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.dimensions.level.vanilla.extension

import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.ListenerRegistry.Companion.register
import net.casual.arcade.extensions.DataExtension
import net.casual.arcade.extensions.event.LevelExtensionEvent
import net.casual.arcade.utils.ArcadeUtils
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.Level
import net.minecraft.world.level.dimension.end.EndDragonFight
import net.minecraft.world.level.storage.ValueInput
import net.minecraft.world.level.storage.ValueOutput
import kotlin.jvm.optionals.getOrNull

internal class DragonDataExtension(
    private val level: ServerLevel
): DataExtension {
    private var data: EndDragonFight.Data? = null

    fun getDataOrDefault(): EndDragonFight.Data {
        return this.data ?: EndDragonFight.Data.DEFAULT
    }

    override fun getId(): ResourceLocation {
        return ArcadeUtils.id("dragon_data")
    }

    override fun serialize(output: ValueOutput) {
        val fight = this.level.dragonFight
        // We let vanilla handle the default end dimension.
        if (fight == null || this.level.dimension() == Level.END) {
            return
        }

        output.store("fight_data", EndDragonFight.Data.CODEC, fight.saveData())
    }

    override fun deserialize(input: ValueInput) {
        this.data = input.read("fight_data", EndDragonFight.Data.CODEC).getOrNull()
    }

    companion object {
        fun registerEvents() {
            GlobalEventHandler.Server.register<LevelExtensionEvent> { event ->
                event.addExtension(::DragonDataExtension)
            }
        }
    }
}
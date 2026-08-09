/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.scheduler.utils

import com.mojang.serialization.MapCodec
import net.casual.arcade.scheduler.task.routine.Routine
import net.casual.arcade.scheduler.task.routine.RoutineScope
import net.casual.arcade.utils.arcade
import net.casual.arcade.utils.serialization.codec.CodecProvider
import net.minecraft.resources.Identifier

public object EmptyRoutine: Routine<Unit>, CodecProvider<EmptyRoutine> {
    override val id: Identifier = arcade("empty")

    override val codec: MapCodec<out EmptyRoutine> = MapCodec.unit(this)

    override fun codec(): MapCodec<out Routine<Unit>> {
        return this.codec
    }

    override suspend fun RoutineScope<Unit>.run() {

    }

    @Suppress("UNCHECKED_CAST")
    public fun <O> cast(): Routine<O> {
        return EmptyRoutine as Routine<O>
    }
}
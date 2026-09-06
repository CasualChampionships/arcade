/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.tests.server.scheduler

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.common.ServerSideEvent
import net.casual.arcade.events.phase.BuiltInEventPhases
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.casual.arcade.scheduler.task.ScheduledTask
import net.casual.arcade.scheduler.task.routine.Routine
import net.casual.arcade.scheduler.task.routine.RoutineScope
import net.casual.arcade.scheduler.utils.TaskRegistries
import net.casual.arcade.scheduler.utils.await
import net.casual.arcade.scheduler.utils.call
import net.casual.arcade.utils.TimeUtils.Ticks
import net.casual.arcade.utils.arcade
import net.casual.arcade.utils.serialization.codec.CodecProvider
import net.casual.arcade.utils.serialization.codec.CodecProvider.Companion.register
import net.minecraft.resources.Identifier

class RoutineOwner(
    val next: Int = 0
) {
    val log = ArrayList<String>()

    var count = 0
    var recorded = -1
    var held = false
    var remaining = -1
    var handle: ScheduledTask? = null

    fun log(entry: String) {
        this.log.add(entry)
    }
}

class CountingRoutine(
    val times: Int,
    val interval: Int
): Routine<RoutineOwner> {
    override fun codec(): MapCodec<out Routine<RoutineOwner>> {
        return codec
    }

    override suspend fun RoutineScope<RoutineOwner>.run() {
        repeat(times) { index ->
            step("count-$index") { owner.count += 1 }
            delay(interval.Ticks)
        }
        step("finished") { owner.log("finished") }
    }

    companion object: CodecProvider<CountingRoutine> {
        override val id: Identifier = arcade("counting")
        override val codec: MapCodec<CountingRoutine> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                Codec.INT.fieldOf("times").forGetter(CountingRoutine::times),
                Codec.INT.fieldOf("interval").forGetter(CountingRoutine::interval)
            ).apply(instance, ::CountingRoutine)
        }
    }
}

class CleanupRoutine(
    val duration: Int
): Routine<RoutineOwner> {
    override fun codec(): MapCodec<out Routine<RoutineOwner>> {
        return codec
    }

    override suspend fun RoutineScope<RoutineOwner>.run() {
        try {
            step("start") { owner.log("start") }
            delay(duration.Ticks)
            step("end") { owner.log("end") }
        } finally {
            owner.log("cleanup")
        }
    }

    companion object: CodecProvider<CleanupRoutine> {
        override val id: Identifier = arcade("cleanup")
        override val codec: MapCodec<CleanupRoutine> = Codec.INT.fieldOf("duration")
            .xmap(::CleanupRoutine, CleanupRoutine::duration)
    }
}

class RecordingRoutine: Routine<RoutineOwner> {
    override fun codec(): MapCodec<out Routine<RoutineOwner>> {
        return codec
    }

    override suspend fun RoutineScope<RoutineOwner>.run() {
        val picked = step(Codec.INT, "pick") { owner.next }
        delay(1.Ticks)
        step("record") { owner.recorded = picked }
    }

    companion object: CodecProvider<RecordingRoutine> {
        override val id: Identifier = arcade("recording")
        override val codec: MapCodec<RecordingRoutine> = MapCodec.unit(::RecordingRoutine)
    }
}

class NestedRoutine: Routine<RoutineOwner> {
    override fun codec(): MapCodec<out Routine<RoutineOwner>> {
        return codec
    }

    override suspend fun RoutineScope<RoutineOwner>.run() {
        step("outer-start") { owner.log("outer-start") }
        call(InnerRoutine())
        step("outer-end") { owner.log("outer-end") }
    }

    companion object: CodecProvider<NestedRoutine> {
        override val id: Identifier = arcade("nested")
        override val codec: MapCodec<NestedRoutine> = MapCodec.unit(::NestedRoutine)
    }
}

class InnerRoutine: Routine<RoutineOwner> {
    override fun codec(): MapCodec<out Routine<RoutineOwner>> {
        return codec
    }

    override suspend fun RoutineScope<RoutineOwner>.run() {
        step("inner-start") { owner.log("inner-start") }
        delay(2.Ticks)
        step("inner-end") { owner.log("inner-end") }
    }

    companion object {
        val codec: MapCodec<InnerRoutine> = MapCodec.unit(::InnerRoutine)
    }
}

class OutdatedRoutine(
    override val version: Int
): Routine<RoutineOwner> {
    override fun codec(): MapCodec<out Routine<RoutineOwner>> {
        return codec
    }

    override suspend fun RoutineScope<RoutineOwner>.run() {
        step("first") { owner.log("first") }
        delay(2.Ticks)
        step("second") { owner.log("second") }
    }

    companion object: CodecProvider<OutdatedRoutine> {
        override val id: Identifier = arcade("outdated")
        override val codec: MapCodec<OutdatedRoutine> = MapCodec.unit { OutdatedRoutine(2) }
    }
}

class DivergingRoutine(
    val inserted: Boolean
): Routine<RoutineOwner> {
    override fun codec(): MapCodec<out Routine<RoutineOwner>> {
        return codec
    }

    override suspend fun RoutineScope<RoutineOwner>.run() {
        if (inserted) {
            step("inserted") { owner.log("inserted") }
        }
        step("first") { owner.log("first") }
        delay(2.Ticks)
        step("second") { owner.log("second") }
    }

    companion object: CodecProvider<DivergingRoutine> {
        override val id: Identifier = arcade("diverging")
        override val codec: MapCodec<DivergingRoutine> = MapCodec.unit { DivergingRoutine(true) }
    }
}

class HoldingRoutine(
    val duration: Int
): Routine<RoutineOwner> {
    override fun codec(): MapCodec<out Routine<RoutineOwner>> {
        return codec
    }

    override suspend fun RoutineScope<RoutineOwner>.run() {
        owner.held = true
        owner.log("held")
        try {
            delay(duration.Ticks) { remaining -> owner.remaining = remaining.ticks }
        } finally {
            owner.held = false
            owner.log("released")
        }
    }

    companion object: CodecProvider<HoldingRoutine> {
        override val id: Identifier = arcade("holding")
        override val codec: MapCodec<HoldingRoutine> = Codec.INT.fieldOf("duration")
            .xmap(::HoldingRoutine, HoldingRoutine::duration)
    }
}

class SelfCancellingRoutine(
    val delay: Int
): Routine<RoutineOwner> {
    override fun codec(): MapCodec<out Routine<RoutineOwner>> {
        return codec
    }

    override suspend fun RoutineScope<RoutineOwner>.run() {
        try {
            step("start") { owner.log("start") }
            if (delay > 0) {
                delay(delay.Ticks)
            }
            step("cancel") {
                owner.log("cancel")
                owner.handle?.cancel()
            }
            step("after") { owner.log("after") }
            delay(5.Ticks)
            step("resumed") { owner.log("resumed") }
        } finally {
            owner.log("cleanup")
        }
    }

    companion object: CodecProvider<SelfCancellingRoutine> {
        override val id: Identifier = arcade("self_cancelling")
        override val codec: MapCodec<SelfCancellingRoutine> = Codec.INT.fieldOf("delay")
            .xmap(::SelfCancellingRoutine, SelfCancellingRoutine::delay)
    }
}

class TestRoutineEvent(val value: Int): ServerSideEvent

class AwaitingRoutine(
    val phase: Int = BuiltInEventPhases.DEFAULT,
    val priority: Int = 1_000
): Routine<RoutineOwner> {
    override fun codec(): MapCodec<out Routine<RoutineOwner>> {
        return codec
    }

    override suspend fun RoutineScope<RoutineOwner>.run() {
        try {
            step("start") { owner.log("start") }
            await<TestRoutineEvent>(
                GlobalEventHandler.Server,
                id = "received",
                priority = priority,
                phase = phase,
                predicate = { it.value > 0 }
            ) { event ->
                owner.log("received")
                owner.recorded = event.value
            }
            delay(5.Ticks)
            step("end") { owner.log("end") }
        } finally {
            owner.log("cleanup")
        }
    }

    companion object: CodecProvider<AwaitingRoutine> {
        override val id: Identifier = arcade("awaiting")
        override val codec: MapCodec<AwaitingRoutine> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                Codec.INT.optionalFieldOf("phase", BuiltInEventPhases.DEFAULT).forGetter(AwaitingRoutine::phase),
                Codec.INT.optionalFieldOf("priority", 1_000).forGetter(AwaitingRoutine::priority)
            ).apply(instance, ::AwaitingRoutine)
        }
    }
}

class RecordingAwaitRoutine: Routine<RoutineOwner> {
    override fun codec(): MapCodec<out Routine<RoutineOwner>> {
        return codec
    }

    override suspend fun RoutineScope<RoutineOwner>.run() {
        val value = await<TestRoutineEvent, Int>(GlobalEventHandler.Server, Codec.INT, "await") { event ->
            owner.log("received")
            event.value
        }
        delay(5.Ticks)
        step("record") { owner.recorded = value }
    }

    companion object: CodecProvider<RecordingAwaitRoutine> {
        override val id: Identifier = arcade("recording_await")
        override val codec: MapCodec<RecordingAwaitRoutine> = MapCodec.unit(::RecordingAwaitRoutine)
    }
}

class UnregisteredRoutine: Routine<RoutineOwner> {
    override fun codec(): MapCodec<out Routine<RoutineOwner>> {
        return codec
    }

    override suspend fun RoutineScope<RoutineOwner>.run() {
        step("unregistered") { owner.log("unregistered") }
    }

    companion object {
        val codec: MapCodec<UnregisteredRoutine> = MapCodec.unit(::UnregisteredRoutine)
    }
}

object TestRoutines {
    fun registerRoutines() {
        CountingRoutine.register(TaskRegistries.ROUTINE)
        CleanupRoutine.register(TaskRegistries.ROUTINE)
        RecordingRoutine.register(TaskRegistries.ROUTINE)
        NestedRoutine.register(TaskRegistries.ROUTINE)
        OutdatedRoutine.register(TaskRegistries.ROUTINE)
        DivergingRoutine.register(TaskRegistries.ROUTINE)
        HoldingRoutine.register(TaskRegistries.ROUTINE)
        SelfCancellingRoutine.register(TaskRegistries.ROUTINE)
        AwaitingRoutine.register(TaskRegistries.ROUTINE)
        RecordingAwaitRoutine.register(TaskRegistries.ROUTINE)
    }
}

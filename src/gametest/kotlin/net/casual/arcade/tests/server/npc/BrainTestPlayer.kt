/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.tests.server.npc

import com.google.common.collect.ImmutableList
import com.mojang.authlib.GameProfile
import net.casual.arcade.gametest.utils.TestFakePlayer
import net.casual.arcade.npc.utils.AttributeUtils.toBuilder
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ClientInformation
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.ActivityData
import net.minecraft.world.entity.ai.Brain
import net.minecraft.world.entity.ai.attributes.AttributeSupplier
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.entity.ai.behavior.BehaviorControl
import net.minecraft.world.entity.ai.memory.MemoryModuleType
import net.minecraft.world.entity.ai.sensing.Sensor
import net.minecraft.world.entity.ai.sensing.SensorType
import net.minecraft.world.entity.schedule.Activity

class BrainTestPlayer(
    server: MinecraftServer,
    level: ServerLevel,
    profile: GameProfile,
    info: ClientInformation
): TestFakePlayer(server, level, profile, info) {
    override fun createAttributeSupplier(): AttributeSupplier {
        return super.createAttributeSupplier().toBuilder()
            .add(Attributes.FOLLOW_RANGE, FOLLOW_RANGE)
            .build()
    }

    fun initialize(
        core: List<BehaviorControl<in BrainTestPlayer>> = listOf(),
        idle: List<BehaviorControl<in BrainTestPlayer>> = listOf(),
        fight: List<BehaviorControl<in BrainTestPlayer>> = listOf(),
        sensors: List<SensorType<out Sensor<in BrainTestPlayer>>> = listOf(SensorType.NEAREST_LIVING_ENTITIES)
    ) {
        val activities = listOf(
            ActivityData.create(Activity.CORE, 0, ImmutableList.copyOf(core)),
            ActivityData.create(Activity.IDLE, 0, ImmutableList.copyOf(idle)),
            ActivityData.create(Activity.FIGHT, 0, ImmutableList.copyOf(fight), MemoryModuleType.ATTACK_TARGET)
        )
        val provider = Brain.provider<BrainTestPlayer>(sensors) { _ -> activities }
        val brain = provider.makeBrain(this, Brain.Packed.EMPTY)
        brain.setCoreActivities(setOf(Activity.CORE))
        brain.setDefaultActivity(Activity.IDLE)
        brain.useDefaultActivity()

        this.brain = brain
    }

    override fun customServerAiStep(level: ServerLevel) {
        val brain = this.getBrain()
        brain.tick(level, this)
        brain.setActiveActivityToFirstValid(listOf(Activity.FIGHT, Activity.IDLE))
    }

    fun <U: Any> memory(type: MemoryModuleType<U>): U? {
        return this.brain.getMemory(type).orElse(null)
    }

    fun <U: Any> setMemory(type: MemoryModuleType<U>, value: U) {
        this.brain.setMemory(type, value)
    }

    fun hasMemory(type: MemoryModuleType<*>): Boolean {
        return this.brain.hasMemoryValue(type)
    }

    fun sees(entity: LivingEntity): Boolean {
        val nearest = this.memory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES)
        return nearest != null && nearest.contains(entity)
    }

    @Suppress("UNCHECKED_CAST")
    override fun getBrain(): Brain<BrainTestPlayer> {
        return super.getBrain() as Brain<BrainTestPlayer>
    }

    companion object {
        private const val FOLLOW_RANGE = 32.0
    }
}

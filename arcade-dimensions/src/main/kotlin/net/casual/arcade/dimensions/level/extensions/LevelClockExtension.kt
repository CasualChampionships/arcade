/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.dimensions.level.extensions

import net.casual.arcade.dimensions.level.clock.LevelClockInstance
import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.ListenerRegistry.Companion.register
import net.casual.arcade.events.server.level.LevelTickEvent
import net.casual.arcade.extensions.SerializableExtension
import net.casual.arcade.extensions.utils.getExtension
import net.casual.arcade.utils.arcade
import net.casual.arcade.utils.registries.isOf
import net.casual.arcade.utils.player.broadcast
import net.minecraft.core.Holder
import net.minecraft.core.registries.Registries
import net.minecraft.network.protocol.game.ClientboundSetTimePacket
import net.minecraft.resources.Identifier
import net.minecraft.resources.ResourceKey
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.clock.ClockNetworkState
import net.minecraft.world.clock.ClockState
import net.minecraft.world.clock.ClockTimeMarker
import net.minecraft.world.clock.WorldClock
import net.minecraft.world.level.gamerules.GameRules
import net.minecraft.world.level.storage.ValueInput
import net.minecraft.world.level.storage.ValueOutput
import org.jetbrains.annotations.ApiStatus.Internal
import kotlin.jvm.optionals.getOrNull

public class LevelClockExtension(
    private val level: ServerLevel
): SerializableExtension {
    private val markers = HashMap<ResourceKey<ClockTimeMarker>, ClockTimeMarker>()

    private var instance: LevelClockInstance? = null
    private var initialized: Boolean = false

    init {
        this.loadMarkers()
    }

    public fun initialized(): Boolean {
        return this.initialized
    }

    public fun customized(): Boolean {
        return this.instance != null
    }

    public fun createNetworkState(): ClockNetworkState? {
        return this.instance?.packNetworkState(this.level.gameRules)
    }

    public fun set(state: ClockState) {
        val holder = this.level.dimensionType().defaultClock.getOrNull()
            ?: throw IllegalStateException("Cannot set clock state for level without a default clock!")

        this.initialized = true
        val instance = LevelClockInstance.from(state)
        this.instance = instance

        val packet = ClientboundSetTimePacket(
            this.level.gameTime, mapOf(holder to instance.packNetworkState(this.level.gameRules))
        )
        this.level.players().broadcast(packet)
    }

    public fun modify(modifier: (ClockState) -> ClockState) {
        val state = this.instance?.packState() ?: return
        this.set(modifier.invoke(state))
    }

    public fun clear() {
        this.initialized = true
        this.instance = null

        // Marks the clock dirty
        val defaultClock = this.level.dimensionType().defaultClock().getOrNull()
        if (defaultClock != null) {
            this.level.clockManager().addTicks(defaultClock, 0)
        }
    }

    @Internal
    public fun ticks(): Long {
        return this.instance?.totalTicks ?: -1
    }

    @Internal
    public fun getMarker(id: ResourceKey<ClockTimeMarker>): ClockTimeMarker? {
        return this.markers[id]
    }

    private fun tick() {
        this.instance?.tick()
    }

    override fun id(): Identifier {
        return arcade("clock")
    }

    override fun serialize(output: ValueOutput) {
        output.storeNullable("state", ClockState.CODEC, this.instance?.packState())
        output.putBoolean("initialized", this.initialized)
    }

    override fun deserialize(input: ValueInput) {
        val state = input.read("state", ClockState.CODEC).getOrNull()
        if (state != null) {
            this.set(state)
        } else {
            this.initialized = input.getBooleanOr("initialized", false)
        }
    }

    private fun loadMarkers() {
        val default = this.level.dimensionType().defaultClock
            .flatMap(Holder<WorldClock>::unwrapKey)
            .getOrNull() ?: return
        return this.level.registryAccess().lookupOrThrow(Registries.TIMELINE).listElements()
            .filter { holder -> holder.value().clock().isOf(default) }
            .forEach { holder ->
                holder.value().registerTimeMarkers { key, marker -> this.markers[key] = marker }
            }
    }

    public companion object {
        @JvmStatic
        public val ServerLevel.clockExtension: LevelClockExtension
            get() = this.getExtension<LevelClockExtension>()

        @JvmStatic
        public fun shouldManuallySyncTime(server: MinecraftServer): Boolean {
            return server.allLevels.any { level -> level.clockExtension.customized() }
        }

        @JvmStatic
        public fun modifyClientboundSetTimePacket(
            packet: ClientboundSetTimePacket,
            level: ServerLevel
        ): ClientboundSetTimePacket {
            val extension = level.clockExtension
            val state = extension.createNetworkState()
            if (state != null) {
                val copy = HashMap(packet.clockUpdates())
                // Our level clock extension should ensure that this will never throw
                val holder = level.dimensionType().defaultClock().orElseThrow()
                copy[holder] = state
                return ClientboundSetTimePacket(packet.gameTime(), copy)
            }
            return packet
        }

        internal fun registerEvents() {
            // We register our extension via mixin, because it needs priority
            GlobalEventHandler.Server.register<LevelTickEvent> { (level) ->
                if (level.gameRules.get(GameRules.ADVANCE_TIME)) {
                    level.clockExtension.tick()
                }
            }
        }
    }
}
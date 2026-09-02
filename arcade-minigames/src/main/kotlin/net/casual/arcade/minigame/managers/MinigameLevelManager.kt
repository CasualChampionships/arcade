/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.minigame.managers

import com.mojang.serialization.Codec
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap
import it.unimi.dsi.fastutil.objects.Reference2ObjectLinkedOpenHashMap
import net.casual.arcade.dimensions.level.CustomLevel
import net.casual.arcade.dimensions.level.LevelPersistence
import net.casual.arcade.dimensions.level.builder.CustomLevelBuilder
import net.casual.arcade.dimensions.utils.addCustomLevel
import net.casual.arcade.dimensions.utils.deleteCustomLevel
import net.casual.arcade.dimensions.utils.hasCustomLevel
import net.casual.arcade.dimensions.utils.loadCustomLevel
import net.casual.arcade.dimensions.utils.removeCustomLevel
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.events.MinigameInitializeEvent
import net.casual.arcade.minigame.exception.MinigameSerializationException
import net.casual.arcade.minigame.utils.MinigameUtils.minigame
import net.casual.arcade.utils.ArcadeUtils
import net.casual.arcade.utils.math.location.LocationWithLevel
import net.casual.arcade.utils.serialization.codec.ArcadeExtraCodecs
import net.minecraft.core.Vec3i
import net.minecraft.resources.Identifier
import net.minecraft.resources.ResourceKey
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.util.StringRepresentable
import net.minecraft.world.level.Level
import net.minecraft.world.level.gamerules.GameRules
import net.minecraft.world.level.levelgen.structure.BoundingBox
import net.minecraft.world.level.storage.ValueInput
import net.minecraft.world.level.storage.ValueOutput
import kotlin.jvm.optionals.getOrDefault
import kotlin.jvm.optionals.getOrNull

/**
 * This class manages the levels of a minigame.
 *
 * It has full support for managing [CustomLevel] instances
 * if you are using the dimensions api.
 *
 * @see Minigame.levels
 */
public class MinigameLevelManager(
    private val minigame: Minigame
): Iterable<ServerLevel> {
    private val levels = Object2ObjectLinkedOpenHashMap<Identifier, Entry>()
    private val entries = Reference2ObjectLinkedOpenHashMap<ServerLevel, Entry>()

    /**
     * The default spawn location for the minigame.
     *
     * If this is not set and a player dies without a respawn
     * point, then the player will respawn in the overworld
     * at the default world spawn.
     */
    public var spawn: SpawnLocation = SpawnLocation.global()

    /**
     * This adds a level to the minigame.
     *
     * If you are using instances of [CustomLevel] you can
     * allow the minigame to handle the loading/unloading of
     * the level over the minigame's lifetime.
     *
     * If you add an instance of [CustomLevel] which **has not**
     * been added to the server then the minigame will handle
     * adding and removing the level, if you previously added
     * the level to the server, then you will also need
     * to handle removing the level.
     *
     * @param level The level to add.
     */
    @JvmOverloads
    public fun add(
        id: Identifier,
        level: ServerLevel,
        ownership: LevelOwnership = LevelOwnership.Borrowed,
        bounds: BoundingBox? = null
    ) {
        require(!this.levels.containsKey(id)) { "Minigame ${this.minigame.id} already has a level $id" }
        require(!this.entries.containsKey(level)) { "Minigame ${this.minigame.id} already has level ${level.dimension().identifier()}" }

        this.validate(id, level, ownership)

        val entry = Entry(id, level, ownership, bounds)
        this.levels[id] = entry
        this.entries[level] = entry

        level.minigame.addMinigame(this.minigame)

        if (this.minigame.initialized) {
            this.ensureLoaded(level)
        }
    }

    @JvmOverloads
    public fun create(
        id: Identifier,
        ownership: LevelOwnership = LevelOwnership.Exclusive,
        bounds: BoundingBox? = null,
        block: CustomLevelBuilder.() -> Unit
    ): CustomLevel {
        val builder = CustomLevelBuilder()
        builder.randomDimensionKey()
        builder.persistence(ownership.persistence())
        builder.block()

        val level = builder.build(this.minigame.server)
        this.add(id, level, ownership, bounds)
        return level
    }

    public fun get(id: Identifier): ServerLevel? {
        return this.levels[id]?.level
    }

    public fun require(id: Identifier): ServerLevel {
        return requireNotNull(this.get(id)) { "Minigame ${this.minigame.id} does not have level $id" }
    }

    public fun ownership(level: ServerLevel): LevelOwnership? {
        return this.entries[level]?.ownership
    }

    public fun bounds(level: ServerLevel): BoundingBox? {
        return this.entries[level]?.bounds
    }

    /**
     * This checks whether a given level is part of this minigame.
     *
     * @param level The level to check whether is part of the minigame.
     * @return Whether the level is part of the minigame.
     */
    public fun has(level: ServerLevel): Boolean {
        return this.entries.containsKey(level)
    }

    public fun has(level: ServerLevel, pos: Vec3i): Boolean {
        val entry = this.entries[level] ?: return false
        val bounds = entry.bounds ?: return true
        return bounds.isInside(pos)
    }

    public fun has(id: Identifier): Boolean {
        return this.levels.containsKey(id)
    }

    public fun ids(): Collection<Identifier> {
        return this.levels.keys
    }

    /**
     * This gets all the levels that are part of the minigame.
     *
     * @return The collection of levels.
     */
    public fun all(): Collection<ServerLevel> {
        return this.entries.keys
    }

    override fun iterator(): Iterator<ServerLevel> {
        return this.all().iterator()
    }

    /**
     * This sets the [GameRules] for all the levels in the minigame.
     *
     * @param modifier The modifier to apply to the game rules.
     * @see GameRules
     */
    public fun setGameRules(modifier: GameRules.() -> Unit) {
        for (level in this.all()) {
            modifier(level.gameRules)
        }
    }

    /**
     * Transfers all levels and ownership of levels
     * to another minigame.
     *
     * @param minigame The minigame to transfer levels to.
     */
    public fun transferTo(minigame: Minigame) {
        if (this.minigame === minigame) {
            return
        }

        for (entry in ArrayList(this.levels.values)) {
            if (!minigame.levels.has(entry.id)) {
                minigame.levels.add(entry.id, entry.level, entry.ownership, entry.bounds)
            }
            this.demote(entry)
        }

        minigame.levels.spawn = this.spawn
    }

    internal fun initialize() {
        for (level in this.all()) {
            this.ensureLoaded(level)
        }
    }

    internal fun close() {
        for (entry in this.levels.values) {
            entry.level.minigame.removeMinigame(this.minigame)
        }
        for (entry in this.levels.values) {
            val level = entry.level
            if (level !is CustomLevel) {
                continue
            }
            when {
                entry.ownership.shouldDeleteOnClose() -> this.minigame.server.deleteCustomLevel(level)
                entry.ownership.shouldUnloadOnClose() -> this.minigame.server.removeCustomLevel(level)
            }
        }
        this.levels.clear()
        this.entries.clear()
    }

    internal fun serialize(output: ValueOutput) {
        val list = output.childrenList("levels")
        for (entry in this.levels.values) {
            val child = list.addChild()
            child.store("id", Identifier.CODEC, entry.id)
            child.store("dimension", ArcadeExtraCodecs.DIMENSION, entry.level.dimension())
            child.store("ownership", LevelOwnership.CODEC, entry.ownership)
            child.storeNullable("bounds", BoundingBox.CODEC, entry.bounds)
        }
    }

    internal fun deserialize(input: ValueInput) {
        for (child in input.childrenListOrEmpty("levels")) {
            val id = child.read("id", Identifier.CODEC).getOrNull() ?: continue
            val dimension = child.read("dimension", ArcadeExtraCodecs.DIMENSION).getOrNull()
                ?: throw MinigameSerializationException("Minigame ${this.minigame.id} has level $id with no dimension")
            val ownership = child.read("ownership", LevelOwnership.CODEC).getOrDefault(LevelOwnership.Borrowed)
            val bounds = child.read("bounds", BoundingBox.CODEC).getOrNull()

            this.add(id, this.resolve(id, dimension), ownership, bounds)
        }
    }

    private fun resolve(id: Identifier, dimension: ResourceKey<Level>): ServerLevel {
        val loaded = this.minigame.server.getLevel(dimension)
        if (loaded != null) {
            return loaded
        }
        return this.minigame.server.loadCustomLevel(dimension) ?: throw MinigameSerializationException(
            "Minigame ${this.minigame.id} cannot restore level $id, dimension ${dimension.identifier()} doesn't exist"
        )
    }

    private fun demote(entry: Entry) {
        if (entry.ownership != LevelOwnership.Borrowed) {
            val demoted = Entry(entry.id, entry.level, LevelOwnership.Borrowed, entry.bounds)
            this.levels[entry.id] = demoted
            this.entries[entry.level] = demoted
        }
    }

    private fun ensureLoaded(level: ServerLevel) {
        if (level is CustomLevel && !this.minigame.server.hasCustomLevel(level)) {
            this.minigame.server.addCustomLevel(level)
        }
    }

    private fun validate(id: Identifier, level: ServerLevel, ownership: LevelOwnership) {
        if (ownership == LevelOwnership.Borrowed) {
            return
        }
        if (level !is CustomLevel) {
            ArcadeUtils.logger.warn("Minigame ${this.minigame.id} added non-custom level $id as $ownership")
            return
        }

        val persistence = level.persistence
        if (!persistence.shouldSave() || persistence == LevelPersistence.Persistent) {
            ArcadeUtils.logger.warn("Minigame ${this.minigame.id} added level $id as $ownership, but it is $persistence")
            return
        }
        if (persistence.shouldDeleteOnRemove() && ownership == LevelOwnership.Owned) {
            ArcadeUtils.logger.warn(
                "Minigame ${this.minigame.id} added level $id as $ownership, but it is $persistence, ${LevelPersistence.Permanent} should be used instead"
            )
        }
    }

    private class Entry(
        val id: Identifier,
        val level: ServerLevel,
        val ownership: LevelOwnership,
        val bounds: BoundingBox?
    )

    public enum class LevelOwnership: StringRepresentable {
        Borrowed,
        Owned,
        Exclusive;

        public fun shouldUnloadOnClose(): Boolean {
            return this != Borrowed
        }

        public fun shouldDeleteOnClose(): Boolean {
            return this == Exclusive
        }

        public fun persistence(): LevelPersistence {
            return when (this) {
                Borrowed -> LevelPersistence.Persistent
                else -> LevelPersistence.Permanent
            }
        }

        override fun getSerializedName(): String {
            return this.name.lowercase()
        }

        public companion object {
            @JvmField
            public val CODEC: Codec<LevelOwnership> = StringRepresentable.fromEnum(LevelOwnership::values)
        }
    }

    public interface SpawnLocation {
        public val overridesPlayerSpawnPoint: Boolean
            get() = false

        public fun get(player: ServerPlayer): LocationWithLevel<ServerLevel>?

        public companion object {
            public fun global(
                location: LocationWithLevel<ServerLevel>? = null,
                overridesPlayerSpawnPoint: Boolean = false
            ): SpawnLocation {
                return object: SpawnLocation {
                    override val overridesPlayerSpawnPoint: Boolean = overridesPlayerSpawnPoint

                    override fun get(player: ServerPlayer): LocationWithLevel<ServerLevel>? {
                        return location
                    }
                }
            }
        }
    }
}
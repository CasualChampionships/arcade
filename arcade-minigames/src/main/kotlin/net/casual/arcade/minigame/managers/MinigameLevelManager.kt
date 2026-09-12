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
import net.casual.arcade.dimensions.utils.*
import net.casual.arcade.events.server.level.LevelEvent
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.exception.MinigameSerializationException
import net.casual.arcade.minigame.serialization.SerializableMinigame
import net.casual.arcade.minigame.utils.MinigameUtils.minigame
import net.casual.arcade.utils.ArcadeUtils
import net.casual.arcade.utils.math.location.LocationWithLevel
import net.casual.arcade.utils.serialization.codec.ArcadeExtraCodecs
import net.minecraft.core.Vec3i
import net.minecraft.resources.Identifier
import net.minecraft.resources.ResourceKey
import net.minecraft.server.MinecraftServer
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
 * Any levels that are part of your minigame should be
 * added via [add], as this allows the minigame's event
 * manager to filter out [LevelEvent]s that are not
 * relevant to your minigame.
 *
 * Levels can be added with different ownership rules which
 * determine how a minigame handles the level when closing.
 * Ownership is determined by [LevelOwnership]:
 * - [LevelOwnership.Borrowed] means that the minigame takes
 * no responsibility in removing or deleting the level after
 * the minigame is closed. This should typically only be used
 * if you are either using a vanilla dimension *or* have
 * some other manual management for your levels.
 * - [LevelOwnership.Owned] means that the minigame owns
 * responsibility for removing the level after the minigame
 * is closed, but it *doesn't* delete the level.
 * - [LevelOwnership.Exclusive] means that the minigame owns
 * *full* responsibility for removing *and* deleting the level
 * after the minigame is closed.
 *
 * Ideally minigames should have *exclusive ownership* of their
 * levels which only exist exclusively during a minigame's lifetime,
 * which allows the minigame to completely manage the level itself.
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
     * point, then the player will respawn in `minecraft:overworld`
     * at the default world spawn.
     */
    public var spawn: SpawnLocation = SpawnLocation.global()

    /**
     * This adds a level to the minigame under a specified [id].
     *
     * The [level] provided can then later be queried via [get]
     * providing the same [id]. The [id] *does not* need to match
     * [Level.dimension]'s id, but must be unique to other levels
     * added via this method. This allows for the level handler
     * to handle level serialization and re-linking after a reload.
     *
     * The [ownership] of the level determines how the minigame
     * handles the level when the minigame closes, see [LevelOwnership]
     * for specifics. The [ownership] provided must be compatible
     * with the [level] provided - for example, [LevelOwnership.Borrowed]
     * works with any [ServerLevel], whereas the other ownership types
     * require [CustomLevel]. If your minigame is serializable then the
     * level's persistence must not be [LevelPersistence.Transient].
     *
     * The provided [level] doesn't need to be already added to the
     * server via [MinecraftServer.addCustomLevel], the minigame will
     * do this automatically (if not already added) when it initializes.
     *
     * Typically [create] is preferred over [add] as it allows the
     * minigame to deal with the level creation/initialization, but
     * this method allows for more control if you need it.
     *
     * @param id The id given to the [level].
     * @param level The level to add.
     * @param ownership The minigame's ownership of the level.
     * @param bounds The bounds of the minigame in the level,
     *   leave as `null` if the minigame uses the whole level.
     * @throws IllegalArgumentException If a level under [id] already
     *   exists or if [level] has already been added (under a different id).
     * @see create
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

    /**
     * This creates a [CustomLevel] to add to this manager.
     *
     * The created level can then later be queried via [get]
     * providing the same [id]. The [id] *does not* need to match
     * [Level.dimension]'s id, but must be unique to other levels
     * added via this method. This allows for the level handler
     * to handle level serialization and re-linking after a reload.
     *
     * The [ownership] of the level determines how the minigame
     * handles the level when the minigame closes, see [LevelOwnership]
     * for specifics. Unlike [add], this function enforces that
     * the created level has proper ownership.
     *
     * The returned [CustomLevel] *is not* added to the server if the
     * minigame hasn't initialized yet.
     *
     * @param id The id given to the created level.
     * @param ownership The minigame's ownership of the level.
     * @param bounds The bounds of the minigame in the level,
     *   leave as `null` if the minigame uses the whole level.
     * @param block The builder for the level.
     * @throws IllegalArgumentException If a level under [id] already.
     * @see add
     */
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

    /**
     * This gets a [ServerLevel] from the given [id].
     *
     * @param id The id of the added level.
     * @return The level, `null` if none exists.
     */
    public fun get(id: Identifier): ServerLevel? {
        return this.levels[id]?.level
    }

    /**
     * This gets a [ServerLevel] from the given [id],
     * throwing if it was never added.
     *
     * @param id The id of the added level.
     * @return The level.
     */
    public fun require(id: Identifier): ServerLevel {
        return requireNotNull(this.get(id)) { "Minigame ${this.minigame.id} does not have level $id" }
    }

    /**
     * Gets the minigame's ownership of the provided [level].
     *
     * @param level The level to check.
     * @return The minigame's ownership, `null` if the [level]
     *   is not added to this manager.
     */
    public fun ownership(level: ServerLevel): LevelOwnership? {
        return this.entries[level]?.ownership
    }

    /**
     * Gets the bounds of the minigame of the [level].
     *
     * @param level The level to check.
     * @return The bounds, `null` if the level has no bounds
     *   or if the level is not added to this manager.
     */
    public fun bounds(level: ServerLevel): BoundingBox? {
        return this.entries[level]?.bounds
    }

    /**
     * This checks whether a given level is part of this minigame.
     *
     * @param level The level to check.
     * @return Whether the level is part of the minigame.
     */
    public fun has(level: ServerLevel): Boolean {
        return this.entries.containsKey(level)
    }

    /**
     * This checks whether a given level and position are within the minigame.
     *
     * @param level The level to check.
     * @param pos The position to check.
     * @return Whether it's within the minigame's bounds.
     */
    public fun has(level: ServerLevel, pos: Vec3i): Boolean {
        val entry = this.entries[level] ?: return false
        val bounds = entry.bounds ?: return true
        return bounds.isInside(pos)
    }

    /**
     * This checks whether a level is registered with a given [id].
     *
     * This is the [Identifier] that is provided to either [add] or
     * [create] and ***not*** [Level.dimension]'s id.
     *
     * @param id The id to check.
     * @return Whether a level with [id] exists.
     */
    public fun has(id: Identifier): Boolean {
        return this.levels.containsKey(id)
    }

    /**
     * Gets all of the [Identifier]s for added levels.
     *
     * These are the [Identifier]s that were provided to either [add] or
     * [create] and ***not*** [Level.dimension]'s id.
     *
     * @return The collection of ids.
     */
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

    internal fun debug(output: ValueOutput) {
        output.store("all", Identifier.CODEC.listOf(), this.all().map { it.dimension().identifier() })
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

        if (this.minigame !is SerializableMinigame) {
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
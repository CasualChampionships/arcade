/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.minigame.managers

import it.unimi.dsi.fastutil.objects.Reference2ObjectLinkedOpenHashMap
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet
import net.casual.arcade.dimensions.level.CustomLevel
import net.casual.arcade.dimensions.utils.addCustomLevel
import net.casual.arcade.dimensions.utils.hasCustomLevel
import net.casual.arcade.dimensions.utils.removeCustomLevel
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.utils.MinigameUtils.minigame
import net.minecraft.core.BlockPos
import net.minecraft.core.Vec3i
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.GameRules
import net.minecraft.world.level.levelgen.structure.BoundingBox

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
    private val levels = Reference2ObjectLinkedOpenHashMap<ServerLevel, BoundingBox?>()
    private val handling = ReferenceOpenHashSet<CustomLevel>()

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
    public fun add(level: ServerLevel, box: BoundingBox? = null) {
        this.levels[level] = box
        level.minigame.addMinigame(this.minigame)

        if (this.minigame.initialized) {
            this.ensureLevelLoaded(level)
        }
    }

    /**
     * Adds multiple levels to the minigame.
     *
     * See [add] for how adding levels works.
     *
     * @see add
     */
    public fun addAll(levels: Iterable<ServerLevel>) {
        for (level in levels) {
            this.add(level)
        }
    }

    /**
     * This checks whether a given level is part of this minigame.
     *
     * @param level The level to check whether is part of the minigame.
     * @return Whether the level is part of the minigame.
     */
    public fun has(level: ServerLevel): Boolean {
        return this.levels.containsKey(level)
    }

    public fun has(level: ServerLevel, pos: Vec3i): Boolean {
        if (!this.has(level)) {
            return false
        }
        val box = this.levels[level] ?: return true
        return box.isInside(pos)
    }

    /**
     * This gets all the levels that are part of the minigame.
     *
     * @return The collection of levels.
     */
    public fun all(): Collection<ServerLevel> {
        return this.levels.keys
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

    internal fun initialize() {
        for (level in this.all()) {
            this.ensureLevelLoaded(level)
        }
    }

    internal fun close() {
        for (level in this.all()) {
            level.minigame.removeMinigame(this.minigame)
        }
        for (handling in this.handling) {
            this.minigame.server.removeCustomLevel(handling)
        }
        this.levels.clear()
        this.handling.clear()
    }

    private fun ensureLevelLoaded(level: ServerLevel) {
        if (level is CustomLevel) {
            if (!this.minigame.server.hasCustomLevel(level)) {
                this.minigame.server.addCustomLevel(level)
                this.handling.add(level)
            }
        }
    }

    public interface SpawnLocation {
        public fun level(player: ServerPlayer): ServerLevel?

        public fun position(player: ServerPlayer): BlockPos?

        public companion object {
            public fun global(level: ServerLevel? = null, position: BlockPos? = null): SpawnLocation {
                return object: SpawnLocation {
                    override fun level(player: ServerPlayer): ServerLevel? {
                        return level
                    }

                    override fun position(player: ServerPlayer): BlockPos? {
                        return position
                    }
                }
            }
        }
    }
}
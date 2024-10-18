package net.casual.arcade.minigame.managers

import it.unimi.dsi.fastutil.objects.ReferenceLinkedOpenHashSet
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet
import net.casual.arcade.dimensions.level.CustomLevel
import net.casual.arcade.dimensions.utils.addCustomLevel
import net.casual.arcade.dimensions.utils.hasCustomLevel
import net.casual.arcade.dimensions.utils.removeCustomLevel
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.utils.MinigameUtils.minigame
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer

/**
 * This class manages the levels of a minigame.
 *
 * It has full support for managing [CustomLevel] instances
 * if you are using the dimensions api.
 *
 * @see Minigame.levels
 */
public class MinigameLevelManager(
    private val minigame: Minigame<*>
) {
    private val levels = ReferenceLinkedOpenHashSet<ServerLevel>()
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
    public fun add(level: ServerLevel) {
        this.levels.add(level)
        level.minigame.setMinigame(this.minigame)

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
        return this.levels.contains(level)
    }

    /**
     * This gets all the levels that are part of the minigame.
     *
     * @return The collection of levels.
     */
    public fun all(): Collection<ServerLevel> {
        return this.levels
    }

    internal fun initialize() {
        for (level in this.levels) {
            this.ensureLevelLoaded(level)
        }
    }

    internal fun close() {
        for (level in this.levels) {
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
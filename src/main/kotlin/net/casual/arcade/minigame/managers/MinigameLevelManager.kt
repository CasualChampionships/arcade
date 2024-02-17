package net.casual.arcade.minigame.managers

import net.casual.arcade.minigame.Minigame
import net.casual.arcade.utils.MinigameUtils.minigame
import net.minecraft.server.level.ServerLevel
import xyz.nucleoid.fantasy.RuntimeWorldHandle

public class MinigameLevelManager(
    private val minigame: Minigame<*>
) {
    private val handles = HashSet<RuntimeWorldHandle>()
    private val levels = HashSet<ServerLevel>()

    public var spawn: ServerLevel? = null

    /**
     * This adds a level handle to the minigame.
     *
     * This will automatically delete the level after the
     * minigame ends.
     *
     * @param handle The RuntimeWorldHandle to delete after the minigame closes.
     */
    public fun add(handle: RuntimeWorldHandle) {
        this.handles.add(handle)
        this.add(handle.asWorld())
    }

    /**
     * This adds a level to the minigame.
     *
     * @param level The level to add.
     */
    public fun add(level: ServerLevel) {
        this.levels.add(level)
        level.minigame.setMinigame(this.minigame)
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

    internal fun deleteHandles() {
        for (handle in this.handles) {
            handle.delete()
        }
    }

    internal fun clear() {
        this.levels.clear()
        this.handles.clear()
    }
}
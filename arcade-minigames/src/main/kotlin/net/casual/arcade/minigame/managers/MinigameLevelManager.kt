package net.casual.arcade.minigame.managers

import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.SingleListenerProvider
import net.casual.arcade.events.server.ServerStoppingEvent
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.utils.MinigameUtils.minigame
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import xyz.nucleoid.fantasy.RuntimeWorldHandle

/**
 * This class manages the levels of a minigame.
 *
 * This also handles any [RuntimeWorldHandle]'s from
 * fantasy if you are using them and will close them
 * when the minigame ends.
 *
 * @see Minigame.levels
 */
public class MinigameLevelManager(
    private val minigame: Minigame<*>
) {
    private val handles = HashSet<RuntimeWorldHandle>()
    private val levels = HashSet<ServerLevel>()

    private val handler = SingleListenerProvider.of<ServerStoppingEvent> { this.deleteHandles() }

    init {
        GlobalEventHandler.addProvider(this.handler)
    }

    /**
     * The default spawn location for the minigame.
     *
     * If this is not set and a player dies without a respawn
     * point, then the player will respawn in the overworld
     * at the default world spawn.
     */
    public var spawn: SpawnLocation = SpawnLocation.global()

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

    internal fun unregisterHandler() {
        GlobalEventHandler.removeProvider(this.handler)
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
package net.casual.arcade.minigame.managers

import net.casual.arcade.events.minigame.MinigameAddPlayerEvent
import net.casual.arcade.events.minigame.MinigameRemovePlayerEvent
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.utils.PlayerUtils.grantAdvancementSilently
import net.casual.arcade.utils.PlayerUtils.revokeAdvancement
import net.minecraft.advancements.AdvancementHolder
import net.minecraft.advancements.AdvancementNode
import net.minecraft.advancements.AdvancementTree
import net.minecraft.advancements.TreeNodePosition
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer

/**
 * This class manages the advancements of a minigame.
 *
 * All advancements added to this manager are local to the
 * minigame only and do not exist outside the context
 * of the minigame.
 *
 * @see Minigame.advancements
 */
public class MinigameAdvancementManager(
    private val minigame: Minigame<*>
) {
    private val tree = AdvancementTree()

    init {
        this.minigame.events.register<MinigameAddPlayerEvent> { event ->
            this.reloadFor(event.player)
        }
        this.minigame.events.register<MinigameRemovePlayerEvent> { event ->
            this.unloadFor(event.player)
        }
    }

    /**
     * This adds a collection of advancements to the minigame.
     *
     * @param advancements The advancements to add.
     */
    public fun addAll(advancements: Collection<AdvancementHolder>) {
        this.tree.addAll(advancements)

        for (node in this.tree.roots()) {
            if (node.holder().value().display().isPresent) {
                TreeNodePosition.run(node)
            }
        }
    }

    /**
     * This adds an advancement to the minigame.
     *
     * @param advancement The advancement to add.
     */
    public fun add(advancement: AdvancementHolder) {
        this.tree.addAll(listOf(advancement))
        val node = this.tree.get(advancement) ?: return
        TreeNodePosition.run(node.root())
    }

    /**
     * This gets an advancement by its [ResourceLocation].
     *
     * @param id The [ResourceLocation] of the advancement.
     * @return The advancement or null if it does not exist.
     */
    public fun get(id: ResourceLocation): AdvancementHolder? {
        return this.getNode(id)?.holder()
    }

    public fun getNode(id: ResourceLocation): AdvancementNode? {
        return this.tree.get(id)
    }

    public fun all(): Collection<AdvancementHolder> {
        return this.tree.nodes().map { it.holder() }
    }

    public fun reloadFor(player: ServerPlayer) {
        val advancements = this.minigame.data.getAdvancements(player)
        for (advancement in advancements) {
            player.grantAdvancementSilently(advancement)
        }
    }

    private fun unloadFor(player: ServerPlayer) {
        for (advancement in this.tree.nodes()) {
            player.revokeAdvancement(advancement.holder())
        }
    }
}
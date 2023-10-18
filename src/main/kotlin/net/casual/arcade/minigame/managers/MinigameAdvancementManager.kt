package net.casual.arcade.minigame.managers

import net.casual.arcade.events.minigame.MinigameCloseEvent
import net.casual.arcade.events.server.ServerAdvancementReloadEvent
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.utils.AdvancementUtils.addAdvancement
import net.casual.arcade.utils.AdvancementUtils.removeAdvancement
import net.minecraft.advancements.Advancement

public class MinigameAdvancementManager(
    private val minigame: Minigame<*>
) {
    private val advancements = ArrayList<Advancement>()

    init {
        this.minigame.events.register<ServerAdvancementReloadEvent> {
            it.addAll(this.advancements)
        }
        this.minigame.events.register<MinigameCloseEvent> {
            this.removeAll()
        }
    }

    public fun add(advancement: Advancement) {
        if (this.advancements.add(advancement)) {
            this.minigame.server.advancements.addAdvancement(advancement)
        }
    }

    public fun remove(advancement: Advancement) {
        if (this.advancements.remove(advancement)) {
            this.minigame.server.advancements.removeAdvancement(advancement)
        }
    }

    public fun removeAll() {
        for (advancement in this.advancements) {
            this.minigame.server.advancements.removeAdvancement(advancement)
        }
        this.advancements.clear()
    }
}
package net.casual.arcade.minigame.managers

import net.casual.arcade.events.minigame.MinigameCloseEvent
import net.casual.arcade.events.server.ServerAdvancementReloadEvent
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.utils.AdvancementUtils.addAdvancement
import net.casual.arcade.utils.AdvancementUtils.addAllAdvancements
import net.casual.arcade.utils.AdvancementUtils.removeAdvancement
import net.minecraft.advancements.AdvancementHolder
import net.minecraft.resources.ResourceLocation

public class MinigameAdvancementManager(
    private val minigame: Minigame<*>
) {
    private val advancements = LinkedHashMap<ResourceLocation, AdvancementHolder>()

    init {
        this.minigame.events.register<ServerAdvancementReloadEvent> {
            it.addAll(this.advancements.values)
        }
        this.minigame.events.register<MinigameCloseEvent> {
            this.removeAll()
        }
    }

    public fun addAll(advancements: Collection<AdvancementHolder>) {
        var modified = false
        for (advancement in advancements) {
            if (this.advancements.put(advancement.id, advancement) != advancement) {
                modified = true
            }
        }
        if (modified) {
            this.minigame.server.advancements.addAllAdvancements(advancements)
        }
    }

    public fun add(advancement: AdvancementHolder) {
        if (this.advancements.put(advancement.id, advancement) != advancement) {
            this.minigame.server.advancements.addAdvancement(advancement)
        }
    }

    public fun get(id: ResourceLocation): AdvancementHolder? {
        return this.advancements[id]
    }

    public fun remove(advancement: AdvancementHolder) {
        if (this.advancements.remove(advancement.id) != null) {
            this.minigame.server.advancements.removeAdvancement(advancement)
        }
    }

    public fun removeAll() {
        for (advancement in this.advancements.values) {
            this.minigame.server.advancements.removeAdvancement(advancement)
        }
        this.advancements.clear()
    }

    public fun all(): Collection<AdvancementHolder> {
        return this.advancements.values
    }
}
package net.casualuhc.arcade.events.server

import net.casualuhc.arcade.events.core.Event
import net.minecraft.advancements.Advancement
import net.minecraft.server.ServerAdvancementManager
import net.minecraft.server.packs.resources.ResourceManager
import java.util.*

data class ServerAdvancementReloadEvent(
    val advancementManager: ServerAdvancementManager,
    val resourceManager: ResourceManager
): Event {
    private val advancements = LinkedList<Advancement>()

    fun add(advancement: Advancement) {
        this.advancements.add(advancement)
    }

    fun addAll(advancements: Collection<Advancement>) {
        this.advancements.addAll(advancements)
    }

    fun getAdvancements(): List<Advancement> {
        return this.advancements
    }
}
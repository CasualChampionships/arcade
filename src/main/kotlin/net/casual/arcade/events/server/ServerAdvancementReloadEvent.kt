package net.casual.arcade.events.server

import net.minecraft.advancements.Advancement
import net.minecraft.server.ServerAdvancementManager
import net.minecraft.server.packs.resources.ResourceManager
import java.util.*

public data class ServerAdvancementReloadEvent(
    val advancementManager: ServerAdvancementManager,
    val resourceManager: ResourceManager
): SafeServerlessEvent {
    private val advancements = LinkedList<Advancement>()

    public fun add(advancement: Advancement) {
        this.advancements.add(advancement)
    }

    public fun addAll(advancements: Collection<Advancement>) {
        this.advancements.addAll(advancements)
    }

    public fun getAdvancements(): List<Advancement> {
        return this.advancements
    }
}
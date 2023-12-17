package net.casual.arcade.events.server

import net.minecraft.advancements.AdvancementHolder
import net.minecraft.server.ServerAdvancementManager
import net.minecraft.server.packs.resources.ResourceManager
import java.util.*

public data class ServerAdvancementReloadEvent(
    val advancementManager: ServerAdvancementManager,
    val resourceManager: ResourceManager
): SafeServerlessEvent {
    private val advancements = LinkedList<AdvancementHolder>()

    public fun add(advancement: AdvancementHolder) {
        this.advancements.add(advancement)
    }

    public fun addAll(advancements: Collection<AdvancementHolder>) {
        this.advancements.addAll(advancements)
    }

    public fun getAdvancements(): List<AdvancementHolder> {
        return this.advancements
    }
}
package net.casualuhc.arcade.advancements

import net.casualuhc.arcade.Arcade
import net.minecraft.advancements.Advancement
import java.util.function.Consumer

object AdvancementHandler {
    private val customAdvancements = LinkedHashSet<Advancement>()

    @JvmStatic
    fun register(advancement: Advancement) {
        val manager = Arcade.server.advancements as MutableAdvancements
        manager.addAdvancement(advancement)
        this.customAdvancements.add(advancement)
    }

    @JvmStatic
    fun isCustom(advancement: Advancement): Boolean {
        return this.customAdvancements.contains(advancement)
    }

    @JvmStatic
    fun forEachCustom(consumer: Consumer<Advancement>) {
        this.customAdvancements.forEach(consumer)
    }
}
package net.casualuhc.arcade.advancements

import net.minecraft.advancements.Advancement
import net.minecraft.advancements.FrameType
import net.minecraft.advancements.critereon.ImpossibleTrigger
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.ItemLike

object AdvancementFactory {
    @JvmStatic
    fun create(
        id: ResourceLocation,
        icon: ItemLike,
        title: Component,
        desc: Component,
        frame: FrameType,
        toast: Boolean,
        announce: Boolean,
        hidden: Boolean
    ): Advancement {
        return Advancement.Builder.advancement().display(
            icon, title, desc, null, frame, toast, announce, hidden
        ).addCriterion("impossible", ImpossibleTrigger.TriggerInstance()).build(id)
    }
}
package net.casual.arcade.utils

import net.casual.arcade.advancements.AdvancementBuilder
import net.casual.arcade.ducks.`Arcade$MutableAdvancements`
import net.minecraft.advancements.Advancement
import net.minecraft.advancements.AdvancementHolder
import net.minecraft.advancements.DisplayInfo
import net.minecraft.network.chat.Component
import net.minecraft.server.ServerAdvancementManager
import java.util.*

public object AdvancementUtils {
    public fun AdvancementBuilder.setTitleAndDesc(key: String, descKey: String = "desc") {
        this.title = Component.translatable(key)
        this.description = Component.translatable("$key.$descKey")
    }

    public fun AdvancementHolder.copyWithoutToast(): AdvancementHolder {
        val advancement = this.value
        if (advancement.display.isEmpty) {
            return this
        }
        val display = advancement.display.get()
        if (!display.shouldShowToast()) {
            return this
        }

        val withoutToast = DisplayInfo(
            display.icon,
            display.title,
            display.description,
            display.background,
            display.type,
            false,
            display.shouldAnnounceChat(),
            display.isHidden
        )
        val copy = Advancement(
            advancement.parent,
            Optional.of(withoutToast),
            advancement.rewards,
            advancement.criteria,
            advancement.requirements,
            advancement.sendsTelemetryEvent,
            advancement.name
        )
        return AdvancementHolder(this.id, copy)
    }

    public fun ServerAdvancementManager.addAllAdvancements(advancements: Collection<AdvancementHolder>) {
        (this as `Arcade$MutableAdvancements`).`arcade$addAllAdvancements`(advancements)
    }

    public fun ServerAdvancementManager.addAdvancement(advancement: AdvancementHolder) {
        (this as `Arcade$MutableAdvancements`).`arcade$addAdvancement`(advancement)
    }

    public fun ServerAdvancementManager.removeAdvancement(advancement: AdvancementHolder) {
        (this as `Arcade$MutableAdvancements`).`arcade$removeAdvancement`(advancement)
    }
}
package net.casual.arcade.visuals.elements.component

import it.unimi.dsi.fastutil.objects.Object2IntMaps
import net.casual.arcade.utils.ComponentUtils.join
import net.casual.arcade.visuals.elements.LevelSpecificElement
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.MobCategory

public object MobcapComponentElement: LevelSpecificElement<Component> {
    override fun get(level: ServerLevel): Component {
        val counts = level.chunkSource.lastSpawnState?.mobCategoryCounts ?: Object2IntMaps.emptyMap()
        return MobCategory.entries.map {
            Component.literal("${counts.getInt(it)}").withStyle(this.getColorForCategory(it))
        }.join(Component.literal(" | "))
    }

    private fun getColorForCategory(category: MobCategory): ChatFormatting {
        return when (category) {
            MobCategory.MONSTER -> ChatFormatting.DARK_RED
            MobCategory.CREATURE -> ChatFormatting.DARK_GREEN
            MobCategory.AMBIENT -> ChatFormatting.GREEN
            MobCategory.AXOLOTLS -> ChatFormatting.LIGHT_PURPLE
            MobCategory.UNDERGROUND_WATER_CREATURE -> ChatFormatting.AQUA
            MobCategory.WATER_CREATURE -> ChatFormatting.DARK_BLUE
            MobCategory.WATER_AMBIENT -> ChatFormatting.DARK_AQUA
            MobCategory.MISC -> ChatFormatting.WHITE
        }
    }
}
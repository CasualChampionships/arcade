package net.casual.arcade.border.extensions

import net.casual.arcade.Arcade
import net.casual.arcade.border.SerializableBorder
import net.casual.arcade.extensions.DataExtension
import net.casual.arcade.scheduler.GlobalTickedScheduler
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.Tag
import net.minecraft.server.level.ServerLevel

public class BorderSerializerExtension(
    private val level: ServerLevel
): DataExtension {
    override fun getName(): String {
        return "${Arcade.MOD_ID}_border_serializer"
    }

    override fun serialize(): Tag {
        return (this.level.worldBorder as SerializableBorder).serialize()
    }

    override fun deserialize(element: Tag) {
        element as CompoundTag
        GlobalTickedScheduler.later {
            (this.level.worldBorder as SerializableBorder).deserialize(element)
        }
    }
}
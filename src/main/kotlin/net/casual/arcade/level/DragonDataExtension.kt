package net.casual.arcade.level

import com.mojang.serialization.Dynamic
import net.casual.arcade.Arcade
import net.casual.arcade.extensions.DataExtension
import net.minecraft.Util
import net.minecraft.nbt.EndTag
import net.minecraft.nbt.NbtOps
import net.minecraft.nbt.Tag
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.Level
import net.minecraft.world.level.dimension.end.EndDragonFight
import java.lang.IllegalStateException
import kotlin.jvm.optionals.getOrNull

internal class DragonDataExtension(
    private val level: ServerLevel
): DataExtension {
    private var data: EndDragonFight.Data? = null

    fun getDataOrDefault(): EndDragonFight.Data {
        return this.data ?: EndDragonFight.Data.DEFAULT
    }

    override fun getName(): String {
        return "${Arcade.MOD_ID}_dragon_data_extension"
    }

    override fun serialize(): Tag? {
        val fight = this.level.dragonFight
        // We let vanilla handle the default end dimension.
        if (fight == null || this.level.dimension() == Level.END) {
            return null
        }

        val result = EndDragonFight.Data.CODEC.encodeStart(NbtOps.INSTANCE, fight.saveData())
        return Util.getOrThrow(result, ::IllegalStateException)
    }

    override fun deserialize(element: Tag) {
        this.data = Dynamic(NbtOps.INSTANCE, element)
            .read(EndDragonFight.Data.CODEC)
            .result()
            .getOrNull()
    }
}
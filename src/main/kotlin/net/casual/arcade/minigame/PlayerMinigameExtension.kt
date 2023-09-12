package net.casual.arcade.minigame

import net.casual.arcade.Arcade
import net.casual.arcade.extensions.DataExtension
import net.casual.arcade.scheduler.GlobalTickedScheduler
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.Tag
import net.minecraft.server.level.ServerPlayer

internal class PlayerMinigameExtension(
    private val owner: ServerPlayer
): DataExtension {
    private var minigame: Minigame<*>? = null

    internal fun getMinigame(): Minigame<*>? {
        return this.minigame
    }

    internal fun setMinigame(minigame: Minigame<*>) {
        this.minigame?.removePlayer(this.owner)
        this.minigame = minigame
    }

    internal fun removeMinigame() {
        this.minigame = null
    }

    override fun getName(): String {
        return "${Arcade.MOD_ID}_minigame_extension"
    }

    override fun serialize(): Tag {
        val tag = CompoundTag()
        val minigame = this.minigame
        if (minigame != null) {
            tag.putUUID("minigame", minigame.uuid)
        }
        return tag
    }

    override fun deserialize(element: Tag) {
        element as CompoundTag
        if (element.hasUUID("minigame")) {
            val uuid = element.getUUID("minigame")
            this.minigame = Minigames.get(uuid)

            // Player has not fully initialized yet...
            GlobalTickedScheduler.later {
                this.minigame?.addPlayer(this.owner)
            }
        }
    }
}
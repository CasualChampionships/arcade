package net.casual.arcade.minigame

import net.casual.arcade.extensions.DataExtension
import net.casual.arcade.extensions.Extension
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.Tag
import net.minecraft.server.level.ServerPlayer
import kotlin.math.min

class PlayerMinigameExtension(
    private val owner: ServerPlayer
): DataExtension {
    private var minigame: Minigame? = null

    internal fun getMinigame(): Minigame? {
        return this.minigame
    }

    internal fun setMinigame(minigame: Minigame) {
        this.minigame?.removePlayer(this.owner)
        this.minigame = minigame
    }

    internal fun removeMinigame() {
        this.minigame = null
    }

    override fun getName(): String {
        return "Arcade_MinigameExtension"
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
            this.minigame?.addPlayer(this.owner)
        }
    }
}
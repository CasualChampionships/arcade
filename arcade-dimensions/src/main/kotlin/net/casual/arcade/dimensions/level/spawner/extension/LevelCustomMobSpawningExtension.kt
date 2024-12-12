package net.casual.arcade.dimensions.level.spawner.extension

import net.casual.arcade.dimensions.level.spawner.CustomMobSpawningRules
import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.ListenerRegistry.Companion.register
import net.casual.arcade.extensions.DataExtension
import net.casual.arcade.extensions.event.LevelExtensionEvent
import net.casual.arcade.utils.ArcadeUtils
import net.minecraft.nbt.NbtOps
import net.minecraft.nbt.Tag
import org.jetbrains.annotations.ApiStatus.Internal

@Internal
public class LevelCustomMobSpawningExtension: DataExtension {
    public var rules: CustomMobSpawningRules? = null

    override fun getName(): String {
        return "${ArcadeUtils.MOD_ID}_custom_mob_spawning_extension"
    }

    override fun serialize(): Tag? {
        val rules = this.rules ?: return null
        return CustomMobSpawningRules.CODEC.encodeStart(NbtOps.INSTANCE, rules).orThrow
    }

    override fun deserialize(element: Tag) {
        val result = CustomMobSpawningRules.CODEC.parse(NbtOps.INSTANCE, element).result()
        if (result.isPresent) {
            this.rules = result.get()
        }
    }

    public companion object {
        internal fun registerEvents() {
            GlobalEventHandler.Server.register<LevelExtensionEvent> { event ->
                event.addExtension(LevelCustomMobSpawningExtension())
            }
        }
    }
}
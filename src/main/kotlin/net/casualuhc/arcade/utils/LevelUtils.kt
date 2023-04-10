package net.casualuhc.arcade.utils

import io.netty.util.internal.shaded.org.jctools.queues.MessagePassingQueue.Consumer
import net.casualuhc.arcade.Arcade
import net.casualuhc.arcade.extensions.Extension
import net.casualuhc.arcade.extensions.ExtensionHolder
import net.casualuhc.arcade.utils.ExtensionUtils.addExtension
import net.casualuhc.arcade.utils.ExtensionUtils.getExtension
import net.casualuhc.arcade.utils.ExtensionUtils.getExtensions
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.Level

@Suppress("unused")
object LevelUtils {
    @JvmStatic
    fun overworld(): ServerLevel {
        return Arcade.server.overworld()
    }

    @JvmStatic
    fun nether(): ServerLevel {
        return Arcade.server.getLevel(Level.NETHER)!!
    }

    @JvmStatic
    fun end(): ServerLevel {
        return Arcade.server.getLevel(Level.END)!!
    }

    @JvmStatic
    fun levels(): Iterable<ServerLevel> {
        return Arcade.server.allLevels
    }

    @JvmStatic
    fun forEachLevel(consumer: Consumer<ServerLevel>) {
        for (level in this.levels()) {
            consumer.accept(level)
        }
    }

    @JvmStatic
    fun ServerLevel.addExtension(extension: Extension) {
        (this as ExtensionHolder).addExtension(extension)
    }

    @JvmStatic
    fun <T: Extension> ServerLevel.getExtension(type: Class<T>): T {
        return (this as ExtensionHolder).getExtension(type)
    }

    @JvmStatic
    fun ServerLevel.getExtensions(): Collection<Extension> {
        return (this as ExtensionHolder).getExtensions()
    }
}
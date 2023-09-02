package net.casual.arcade.utils

import io.netty.util.internal.shaded.org.jctools.queues.MessagePassingQueue.Consumer
import net.casual.arcade.Arcade
import net.casual.arcade.extensions.Extension
import net.casual.arcade.extensions.ExtensionHolder
import net.casual.arcade.utils.ExtensionUtils.addExtension
import net.casual.arcade.utils.ExtensionUtils.getExtension
import net.casual.arcade.utils.ExtensionUtils.getExtensions
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.Level

@Suppress("unused")
object LevelUtils {
    @JvmStatic
    fun overworld(): ServerLevel {
        return Arcade.getServer().overworld()
    }

    @JvmStatic
    fun nether(): ServerLevel {
        return Arcade.getServer().getLevel(Level.NETHER)!!
    }

    @JvmStatic
    fun end(): ServerLevel {
        return Arcade.getServer().getLevel(Level.END)!!
    }

    @JvmStatic
    fun levels(): Iterable<ServerLevel> {
        return Arcade.getServer().allLevels
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
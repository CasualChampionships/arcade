package net.casual.arcade.utils

import io.netty.util.internal.shaded.org.jctools.queues.MessagePassingQueue.Consumer
import net.casual.arcade.Arcade
import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.level.LevelExtensionEvent
import net.casual.arcade.extensions.Extension
import net.casual.arcade.extensions.ExtensionHolder
import net.casual.arcade.level.DragonDataExtension
import net.casual.arcade.level.VanillaDimension
import net.casual.arcade.level.VanillaLikeLevel
import net.casual.arcade.utils.ExtensionUtils.addExtension
import net.casual.arcade.utils.ExtensionUtils.getExtension
import net.casual.arcade.utils.ExtensionUtils.getExtensions
import net.minecraft.resources.ResourceKey
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.Level

public object LevelUtils {
    @JvmStatic
    public fun overworld(): ServerLevel {
        return Arcade.getServer().overworld()
    }

    @JvmStatic
    public fun nether(): ServerLevel {
        return level(Level.NETHER)
    }

    @JvmStatic
    public fun end(): ServerLevel {
        return level(Level.END)
    }

    @JvmStatic
    public fun level(key: ResourceKey<Level>): ServerLevel {
        return Arcade.getServer().getLevel(key)!!
    }

    @JvmStatic
    public fun levels(): Iterable<ServerLevel> {
        return Arcade.getServer().allLevels
    }

    @JvmStatic
    public fun forEachLevel(consumer: Consumer<ServerLevel>) {
        for (level in this.levels()) {
            consumer.accept(level)
        }
    }

    @JvmStatic
    public fun getLikeDimension(level: Level): ResourceKey<Level> {
        if (level is VanillaLikeLevel) {
            return level.vanilla.key
        }
        return level.dimension()
    }

    @JvmStatic
    @Deprecated("")
    public fun getNetherOppositeDimension(level: Level, default: ResourceKey<Level>): ResourceKey<Level>? {
        if (level is VanillaLikeLevel) {
            return when (level.vanilla) {
                VanillaDimension.Overworld -> level.others.nether
                VanillaDimension.Nether -> level.others.overworld
                VanillaDimension.End -> null
            }
        }
        return default
    }

    @JvmStatic
    @Deprecated("")
    public fun getEndOppositeDimension(level: Level, default: ResourceKey<Level>): ResourceKey<Level>? {
        if (level is VanillaLikeLevel) {
            return when (level.vanilla) {
                VanillaDimension.Overworld -> level.others.end
                VanillaDimension.End -> level.others.overworld
                VanillaDimension.Nether -> null
            }
        }
        return default
    }

    @JvmStatic
    public fun getReplacementDimensionFor(level: Level, original: ResourceKey<Level>): ResourceKey<Level>? {
        if (level is VanillaLikeLevel) {
            return when (original) {
                Level.OVERWORLD -> level.others.overworld
                Level.NETHER -> level.others.nether
                Level.END -> level.others.end
                else -> null
            }
        }
        return original
    }

    @JvmStatic
    public fun getOverworldDimensionFor(level: Level, fallback: ResourceKey<Level>): ResourceKey<Level>? {
        if (level is VanillaLikeLevel) {
            return level.others.overworld
        }
        return fallback
    }

    @JvmStatic
    public fun getNetherDimensionFor(level: Level, fallback: ResourceKey<Level>): ResourceKey<Level>? {
        if (level is VanillaLikeLevel) {
            return level.others.nether
        }
        return fallback
    }

    @JvmStatic
    public fun getEndDimensionFor(level: Level, fallback: ResourceKey<Level>): ResourceKey<Level>? {
        if (level is VanillaLikeLevel) {
            return level.others.end
        }
        return fallback
    }

    @JvmStatic
    public fun ServerLevel.addExtension(extension: Extension) {
        (this as ExtensionHolder).addExtension(extension)
    }

    @JvmStatic
    public fun <T: Extension> ServerLevel.getExtension(type: Class<T>): T {
        return (this as ExtensionHolder).getExtension(type)
    }

    @JvmStatic
    public fun ServerLevel.getExtensions(): Collection<Extension> {
        return (this as ExtensionHolder).getExtensions()
    }

    internal fun registerEvents() {
        GlobalEventHandler.register<LevelExtensionEvent> { (level) ->
            level.addExtension(DragonDataExtension(level))
        }
    }
}
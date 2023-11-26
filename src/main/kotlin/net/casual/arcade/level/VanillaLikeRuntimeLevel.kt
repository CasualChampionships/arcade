package net.casual.arcade.level

import net.minecraft.resources.ResourceKey
import net.minecraft.server.MinecraftServer
import net.minecraft.world.level.Level
import xyz.nucleoid.fantasy.RuntimeWorld
import xyz.nucleoid.fantasy.RuntimeWorldConfig

public class VanillaLikeRuntimeLevel(
    server: MinecraftServer,
    registryKey: ResourceKey<Level>,
    config: RuntimeWorldConfig,
    style: Style,
    override val vanilla: VanillaDimension,
    override val others: VanillaLikeDimensions
): RuntimeWorld(server, registryKey, config, style), VanillaLikeLevel {
    public companion object {
        public fun constructor(vanilla: VanillaDimension, others: VanillaLikeDimensions): Constructor {
            return Constructor { server, key, config, style ->
                VanillaLikeRuntimeLevel(server, key, config, style, vanilla, others)
            }
        }
    }
}
/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.replay.compat

import com.google.common.collect.HashMultimap
import net.fabricmc.loader.api.FabricLoader
import org.objectweb.asm.tree.ClassNode
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin
import org.spongepowered.asm.mixin.extensibility.IMixinInfo

public class ReplayMixinConfig: IMixinConfigPlugin {
    private companion object {
        const val MIXIN_COMPAT = "net.casual.arcade.replay.mixins.compat."

        val incompatible: HashMultimap<String, String> = HashMultimap.create<String, String>()

        init {
            incompatible.put("net.casual.arcade.replay.mixins.chunk.ChunkMapMixin", "c2me")
            incompatible.put("net.casual.arcade.replay.mixins.chunk.ChunkHolderMixin", "c2me")
        }
    }

    override fun onLoad(mixinPackage: String?) {

    }

    override fun getRefMapperConfig(): String? {
        return null
    }

    override fun shouldApplyMixin(targetClassName: String, mixinClassName: String): Boolean {
        if (mixinClassName.startsWith(MIXIN_COMPAT)) {
            val modId = mixinClassName.removePrefix(MIXIN_COMPAT).substringBefore('.')
            return FabricLoader.getInstance().isModLoaded(modId)
        }
        for (modId in incompatible.get(mixinClassName)) {
            if (FabricLoader.getInstance().isModLoaded(modId)) {
                return false
            }
        }
        return true
    }

    override fun acceptTargets(myTargets: MutableSet<String>, otherTargets: MutableSet<String>) {

    }

    override fun getMixins(): MutableList<String>? {
        return null
    }

    override fun preApply(
        targetClassName: String,
        targetClass: ClassNode,
        mixinClassName: String,
        mixinInfo: IMixinInfo
    ) {

    }

    override fun postApply(
        targetClassName: String,
        targetClass: ClassNode,
        mixinClassName: String,
        mixinInfo: IMixinInfo
    ) {

    }
}
/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.extensions.compat

import net.fabricmc.loader.api.FabricLoader
import org.objectweb.asm.tree.ClassNode
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin
import org.spongepowered.asm.mixin.extensibility.IMixinInfo

public class ExtensionsMixinConfig: IMixinConfigPlugin {
    private companion object {
        const val MIXIN_COMPAT = "net.casual.arcade.extensions.mixins.compat."
    }

    override fun onLoad(mixinPackage: String?) {

    }

    override fun getRefMapperConfig(): String? {
        return null
    }

    override fun shouldApplyMixin(targetClassName: String, mixinClassName: String): Boolean {
        if (mixinClassName.startsWith(MIXIN_COMPAT)) {
            var modId = mixinClassName.removePrefix(MIXIN_COMPAT).substringBefore('.')
            if (modId == "polymer") {
                modId = "polymer-core"
            }
            return FabricLoader.getInstance().isModLoaded(modId)
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
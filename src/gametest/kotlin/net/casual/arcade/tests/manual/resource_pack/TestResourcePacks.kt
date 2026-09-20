package net.casual.arcade.tests.manual.resource_pack

import com.google.common.collect.HashMultimap
import com.google.common.collect.Multiset
import net.casual.arcade.pack.generation.PackDefinition
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.network.chat.Component

object TestResourcePacks {
    private val registered = HashMultimap.create<String, PackDefinition>()

    fun register(name: String, vararg packs: PackDefinition) {
        this.registered.putAll(name, packs.toList())
    }

    fun names(): Multiset<String> {
        return this.registered.keys()
    }

    fun pop(name: String): Set<PackDefinition> {
        return this.registered.removeAll(name)
    }
}

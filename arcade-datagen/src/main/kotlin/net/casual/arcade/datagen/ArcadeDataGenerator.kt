/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.datagen

import joptsimple.OptionParser
import joptsimple.OptionSpec
import net.casual.arcade.datagen.resource.ArcadeResourceGenerator
import net.casual.arcade.datagen.utils.LOGGER
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.client.Minecraft
import java.util.concurrent.CompletableFuture
import kotlin.system.exitProcess

public class ArcadeDataGenerator: ClientModInitializer {
    public override fun onInitializeClient() {
        val args = FabricLoader.getInstance().getLaunchArguments(true)

        val parser = OptionParser()
        val spec: OptionSpec<Void> = parser.accepts("arcade-datagen")
        parser.allowsUnrecognizedOptions()
        val options = parser.parse(*args)

        if (!options.has(spec)) {
            return
        }

        val entrypoints = FabricLoader.getInstance()
            .getEntrypoints("arcade-datagen", ArcadeResourceGenerator::class.java)

        if (entrypoints.isEmpty()) {
            LOGGER.warn("Started datagen without any entrypoints!")
            exitProcess(0)
        }

        ClientLifecycleEvents.CLIENT_STARTED.register { client ->
            this.processEntrypoint(client, entrypoints, 0)
        }
    }

    private fun processEntrypoint(
        client: Minecraft,
        entrypoints: List<ArcadeResourceGenerator>,
        index: Int
    ) {
        if (index !in entrypoints.indices) {
            client.stop()
            return
        }

        val entrypoint = entrypoints[index]
        LOGGER.info("Started datagen for ${entrypoint.id()}")
        this.loadPacksFor(client, entrypoint).thenRun {
            LOGGER.info("Loaded resource packs for ${entrypoint.id()}")
            entrypoint.run(client)
            LOGGER.info("Finished datagen for ${entrypoint.id()}")
            this.processEntrypoint(client, entrypoints, index + 1)
        }
    }

    private fun loadPacksFor(client: Minecraft, generator: ArcadeResourceGenerator): CompletableFuture<Void> {
        val packs = generator.resources().associateBy { "${it.hashCode().toString(16)}.zip" }

        for (selected in client.resourcePackRepository.selectedIds) {
            client.resourcePackRepository.removePack(selected)
        }

        for ((hash, pack) in packs) {
            val path = client.resourcePackDirectory.resolve(hash)
            pack.build(path)
            path.toFile().deleteOnExit()
        }

        client.resourcePackRepository.reload()
        for (hash in packs.keys) {
            client.resourcePackRepository.addPack("file/${hash}")
        }
        return client.reloadResourcePacks()
    }
}
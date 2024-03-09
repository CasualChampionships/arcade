package net.casual.arcade.datagen.resource

import eu.pb4.polymer.resourcepack.api.ResourcePackCreator
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents
import net.minecraft.client.Minecraft
import java.util.concurrent.CompletableFuture

public interface ResourceGenerator: ClientModInitializer {
    override fun onInitializeClient() {
        ClientLifecycleEvents.CLIENT_STARTED.register { client ->
            this.loadPack(client).thenAcceptAsync({
                this.run(client)

                // We don't really want to boot into the game...
                client.stop()
            }, client)
        }
    }

    public fun run(client: Minecraft)

    public fun resources(): Collection<ResourcePackCreator>

    private fun loadPack(client: Minecraft): CompletableFuture<Void> {
        val packs = this.resources().associateBy { it.hashCode().toString(16) }
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
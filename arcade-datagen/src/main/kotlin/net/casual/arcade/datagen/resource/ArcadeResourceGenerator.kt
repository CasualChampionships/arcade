/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.datagen.resource

import eu.pb4.polymer.resourcepack.api.ResourcePackCreator
import net.minecraft.client.Minecraft
import org.jetbrains.annotations.ApiStatus.NonExtendable
import java.nio.file.Path
import kotlin.io.path.Path

public interface ArcadeResourceGenerator {
    public fun id(): String

    public fun run(client: Minecraft)

    public fun resources(): Collection<ResourcePackCreator> {
        return listOf()
    }

    public fun getResourcesPath(): Path {
        return Path("../src/main/resources")
    }

    @NonExtendable
    public fun getDataPath(): Path {
        return this.getResourcesPath().resolve("data").resolve(this.id())
    }

    @NonExtendable
    public fun getAssetsPath(): Path {
        return this.getResourcesPath().resolve("assets").resolve(this.id())
    }
}
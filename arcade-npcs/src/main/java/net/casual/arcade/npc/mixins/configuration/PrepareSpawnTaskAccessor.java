/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.npc.mixins.configuration;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.config.PrepareSpawnTask;
import net.minecraft.server.players.NameAndId;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PrepareSpawnTask.class)
public interface PrepareSpawnTaskAccessor {
    @Accessor("state")
    PrepareSpawnTask.State arcade_getState();

    @Accessor("server")
    MinecraftServer arcade_getServer();

    @Accessor("nameAndId")
    NameAndId arcade_getNameAndId();
}

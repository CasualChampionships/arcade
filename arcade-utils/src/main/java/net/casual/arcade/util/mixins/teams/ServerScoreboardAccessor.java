/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.util.mixins.teams;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerScoreboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ServerScoreboard.class)
public interface ServerScoreboardAccessor {
    @Accessor("server")
    MinecraftServer arcade_getServer();
}

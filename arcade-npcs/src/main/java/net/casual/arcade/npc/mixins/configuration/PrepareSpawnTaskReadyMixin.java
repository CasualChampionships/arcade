/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.npc.mixins.configuration;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.authlib.GameProfile;
import net.casual.arcade.npc.configuration.FakePlayerConstructor;
import net.casual.arcade.npc.ducks.ReplaceablePlayerConstructor;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.config.PrepareSpawnTask;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PrepareSpawnTask.Ready.class)
public class PrepareSpawnTaskReadyMixin implements ReplaceablePlayerConstructor {
    @Unique private FakePlayerConstructor<?> constructor = null;

    @Override
    public void arcade$set(FakePlayerConstructor<?> constructor) {
        this.constructor = constructor;
    }

    @WrapOperation(
        method = "spawn",
        at = @At(
            value = "NEW",
            target = "(Lnet/minecraft/server/MinecraftServer;Lnet/minecraft/server/level/ServerLevel;Lcom/mojang/authlib/GameProfile;Lnet/minecraft/server/level/ClientInformation;)Lnet/minecraft/server/level/ServerPlayer;"
        )
    )
    private ServerPlayer onCreatePlayer(
        MinecraftServer server,
        ServerLevel level,
        GameProfile gameProfile,
        ClientInformation clientInformation,
        Operation<ServerPlayer> original
    ) {
        if (this.constructor == null) {
            return original.call(server, level, gameProfile, clientInformation);
        }
        return this.constructor.construct(server, level, gameProfile, clientInformation);
    }
}

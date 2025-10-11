/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.npc.configuration

import com.mojang.authlib.GameProfile
import net.casual.arcade.npc.FakePlayer
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ClientInformation
import net.minecraft.server.level.ServerLevel

public fun interface FakePlayerConstructor<T: FakePlayer> {
    public fun construct(server: MinecraftServer, level: ServerLevel, profile: GameProfile, info: ClientInformation): T

    public companion object {
        public val DEFAULT: FakePlayerConstructor<FakePlayer> = FakePlayerConstructor(::FakePlayer)
    }
}
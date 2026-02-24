/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.utils.server

import net.casual.arcade.util.ducks.CustomMOTD
import net.minecraft.network.chat.Component
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.Level
import java.util.UUID

public val MinecraftServer.players: List<ServerPlayer>
    get() = this.playerList.players

public fun MinecraftServer.player(name: String): ServerPlayer? {
    return this.playerList.getPlayerByName(name)
}

public fun MinecraftServer.player(uuid: UUID): ServerPlayer? {
    return this.playerList.getPlayer(uuid)
}

public fun MinecraftServer.nether(): ServerLevel {
    return this.getLevel(Level.NETHER)!!
}

public fun MinecraftServer.end(): ServerLevel {
    return this.getLevel(Level.END)!!
}

public fun MinecraftServer.setMessageOfTheDay(message: Component) {
    (this as CustomMOTD).arcade_setMOTD(message)
}

public fun MinecraftServer.getMessageOfTheDay(): Component {
    val custom = (this as CustomMOTD).arcade_getMOTD()
    return custom ?: Component.nullToEmpty(this.motd)
}
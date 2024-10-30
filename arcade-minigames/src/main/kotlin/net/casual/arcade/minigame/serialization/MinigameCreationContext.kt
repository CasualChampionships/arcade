package net.casual.arcade.minigame.serialization

import net.minecraft.server.MinecraftServer
import java.util.UUID

public class MinigameCreationContext(
    public val server: MinecraftServer,
    public val uuid: UUID = UUID.randomUUID()
)
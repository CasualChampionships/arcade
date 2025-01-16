package net.casual.arcade.extensions

import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity

public abstract class EntityExtension private constructor(
    private val provider: () -> Entity
): Extension {
    public val entity: Entity
        get() = this.provider.invoke()

    public constructor(entity: Entity): this(entityToProvider(entity))

    private companion object {
        fun entityToProvider(entity: Entity): () -> Entity {
            if (entity is ServerPlayer) {
                val connection = entity.connection
                return { connection.player }
            }
            return { entity }
        }
    }
}
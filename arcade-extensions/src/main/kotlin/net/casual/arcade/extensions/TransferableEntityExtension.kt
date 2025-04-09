package net.casual.arcade.extensions

import net.minecraft.world.entity.Entity

public interface TransferableEntityExtension: Extension {
    public fun transfer(entity: Entity, respawned: Boolean): Extension
}
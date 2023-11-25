package net.casual.arcade.border

import net.minecraft.nbt.CompoundTag

public interface SerializableBorder {
    public fun serialize(): CompoundTag {
        return CompoundTag()
    }

    public fun deserialize(compound: CompoundTag) {

    }
}
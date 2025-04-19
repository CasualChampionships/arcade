package net.casual.arcade.extensions.ducks;

import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.Nullable;

public interface ArcadeTeamDataHolder {
    @Nullable CompoundTag arcade$getData();

    void arcade$setData(@Nullable CompoundTag tag);
}

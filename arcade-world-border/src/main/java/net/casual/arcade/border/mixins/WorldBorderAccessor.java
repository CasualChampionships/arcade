package net.casual.arcade.border.mixins;

import net.minecraft.world.level.border.BorderChangeListener;
import net.minecraft.world.level.border.WorldBorder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(WorldBorder.class)
public interface WorldBorderAccessor {
	@Accessor("listeners")
	List<BorderChangeListener> getBorderListeners();
}

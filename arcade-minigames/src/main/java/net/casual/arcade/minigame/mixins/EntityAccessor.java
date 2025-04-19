package net.casual.arcade.minigame.mixins;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Entity.class)
public interface EntityAccessor {
    @Accessor("insideEffectCollector")
    InsideBlockEffectApplier.StepBasedCollector getInsideEffectCollector();
}

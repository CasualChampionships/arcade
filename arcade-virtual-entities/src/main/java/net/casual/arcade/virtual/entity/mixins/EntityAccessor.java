package net.casual.arcade.virtual.entity.mixins;

import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.concurrent.atomic.AtomicInteger;

@Mixin(Entity.class)
public interface EntityAccessor {
    @Accessor("ENTITY_COUNTER")
    static AtomicInteger accessEntityCounter() {
        throw new AssertionError();
    }
}

package net.casual.arcade.util.mixins.bugfixes;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.entity.EntityTickList;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

@Mixin(EntityTickList.class)
public class EntityTickListMixin {
    @Shadow private Int2ObjectMap<Entity> passive;

    @Inject(
        method = "forEach",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/world/level/entity/EntityTickList;iterated:Lit/unimi/dsi/fastutil/ints/Int2ObjectMap;",
            opcode = Opcodes.PUTFIELD,
            ordinal = 1
        )
    )
    private void onFinishIteration(Consumer<Entity> entity, CallbackInfo ci) {
        // Fix a memory leak where entities don't get cleared from this map
        this.passive.clear();
    }
}

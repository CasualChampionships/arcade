package net.casual.arcade.mixin.fantasy;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import xyz.nucleoid.fantasy.Fantasy;

@Mixin(value = Fantasy.class, remap = false)
public interface FantasyInvoker {
	@Invoker("tick")
	void doTick();
}

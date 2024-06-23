package net.casual.arcade.mixin.bugfixes;

import com.mojang.datafixers.util.Either;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Leashable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.UUID;

@Mixin(Leashable.LeashData.class)
public interface LeashDataInvoker {
	@Invoker("<init>")
	static Leashable.LeashData construct(Either<UUID, BlockPos> either) {
		throw new AssertionError();
	}
}

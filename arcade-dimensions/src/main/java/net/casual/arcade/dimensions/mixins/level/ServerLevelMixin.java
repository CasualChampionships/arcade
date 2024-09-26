package net.casual.arcade.dimensions.mixins.level;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.casual.arcade.dimensions.level.LevelGenerationOptions;
import net.casual.arcade.dimensions.utils.GenerationOptionsContext;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.WorldOptions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ServerLevel.class)
public class ServerLevelMixin {
	@ModifyExpressionValue(
		method = "<init>",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/level/storage/WorldData;worldGenOptions()Lnet/minecraft/world/level/levelgen/WorldOptions;"
		)
	)
	private WorldOptions modifyWorldOptions(WorldOptions original, MinecraftServer server) {
		LevelGenerationOptions options = GenerationOptionsContext.get(server);
		return new WorldOptions(options.getSeed(), options.getGenerateStructures(), original.generateBonusChest());
	}
}

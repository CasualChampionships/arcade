package net.casual.arcade.dimensions.mixins.level;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.casual.arcade.dimensions.level.CustomLevel;
import net.casual.arcade.dimensions.level.LevelGenerationOptions;
import net.casual.arcade.dimensions.utils.impl.DerivedLevelData;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraft.world.level.storage.WritableLevelData;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin extends Level {
	@Shadow @Final private ServerLevelData serverLevelData;

	protected ServerLevelMixin(
		WritableLevelData levelData,
		ResourceKey<Level> dimension,
		RegistryAccess registryAccess,
		Holder<DimensionType> dimensionTypeRegistration,
		boolean isClientSide,
		boolean isDebug,
		long biomeZoomSeed,
		int maxChainedNeighborUpdates
	) {
		super(levelData, dimension, registryAccess, dimensionTypeRegistration, isClientSide, isDebug, biomeZoomSeed, maxChainedNeighborUpdates);
	}

	@ModifyExpressionValue(
		method = "<init>",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/level/storage/WorldData;worldGenOptions()Lnet/minecraft/world/level/levelgen/WorldOptions;"
		)
	)
	private WorldOptions modifyWorldOptions(WorldOptions original, MinecraftServer server) {
		if ((Object) this instanceof CustomLevel) {
			LevelGenerationOptions options = ((DerivedLevelData) this.serverLevelData).getOptions();
			return new WorldOptions(options.getSeed(), options.getGenerateStructures(), original.generateBonusChest());
		}
		return original;
	}

	@Redirect(
		method = "advanceWeatherCycle",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/server/players/PlayerList;broadcastAll(Lnet/minecraft/network/protocol/Packet;)V"
		)
	)
	private void onSendWeatherPackets(PlayerList instance, Packet<?> packet) {
		instance.broadcastAll(packet, this.dimension());
	}
}

package net.casual.arcade.minigame.mixins.recipe;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.casual.arcade.minigame.Minigame;
import net.casual.arcade.minigame.utils.MinigameUtils;
import net.minecraft.network.protocol.game.ServerboundPlaceRecipePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;

@Mixin(ServerGamePacketListenerImpl.class)
public class ServerGamePacketListenerImplMixin {
	@Shadow public ServerPlayer player;

	@ModifyExpressionValue(
		method = "handlePlaceRecipe",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/item/crafting/RecipeManager;byKey(Lnet/minecraft/resources/ResourceLocation;)Ljava/util/Optional;"
		)
	)
	@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
	private Optional<RecipeHolder<?>> onModifyRecipe(Optional<RecipeHolder<?>> original, ServerboundPlaceRecipePacket packet) {
		if (original.isPresent()) {
			return original;
		}
		Minigame<?> minigame = MinigameUtils.getMinigame(this.player);
		if (minigame != null) {
			RecipeHolder<?> holder = minigame.getRecipes().get(packet.getRecipe());
			if (holder != null) {
				return Optional.of(holder);
			}
		}
		return Optional.empty();
	}
}

package net.casual.arcade.mixin.advancements;

import com.llamalad7.mixinextras.injector.WrapWithCondition;
import net.casual.arcade.ducks.Arcade$MutableAdvancements;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementNode;
import net.minecraft.advancements.AdvancementTree;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Map;

@Mixin(AdvancementTree.class)
public abstract class AdvancementTreeMixin implements Arcade$MutableAdvancements {
	@Shadow @Final private Map<ResourceLocation, AdvancementNode> nodes;
	@Unique private boolean arcade$suppressLogs = false;

	@Shadow protected abstract void remove(AdvancementNode node);

	@Shadow protected abstract boolean tryInsert(AdvancementHolder advancement);

	@Override
	public void arcade$addAdvancement(@NotNull AdvancementHolder holder) {
		this.tryInsert(holder);
	}

	@Override
	public void arcade$removeAdvancement(@NotNull AdvancementHolder advancement) {
		AdvancementNode node = this.nodes.get(advancement.id());
		if (node != null) {
			this.arcade$suppressLogs = true;
			this.remove(node);
			this.arcade$suppressLogs = false;
		}
	}

	@WrapWithCondition(
		method = "remove(Lnet/minecraft/advancements/AdvancementNode;)V",
		at = @At(
			value = "INVOKE",
			target = "Lorg/slf4j/Logger;info(Ljava/lang/String;Ljava/lang/Object;)V",
			remap = false
		)
	)
	private boolean onInfo(Logger instance, String string, Object o) {
		return !this.arcade$suppressLogs;
	}
}

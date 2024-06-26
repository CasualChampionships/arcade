package net.casual.arcade.mixin.advancements;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.casual.arcade.ducks.MutableAdvancements;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementNode;
import net.minecraft.advancements.AdvancementTree;
import net.minecraft.advancements.TreeNodePosition;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Mixin(AdvancementTree.class)
public abstract class AdvancementTreeMixin implements MutableAdvancements {
	@Shadow @Final private Map<ResourceLocation, AdvancementNode> nodes;
	@Unique private boolean arcade$suppressLogs = false;

	@Shadow protected abstract void remove(AdvancementNode node);

	@Shadow protected abstract boolean tryInsert(AdvancementHolder advancement);

	@Shadow public abstract Iterable<AdvancementNode> roots();

	@Override
	public void arcade$addAllAdvancements(Collection<AdvancementHolder> advancements) {
		List<AdvancementHolder> list = new ArrayList<>(advancements);
		while(!list.isEmpty()) {
			if (!list.removeIf(this::tryInsert)) {
				break;
			}
		}

		for (AdvancementNode advancementNode : this.roots()) {
			if (advancementNode.holder().value().display().isPresent()) {
				TreeNodePosition.run(advancementNode);
			}
		}
	}

	@Override
	public void arcade$addAdvancement(@NotNull AdvancementHolder holder) {
		this.tryInsert(holder);

		for (AdvancementNode advancementNode : this.roots()) {
			if (advancementNode.holder().value().display().isPresent()) {
				TreeNodePosition.run(advancementNode);
			}
		}
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

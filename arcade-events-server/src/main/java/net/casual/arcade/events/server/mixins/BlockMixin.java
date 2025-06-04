package net.casual.arcade.events.server.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.casual.arcade.events.GlobalEventHandler;
import net.casual.arcade.events.server.player.PlayerBlockDropEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

@Mixin(Block.class)
public class BlockMixin {
    @ModifyExpressionValue(
        method = "dropResources(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/entity/BlockEntity;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/item/ItemStack;)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/Block;getDrops(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/entity/BlockEntity;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/item/ItemStack;)Ljava/util/List;"
        )
    )
    private static List<ItemStack> onDropResources(
        List<ItemStack> original,
        BlockState state,
        Level level,
        BlockPos pos,
        @Nullable BlockEntity blockEntity,
        @Nullable Entity entity,
        ItemStack tool
    ) {
        if (entity instanceof ServerPlayer player) {
            PlayerBlockDropEvent event = new PlayerBlockDropEvent(
                player, (ServerLevel) level, state, pos, tool, original
            );
            GlobalEventHandler.Server.broadcast(event);
            return event.getDrops();
        }
        return original;
    }
}

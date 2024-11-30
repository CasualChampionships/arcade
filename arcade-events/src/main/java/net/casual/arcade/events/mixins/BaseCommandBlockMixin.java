package net.casual.arcade.events.mixins;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.casual.arcade.events.GlobalEventHandler;
import net.casual.arcade.events.block.CommandBlockExecuteEvent;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.world.level.BaseCommandBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BaseCommandBlock.class)
public class BaseCommandBlockMixin {
    @WrapWithCondition(
        method = "performCommand",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/commands/Commands;performPrefixedCommand(Lnet/minecraft/commands/CommandSourceStack;Ljava/lang/String;)V"
        )
    )
    private boolean onPerformCommand(Commands instance, CommandSourceStack source, String command) {
        CommandBlockExecuteEvent event = new CommandBlockExecuteEvent(
            source.getLevel(),
            (BaseCommandBlock) (Object) this,
            source,
            command
        );
        GlobalEventHandler.broadcast(event);
        return !event.isCancelled();
    }
}

package net.casual.arcade.mixin.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.RootCommandNode;
import net.casual.arcade.ducks.Arcade$DeletableCommand;
import net.casual.arcade.utils.ducks.DeletableCommand;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = CommandDispatcher.class, remap = false)
public class CommandDispatcherMixin<S> implements Arcade$DeletableCommand {
	@Shadow @Final private RootCommandNode<S> root;

	@Override
	public void arcade$delete(String name) {
		((DeletableCommand) this.root).delete(name);
	}
}

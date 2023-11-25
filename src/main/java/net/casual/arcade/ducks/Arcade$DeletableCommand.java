package net.casual.arcade.ducks;

import net.casual.arcade.utils.ducks.DeletableCommand;
import org.jetbrains.annotations.ApiStatus.NonExtendable;
import org.jetbrains.annotations.NotNull;

public interface Arcade$DeletableCommand extends DeletableCommand {
	void arcade$delete(String name);

	@Override
	@NonExtendable
	default void delete(@NotNull String name) {
		this.arcade$delete(name);
	}
}

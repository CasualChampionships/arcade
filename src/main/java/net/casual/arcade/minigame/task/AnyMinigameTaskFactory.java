package net.casual.arcade.minigame.task;

import net.casual.arcade.minigame.Minigame;
import net.casual.arcade.task.Task;
import net.casual.arcade.task.TaskCreationContext;
import org.jetbrains.annotations.NotNull;

/**
 * This interface extends the raw class of {@link MinigameTaskFactory},
 * this allows us to create a task with an unspecified Minigame type.
 *
 * @see MinigameTaskFactory
 */
@SuppressWarnings("rawtypes")
public interface AnyMinigameTaskFactory extends MinigameTaskFactory {
	/**
	 * This creates a {@link Task} from the given {@code context} and
	 * the task's {@code minigame} owner.
	 *
	 * @param minigame The owner of this task.
	 * @param context The task creation context.
	 * @return The generated task.
	 */
	@NotNull
	@Override
	Task create(@NotNull Minigame minigame, @NotNull TaskCreationContext context);
}

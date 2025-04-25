/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.commands.ducks;

public interface DeletableCommand {
	boolean arcade$delete(String name);
}

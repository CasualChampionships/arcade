/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.replay.mixins.rejoin;

import net.minecraft.server.network.ConfigurationTask;
import net.minecraft.server.network.ServerConfigurationPacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Queue;

@Mixin(ServerConfigurationPacketListenerImpl.class)
public interface ServerConfigurationPacketListenerImplAccessor {
    @Accessor("configurationTasks")
    Queue<ConfigurationTask> tasks();

    @Accessor("currentTask")
    void setCurrentTask(ConfigurationTask task);
}

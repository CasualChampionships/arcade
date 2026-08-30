/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.util.mixins.bugfixes;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.jtracy.TracyClient;
import com.mojang.jtracy.Zone;
import com.mojang.logging.LogUtils;
import net.minecraft.SharedConstants;
import net.minecraft.util.thread.BlockableEventLoop;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BlockableEventLoop.class)
public class BlockableEventLoopMixin {
    @Shadow
    @Final
    private static Logger LOGGER;

    // See https://mojira.dev/MC-311528
    @WrapOperation(
        method = "execute",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/util/thread/BlockableEventLoop;doRunTask(Ljava/lang/Runnable;)V"
        )
    )
    private <R extends Runnable> void onlyCallDoRunTaskOnMainThread(
        BlockableEventLoop<R> instance,
        R task,
        Operation<Void> original
    ) {
        if (instance.isSameThread()) {
            original.call(instance, task);
        } else {
            // This can technically still lead to bugs because this
            // task was intended to run on the main thread.
            // But vanilla just runs the task on the current thread
            // instead of dropping the task, so lets just keep it the same.
            doRunTaskSafely(instance, task);
        }
    }

    @Unique
    private static <R extends Runnable> void doRunTaskSafely(BlockableEventLoop<R> loop, R task) {
        try (Zone ignored = TracyClient.beginZone("Task", SharedConstants.IS_RUNNING_IN_IDE)) {
            task.run();
        } catch (Exception e) {
            LOGGER.error(LogUtils.FATAL_MARKER, "Error executing task on {}", loop.name(), e);
            if (BlockableEventLoop.isNonRecoverable(e)) {
                throw e;
            }
        }
    }
}

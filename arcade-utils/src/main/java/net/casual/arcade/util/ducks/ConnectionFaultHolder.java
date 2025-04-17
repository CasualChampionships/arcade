/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.util.ducks;

import org.jetbrains.annotations.Nullable;

public interface ConnectionFaultHolder {
    void arcade$setTimedOut(boolean timedOut);

    boolean arcade$hasTimedOut();

    void arcade$setPacketError(Throwable packetError);

    @Nullable Throwable arcade$getPacketError();
}

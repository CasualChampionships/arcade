/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.util.ducks;

import org.jetbrains.annotations.Nullable;

public interface ConnectionFaultHolder {
    void arcade_setTimeOut(boolean timedOut);

    boolean arcade_hasTimeOut();

    void arcade_setPacketError(Throwable packetError);

    @Nullable Throwable arcade_getPacketError();
}

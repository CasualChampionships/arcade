/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.host.ducks;

public interface MutableConnectionAddressHolder extends ConnectionAddressHolder {
    void arcade$setConnectionAddress(String ip, int port);
}

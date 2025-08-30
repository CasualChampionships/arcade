/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.replay.util.io

import org.apache.commons.lang3.mutable.MutableLong
import java.io.FilterOutputStream
import java.io.OutputStream

public class CounterOutputStream(
    out: OutputStream,
    private val bytes: MutableLong
): FilterOutputStream(out) {
    override fun write(b: Int) {
        super.write(b)
        this.bytes.increment()
    }
}
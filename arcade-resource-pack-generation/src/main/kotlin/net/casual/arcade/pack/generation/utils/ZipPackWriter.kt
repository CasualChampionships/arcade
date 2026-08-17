/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.pack.generation.utils

import com.google.common.hash.Hashing
import com.google.common.hash.HashingOutputStream
import net.casual.arcade.pack.generation.PackFile
import java.io.BufferedOutputStream
import java.io.ByteArrayOutputStream
import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

internal object ZipPackWriter {
    private const val BUFFER_SIZE = 65536

    @Suppress("Deprecation", "UnstableApiUsage")
    fun write(contents: Map<String, PackFile>, output: OutputStream): String {
        val hashing = HashingOutputStream(Hashing.sha1(), output)
        ZipOutputStream(BufferedOutputStream(hashing, BUFFER_SIZE)).use { zip ->
            for ((path, file) in this.sorted(contents)) {
                val entry = ZipEntry(path)
                entry.time = 0
                zip.putNextEntry(entry)
                file?.stream()?.use { stream -> stream.transferTo(zip) }
                zip.closeEntry()
            }
        }
        return hashing.hash().toString()
    }

    fun write(contents: Map<String, PackFile>): Pair<ByteArray, String> {
        val output = ByteArrayOutputStream()
        val hash = this.write(contents, output)
        return output.toByteArray() to hash
    }

    private fun sorted(contents: Map<String, PackFile>): List<Pair<String, PackFile?>> {
        val entries = HashMap<String, PackFile?>(contents)
        for (path in contents.keys) {
            var index = path.lastIndexOf('/')
            while (index > 0) {
                entries.putIfAbsent(path.substring(0, index + 1), null)
                index = path.lastIndexOf('/', index - 1)
            }
        }
        return entries.entries.sortedBy { it.key }.map { it.key to it.value }
    }
}
package net.casual.arcade.pack.generation

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import net.casual.arcade.utils.JsonUtils
import net.minecraft.SharedConstants
import net.minecraft.server.Bootstrap
import java.util.zip.ZipInputStream

internal object PackTestUtils {
    private var bootstrapped = false

    fun bootstrap() {
        if (!this.bootstrapped) {
            SharedConstants.tryDetectVersion()
            Bootstrap.bootStrap()
            this.bootstrapped = true
        }
    }

    fun pack(contents: PackContents.() -> Unit): PackDefinition {
        return PackDefinition("test", contents)
    }

    fun entries(contents: PackContents.() -> Unit): Map<String, ByteArray> {
        return this.entries(this.pack(contents).build())
    }

    fun entries(pack: ResourcePack): Map<String, ByteArray> {
        val entries = LinkedHashMap<String, ByteArray>()
        ZipInputStream(pack.stream()).use { zip ->
            while (true) {
                val entry = zip.nextEntry ?: break
                entries[entry.name] = zip.readBytes()
            }
        }
        return entries
    }

    fun Map<String, ByteArray>.text(path: String): String {
        return this.getValue(path).decodeToString()
    }

    fun Map<String, ByteArray>.json(path: String): JsonObject {
        return JsonUtils.decodeRaw<JsonObject>(this.getValue(path))
    }
}

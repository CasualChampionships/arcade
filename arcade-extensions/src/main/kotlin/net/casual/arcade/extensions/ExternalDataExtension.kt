package net.casual.arcade.extensions

import kotlinx.io.IOException
import net.casual.arcade.utils.ArcadeUtils
import net.minecraft.Util
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtIo
import net.minecraft.nbt.Tag
import org.jetbrains.annotations.ApiStatus.OverrideOnly
import java.nio.file.Path
import kotlin.io.path.createParentDirectories

public interface ExternalDataExtension {
    /**
     * This method serializes any data in your extension.
     *
     * The [Tag] that you serialize will be passed into
     * [deserialize] when deserializing.
     *
     * @return The serialized data.
     */
    @OverrideOnly
    public fun serialize(): Tag?

    /**
     * This method deserializes any data for your extension.
     *
     * The [element] that gets passed in is what you previously
     * serialized with [serialize].
     *
     * @param element The serialized data.
     */
    @OverrideOnly
    public fun deserialize(element: Tag)

    /**
     * Gets the path of the external data.
     *
     * @return The path of the external data.
     */
    @OverrideOnly
    public fun path(): Path

    public companion object {
        public fun ExternalDataExtension.read() {
            val path = this.path()
            try {
                val tag = NbtIo.read(path)?.get("") ?: return
                this.deserialize(tag)
            } catch (e: IOException)  {
                ArcadeUtils.logger.error("Failed to read external data at $path")
            }
        }

        public fun ExternalDataExtension.write() {
            val path = this.path().createParentDirectories()
            val tag = CompoundTag()
            val serialized = this.serialize()
            if (serialized != null) {
                tag.put("", serialized)
            }
            Util.ioPool().execute {
                try {
                    NbtIo.write(tag, path)
                } catch (e: IOException) {
                    ArcadeUtils.logger.error("Failed to write external data at $path")
                }
            }
        }
    }
}
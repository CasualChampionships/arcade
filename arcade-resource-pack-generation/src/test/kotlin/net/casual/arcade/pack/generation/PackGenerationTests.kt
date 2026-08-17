package net.casual.arcade.pack.generation

import com.mojang.serialization.JsonOps
import net.casual.arcade.pack.generation.PackTestUtils.entries
import net.casual.arcade.pack.generation.PackTestUtils.json
import net.casual.arcade.pack.generation.PackTestUtils.pack
import net.minecraft.SharedConstants
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier
import net.minecraft.server.packs.FilePackResources
import net.minecraft.server.packs.PackLocationInfo
import net.minecraft.server.packs.PackType
import net.minecraft.server.packs.metadata.pack.PackMetadataSection
import net.minecraft.server.packs.repository.Pack
import net.minecraft.server.packs.repository.PackSource
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull
import org.junit.jupiter.api.io.TempDir
import java.awt.image.BufferedImage
import java.nio.file.Path
import java.security.MessageDigest
import java.util.Optional
import kotlin.io.path.name
import kotlin.io.path.readBytes

class PackGenerationTests {
    companion object {
        @JvmStatic
        @BeforeAll
        fun bootstrap() {
            PackTestUtils.bootstrap()
        }
    }

    @Test
    fun `pack generates correct mcmeta`() {
        val mcmeta = entries { }.json("pack.mcmeta")

        val section = PackMetadataSection.CLIENT_TYPE.codec()
            .parse(JsonOps.INSTANCE, mcmeta.get("pack"))
            .orThrow

        assertEquals("Server Resource Pack", section.description().string)

        val current = SharedConstants.getCurrentVersion().packVersion(PackType.CLIENT_RESOURCES)
        assertEquals(current.minorRange(), section.supportedFormats())
    }

    @Test
    fun `pack can be read by minecraft`(@TempDir directory: Path) {
        val zip = pack {
            description = Component.literal("My pack")
            addFile("assets/test/font/example.json", "{}")
        }.buildTo(directory)

        val location = PackLocationInfo("test", Component.literal("test"), PackSource.SERVER, Optional.empty())
        val supplier = FilePackResources.FileResourcesSupplier(zip)
        val format = SharedConstants.getCurrentVersion().packVersion(PackType.CLIENT_RESOURCES)

        val metadata = Pack.readPackMetadata(location, supplier, format, PackType.CLIENT_RESOURCES)
        assertNotNull(metadata, "Minecraft could not read the generated pack")
        assertTrue(metadata.compatibility().isCompatible)
        assertEquals("My pack", metadata.description().string)

        supplier.openPrimary(location).use { resources ->
            assertEquals(setOf("test"), resources.getNamespaces(PackType.CLIENT_RESOURCES))

            val font = Identifier.parse("test:font/example.json")
            val resource = resources.getResource(PackType.CLIENT_RESOURCES, font)
            assertNotNull(resource, "The generated pack is missing the font we added")
            assertEquals("{}", resource.get().use { stream -> stream.readBytes().decodeToString() })
        }
    }

    @Test
    fun `pack generates all directories`() {
        val entries = entries {
            addFile("assets/test/font/example.json", "{}")
        }.keys

        assertTrue(entries.containsAll(listOf("assets/", "assets/test/", "assets/test/font/")))
    }

    @Test
    fun `pack entries are sorted`() {
        val entries = entries {
            addFile("assets/test/font/example.json", "{}")
            addFile("assets/test/atlases/blocks.json", "{}")
            addFile("assets/other/lang/en_us.json", "{}")
        }.keys

        assertEquals(entries.sorted(), entries.toList())
    }

    @Test
    fun `pack icon is correctly written`() {
        val image = BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB)
        val entries = entries { setIcon(image) }

        assertTrue(entries.containsKey("pack.png"))
    }

    @Test
    fun `pack definition generates identical packs`() {
        val definition = pack {
            addFile("assets/test/lang/en_us.json", """{"a": "one"}""")
            addFile("assets/test/font/example.json", "{}")
        }

        val first = definition.build()
        val second = definition.build()

        assertArrayEquals(first.stream().readBytes(), second.stream().readBytes())
        assertEquals(first.hash(), second.hash())
    }

    @Test
    fun `pack hash is sha1 of its contents`() {
        val built = pack { addFile("assets/test/font/example.json", "{}") }.build()

        val digest = MessageDigest.getInstance("SHA-1").digest(built.stream().readBytes())
        val hash = digest.joinToString("") { byte -> "%02x".format(byte) }

        assertEquals(hash, built.hash())
        assertEquals(built.stream().readBytes().size.toLong(), built.length())
    }

    @Test
    fun `pack writes to file correctly`(@TempDir directory: Path) {
        val built = pack { addFile("assets/test/font/example.json", "{}") }.build()
        val zip = built.writeTo(directory.resolve("nested"))

        assertEquals("test.zip", zip.name)
        assertEquals(built.zipped, zip.name)
        assertArrayEquals(built.stream().readBytes(), zip.readBytes())
    }
}

package net.casual.arcade.pack.generation

import net.casual.arcade.pack.font.FontResources
import net.casual.arcade.pack.generation.PackTestUtils.entries
import net.casual.arcade.pack.generation.PackTestUtils.json
import net.casual.arcade.pack.generation.PackTestUtils.text
import net.casual.arcade.pack.sound.SoundResources
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier
import net.minecraft.sounds.SoundEvent
import net.minecraft.world.scores.TeamColor
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.io.TempDir
import java.awt.image.BufferedImage
import java.nio.file.Path
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.io.path.createDirectories
import kotlin.io.path.outputStream
import kotlin.io.path.writeText

class PackContentsTests {
    companion object {
        @JvmStatic
        @BeforeAll
        fun bootstrap() {
            PackTestUtils.bootstrap()
        }
    }

    @Test
    fun `language files are merged`() {
        val lang = entries {
            addFile("assets/test/lang/en_us.json", """{"a": "one", "b": "two"}""")
            addFile("assets/test/lang/en_us.json", """{"b": "three", "c": "four"}""")
        }.json("assets/test/lang/en_us.json")

        assertEquals("one", lang.get("a").asString)
        assertEquals("three", lang.get("b").asString)
        assertEquals("four", lang.get("c").asString)
    }

    @Test
    fun `atlas sources are concatenated`() {
        val atlas = entries {
            addFile("assets/test/atlases/blocks.json", """{"sources": [{"type": "a"}]}""")
            addFile("assets/test/atlases/blocks.json", """{"sources": [{"type": "b"}]}""")
        }.json("assets/test/atlases/blocks.json")

        assertEquals(2, atlas.getAsJsonArray("sources").size())
    }

    @Test
    fun `sounds merge unless replace is specified`() {
        val sounds = entries {
            addFile("assets/test/sounds.json", """{"one": {"sounds": ["a"]}, "two": {"sounds": ["a"]}}""")
            addFile("assets/test/sounds.json", """{"one": {"sounds": ["b"]}, "two": {"replace": true, "sounds": ["b"]}}""")
        }.json("assets/test/sounds.json")

        assertEquals(2, sounds.getAsJsonObject("one").getAsJsonArray("sounds").size())
        assertEquals(1, sounds.getAsJsonObject("two").getAsJsonArray("sounds").size())
    }

    @Test
    fun `last file write is final`() {
        val entries = entries {
            addFile("assets/test/models/item/example.json", """{"first": true}""")
            addFile("assets/test/models/item/example.json", """{"second": true}""")
        }

        assertEquals("""{"second": true}""", entries.text("assets/test/models/item/example.json"))
    }

    @Test
    fun `leading slashes are ignored`() {
        val entries = entries {
            addFile("/assets/test/font/example.json", "{}")
            assertTrue("assets/test/font/example.json" in this)
        }

        assertTrue(entries.containsKey("assets/test/font/example.json"))
    }

    @Test
    fun `including a directory copies all contents`(@TempDir directory: Path) {
        directory.resolve("assets/test/font").createDirectories()
        directory.resolve("assets/test/font/example.json").writeText("{}")
        directory.resolve("pack.mcmeta").writeText(
            """{"pack": {"description": "Ignored"}, "filter": {"block": [{"namespace": "test"}]}}"""
        )

        val entries = entries { include(directory) }

        assertEquals("{}", entries.text("assets/test/font/example.json"))

        val mcmeta = entries.json("pack.mcmeta")
        assertEquals(1, mcmeta.getAsJsonObject("filter").getAsJsonArray("block").size())
        assertNotEquals("Ignored", mcmeta.getAsJsonObject("pack").get("description").asString)
    }

    @Test
    fun `including a zip copies all contents`(@TempDir directory: Path) {
        val zip = directory.resolve("source.zip")
        ZipOutputStream(zip.outputStream()).use { stream ->
            stream.putNextEntry(ZipEntry("assets/test/font/example.json"))
            stream.write("{}".encodeToByteArray())
            stream.closeEntry()
        }

        val entries = entries { include(zip) }

        assertEquals("{}", entries.text("assets/test/font/example.json"))
    }

    @Test
    fun `copying puts files at specified destination`(@TempDir directory: Path) {
        directory.resolve("nested").createDirectories()
        directory.resolve("example.png").writeText("root")
        directory.resolve("nested/example.png").writeText("nested")

        val entries = entries { copy(directory, "assets/test/textures") }

        assertEquals("root", entries.text("assets/test/textures/example.png"))
        assertEquals("nested", entries.text("assets/test/textures/nested/example.png"))
    }

    @Test
    fun `adding langs from directory copies all langs`(@TempDir directory: Path) {
        directory.createDirectories()
        directory.resolve("en_us.json").writeText("""{"a": "one"}""")
        directory.resolve("en_gb.json").writeText("""{"a": "won"}""")

        val entries = entries { addLangs("test", directory) }

        assertEquals("one", entries.json("assets/test/lang/en_us.json").get("a").asString)
        assertEquals("won", entries.json("assets/test/lang/en_gb.json").get("a").asString)
    }

    @Test
    fun `adding langs from other mods`() {
        val entries = entries {
            addLangs("not-a-real-mod", throwIfMissing = false)
            assertThrows<IllegalArgumentException> { addLangs("not-a-real-mod") }
        }

        assertEquals(setOf("pack.mcmeta"), entries.keys)
    }

    @Test
    fun `font resources generate correctly`() {
        val entries = entries { addFont(TestFontResources) }

        val providers = entries.json("assets/test/font/example.json").getAsJsonArray("providers")
        assertEquals(3, providers.size())

        val lang = entries.json("assets/test/lang/en_us.json")
        assertTrue(lang.has("test.translated"))

        assertTrue(entries.containsKey("assets/test/textures/font/icon.png"))
    }

    @Test
    fun `sound resources generate correctly`() {
        val sounds = entries { addSounds(TestSoundResources) }.json("assets/test/sounds.json")

        assertEquals(1, sounds.getAsJsonObject("ping").getAsJsonArray("sounds").size())
        assertEquals(2, sounds.getAsJsonObject("group").getAsJsonArray("sounds").size())
    }

    @Test
    fun `missing item models and definitions are generated`(@TempDir directory: Path) {
        val assets = directory.resolve("assets")
        assets.resolve("test/textures/item/nested").createDirectories()
        assets.resolve("test/textures/item/nested/example.png").writeText("png")

        val entries = entries { generateMissingItemModels("test", assets) }

        val model = entries.json("assets/test/models/item/nested/example.json")
        assertEquals("test:item/nested/example", model.getAsJsonObject("textures").get("layer0").asString)

        val definition = entries.json("assets/test/items/nested/example.json")
        assertEquals("test:item/nested/example", definition.getAsJsonObject("model").get("model").asString)
    }

    @Test
    fun `existing item models are not overwritten`(@TempDir directory: Path) {
        val assets = directory.resolve("assets")
        assets.resolve("test/textures/item").createDirectories()
        assets.resolve("test/textures/item/example.png").writeText("png")
        assets.resolve("test/models/item").createDirectories()
        assets.resolve("test/models/item/example.json").writeText("""{"custom": true}""")

        val entries = entries {
            include(directory)
            generateMissingItemModels("test", assets)
        }

        assertEquals("""{"custom": true}""", entries.text("assets/test/models/item/example.json"))

        val definition = entries.json("assets/test/items/example.json")
        assertEquals("test:item/example", definition.getAsJsonObject("model").get("model").asString)
    }

    @Test
    fun `outline colors are replaced in the shader`() {
        val shader = entries {
            addOutlineColors {
                set(TeamColor.RED, 0xFF8800)
            }
        }.text("assets/minecraft/shaders/core/rendertype_outline.vsh")

        assertTrue(shader.contains("#FF8800"))
    }

    @Test
    fun `on finish hook can view pack resources`() {
        val entries = entries {
            onFinish {
                val paths = ArrayList<String>()
                forEach { path, _ -> paths.add(path) }
                addFile("summary.txt", paths.sorted().joinToString("\n"))
            }
            addFile("assets/test/font/example.json", "{}")
        }

        assertEquals("assets/test/font/example.json", entries.text("summary.txt"))
    }

    @Suppress("Unused")
    private object TestFontResources: FontResources(Identifier.parse("test:example")) {
        val SPACE: Component = space(4.0F)

        val ICON: Component = bitmap(at("icon")) {
            BufferedImage(8, 8, BufferedImage.TYPE_INT_ARGB)
        }

        val TRANSLATED: Component = translatable("test.translated") {
            bitmap("en_us", Identifier.parse("test:font/translated"))
        }
    }

    @Suppress("Unused")
    private object TestSoundResources: SoundResources("test") {
        val PING: SoundEvent = sound(at("ping"))

        val GROUP: SoundEvent = group("group") {
            sound(Identifier.parse("test:one"))
            sound(Identifier.parse("test:two"))
        }
    }
}

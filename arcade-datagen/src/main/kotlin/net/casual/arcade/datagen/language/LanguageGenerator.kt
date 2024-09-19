package net.casual.arcade.datagen.language

import net.casual.arcade.datagen.utils.LOGGER
import net.casual.arcade.datagen.utils.LanguageUtils
import net.casual.arcade.resources.lang.LanguageEntry
import net.minecraft.client.Minecraft
import java.nio.file.Path
import kotlin.io.path.readText
import kotlin.io.path.writeText

public class LanguageGenerator(
    private val languages: List<String>
) {
    private val generators = ArrayList<LanguageEntryGenerator>()

    public fun add(generator: LanguageEntryGenerator): LanguageGenerator {
        this.generators.add(generator)
        return this
    }

    public fun generate(client: Minecraft, consumer: (lang: String, entries: List<LanguageEntry>) -> Unit) {
        LanguageUtils.setForeachLanguage(client, this.languages) { lang ->
            val entries = ArrayList<LanguageEntry>()
            for (entry in this.generators) {
                try {
                    entry.run(client.font, entries)
                } catch (e: Exception) {
                    LOGGER.error("Failed to run entry ${entry::class.java.simpleName}", e)
                }
            }
            consumer(lang, entries)
        }
    }

    public fun replaceLangs(client: Minecraft, langs: Path) {
        this.generate(client) { lang, entries ->
            val json = langs.resolve("${lang}.json")
            try {
                var original = json.readText()
                for (entry in entries) {
                    original = entry.replaceInJson(original)
                }
                json.writeText(original)
            } catch (e: Exception) {
                LOGGER.error("Failed to replace lang $lang", e)
            }
        }
    }
}
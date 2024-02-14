package net.casual.arcade.datagen.language

import net.casual.arcade.datagen.utils.LanguageUtils
import net.minecraft.client.Minecraft
import java.nio.file.Path
import kotlin.io.path.readText
import kotlin.io.path.writeText

class LanguageGenerator(
    private val languages: List<String>
) {
    private val generators = ArrayList<LanguageEntryGenerator>()

    fun add(generator: LanguageEntryGenerator): LanguageGenerator {
        this.generators.add(generator)
        return this
    }

    fun generate(client: Minecraft, consumer: (lang: String, entries: List<LanguageEntry>) -> Unit) {
        LanguageUtils.setForeachLanguage(client, this.languages) { lang ->
            val entries = ArrayList<LanguageEntry>()
            for (entry in this.generators) {
                entry.run(client.font, entries)
            }
            consumer(lang, entries)
        }
    }

    fun replaceLangs(client: Minecraft, langs: Path) {
        this.generate(client) { lang, entries ->
            val json = langs.resolve("${lang}.json")
            var original = json.readText()
            for (entry in entries) {
                original = entry.replaceInJson(original)
            }
            json.writeText(original)
        }
    }
}
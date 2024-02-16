package net.casual.arcade.datagen.utils

import net.minecraft.client.Minecraft

object LanguageUtils {
    fun setForeachLanguage(client: Minecraft, languages: Collection<String>, consumer: (String) -> Unit) {
        val selected = client.languageManager.selected

        for (language in languages) {
            client.setLanguage(language)
            consumer(language)
        }
        client.setLanguage(selected)
    }

    fun Minecraft.setLanguage(lang: String) {
        this.languageManager.selected = lang
        this.languageManager.onResourceManagerReload(this.resourceManager)
    }
}
/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.datagen.utils

import net.minecraft.client.Minecraft

public object LanguageUtils {
    public fun setForeachLanguage(client: Minecraft, languages: Collection<String>, consumer: (String) -> Unit) {
        val selected = client.languageManager.selected

        for (language in languages) {
            client.setLanguage(language)
            consumer(language)
        }
        client.setLanguage(selected)
    }

    public fun Minecraft.setLanguage(lang: String) {
        try {
            this.languageManager.selected = lang
            this.reloadResourcePacks()
        } catch (e: Exception) {
            LOGGER.error("Failed to set language to $lang", e)
        }
    }
}
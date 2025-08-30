/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.utils.string

import net.casual.arcade.utils.capitalize
import net.casual.arcade.utils.decapitalize
import net.casual.arcade.utils.fromSmallCaps
import net.casual.arcade.utils.toSmallCaps

public interface StringCasingEncoder {
    public fun encode(strings: List<String>): String
}

public interface StringCasingDecoder {
    public fun decode(string: String): List<String>
}

public interface StringCasingCodec: StringCasingEncoder, StringCasingDecoder

public data object PascalCase: StringCasingCodec {
    override fun encode(strings: List<String>): String {
        return strings.joinToString("") { it.lowercase().capitalize() }
    }

    override fun decode(string: String): List<String> {
        val strings = ArrayList<String>()
        val builder = StringBuilder()
        for (char in string) {
            val last = builder.lastOrNull()
            if (last == null || last.isUpperCase() || !char.isUpperCase() ) {
                builder.append(char)
                continue
            }

            strings.add(builder.toString())

            builder.clear()
            builder.append(char)
        }
        if (builder.isNotEmpty()) {
            strings.add(builder.toString())
        }
        return strings
    }
}

public data object CamelCase: StringCasingCodec {
    override fun encode(strings: List<String>): String {
        return PascalCase.encode(strings).decapitalize()
    }

    override fun decode(string: String): List<String> {
        return PascalCase.decode(string)
    }
}

public data object SnakeCase: StringCasingCodec {
    override fun encode(strings: List<String>): String {
        return strings.joinToString("_") { it.lowercase() }
    }

    override fun decode(string: String): List<String> {
        return string.split("_")
    }
}

public data object KebabCase: StringCasingCodec {
    override fun encode(strings: List<String>): String {
        return strings.joinToString("-") { it.lowercase() }
    }

    override fun decode(string: String): List<String> {
        return string.split("-")
    }
}

public data object ScreamingSnakeCase: StringCasingCodec {
    override fun encode(strings: List<String>): String {
        return strings.joinToString("_") { it.uppercase() }
    }

    override fun decode(string: String): List<String> {
        return SnakeCase.decode(string)
    }
}

public data object TitleCase: StringCasingCodec {
    override fun encode(strings: List<String>): String {
        return strings.joinToString(" ") { it.lowercase().capitalize() }
    }

    override fun decode(string: String): List<String> {
        return string.split(" ")
    }
}

public data object SmallCapsTitleCase: StringCasingCodec {
    override fun encode(strings: List<String>): String {
        return strings.joinToString(" ") { string ->
            string.toSmallCaps().replaceFirstChar { string.first().uppercase() }
        }
    }

    override fun decode(string: String): List<String> {
        return TitleCase.decode(string.fromSmallCaps())
    }
}

public data object FlatCase: StringCasingEncoder {
    override fun encode(strings: List<String>): String {
        return strings.joinToString("") { it.lowercase() }
    }
}
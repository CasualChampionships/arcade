/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.utils

import net.minecraft.resources.Identifier
import java.util.*

public fun arcade(path: String): Identifier {
    return Identifier.fromNamespaceAndPath(ArcadeUtils.MOD_ID, path)
}

public fun minecraft(path: String): Identifier {
    return Identifier.withDefaultNamespace(path)
}

public fun Identifier(namespace: String, path: String): Identifier {
    return Identifier.fromNamespaceAndPath(namespace, path)
}

public object IdentifierUtils {
    public const val VALID_NAMESPACE_CHARS: String = Identifier.ALLOWED_NAMESPACE_CHARACTERS
    public const val VALID_PATH_CHARS: String = "[a-z0-9_./-]"

    public val REGEX: Regex = Regex("$VALID_PATH_CHARS+:$VALID_PATH_CHARS+")

    public fun isValidNamespaceChar(c: Char): Boolean {
        return c == '_' || c == '-' || c in 'a'..'z' || c in '0'..'9' || c == '.'
    }

    public fun isValidPathChar(c: Char): Boolean {
        return Identifier.validPathChar(c)
    }

    public fun random(namespace: String = ArcadeUtils.MOD_ID): Identifier {
        val key = UUID.randomUUID().toString()
        return Identifier.fromNamespaceAndPath(namespace, key)
    }

    public fun random(namespace: String = ArcadeUtils.MOD_ID, modifier: (String) -> String): Identifier {
        val key = UUID.randomUUID().toString()
        return Identifier.fromNamespaceAndPath(namespace, modifier(key))
    }
}

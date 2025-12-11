/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.utils

import net.minecraft.resources.Identifier
import org.jetbrains.annotations.ApiStatus.Internal
import java.util.*

@Internal
public fun arcade(path: String): Identifier {
    return ArcadeUtils.id(path)
}

public fun Identifier(namespace: String, path: String): Identifier {
    return Identifier.fromNamespaceAndPath(namespace, path)
}

public object IdentifierUtils {
    public fun arcade(path: String): Identifier {
        return ArcadeUtils.id(path)
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

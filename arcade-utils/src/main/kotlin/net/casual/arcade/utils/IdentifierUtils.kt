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
    @Deprecated("use arcade() instead", ReplaceWith("arcade(path)", "net.casual.arcade.utils.arcade"))
    public fun arcade(path: String): Identifier {
        return net.casual.arcade.utils.arcade(path)
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

package net.casual.arcade.virtual.entity

import net.casual.arcade.utils.math.location.Location

public interface VirtualEntityAttachment {
    public val origin: Location

    public fun entities(): Collection<VirtualEntity>
}
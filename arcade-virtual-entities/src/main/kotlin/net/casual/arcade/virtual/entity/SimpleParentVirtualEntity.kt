package net.casual.arcade.virtual.entity

import it.unimi.dsi.fastutil.objects.ObjectArrayList
import net.minecraft.server.level.ServerPlayer

public open class SimpleParentVirtualEntity: TrackingVirtualEntity(), ParentVirtualEntity {
    protected val children: MutableList<VirtualEntity> = ObjectArrayList()

    public fun addChild(child: VirtualEntity) {
        this.children.add(child)

        for (observer in this.observers()) {
            child.startObserving(observer)

        }
    }

    override fun startObserving(observer: ServerPlayer) {
        super<TrackingVirtualEntity>.startObserving(observer)
        super<ParentVirtualEntity>.startObserving(observer)
    }

    override fun stopObserving(observer: ServerPlayer) {
        super<TrackingVirtualEntity>.stopObserving(observer)
        super<ParentVirtualEntity>.stopObserving(observer)
    }

    override fun children(): Collection<VirtualEntity> {
        return this.children
    }
}
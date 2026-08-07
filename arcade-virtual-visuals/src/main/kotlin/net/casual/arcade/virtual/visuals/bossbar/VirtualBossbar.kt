/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.virtual.visuals.bossbar

import net.casual.arcade.observer.Observer
import net.casual.arcade.observer.tracker.ObserverTracker
import net.casual.arcade.observer.tracker.SimpleObserverTracker
import net.casual.arcade.observer.utils.asPlayerOrNull
import net.casual.arcade.utils.arcade
import net.casual.arcade.utils.network.PacketSender
import net.casual.arcade.virtual.visuals.VirtualVisual
import net.casual.arcade.virtual.visuals.data.PlayerSpecificValue
import net.casual.arcade.virtual.visuals.data.PlayerSpecificVisualData
import net.minecraft.network.chat.CommonComponents
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.game.ClientboundBossEventPacket
import net.minecraft.world.BossEvent
import net.minecraft.world.BossEvent.BossBarColor
import net.minecraft.world.BossEvent.BossBarOverlay
import java.util.*

/**
 * A bossbar implementation of [VirtualVisual].
 *
 * Every part of the bossbar has a base value, which all observers
 * are shown by default, as well as optional per-player overrides:
 * ```
 * val bossbar = VirtualBossbar()
 * bossbar.title.set(Component.literal("Hello!"))
 * bossbar.color.set(BossBarColor.RED)
 *
 * bossbar.title.set(player, Component.literal("Hello, ${player.username}!"))
 * ```
 *
 * Setting a value to what it already is does nothing.
 *
 * @param observers The observer tracker for this bossbar.
 * @see PlayerSpecificValue
 */
public open class VirtualBossbar(
    override val observers: ObserverTracker = SimpleObserverTracker()
): VirtualVisual {
    /**
     * The bossbar's uuid, this identifies the bossbar.
     */
    public val uuid: UUID = UUID.randomUUID()

    /**
     * The data for this bossbar.
     */
    protected val data: PlayerSpecificVisualData = PlayerSpecificVisualData()

    /**
     * The title of the bossbar.
     */
    public val title: PlayerSpecificValue<Component> = this.data.register(CommonComponents.EMPTY)

    /**
     * The progress of the bossbar, between `0.0` and `1.0`.
     */
    public val progress: PlayerSpecificValue<Float> = this.data.register(1.0F)

    /**
     * The color of the bossbar.
     */
    public val color: PlayerSpecificValue<BossBarColor> = this.data.register(BossBarColor.WHITE)

    /**
     * The overlay of the bossbar.
     */
    public val overlay: PlayerSpecificValue<BossBarOverlay> = this.data.register(BossBarOverlay.PROGRESS)

    /**
     * Whether the observer's screen should be darkened.
     */
    public val dark: PlayerSpecificValue<Boolean> = this.data.register(false)

    /**
     * Whether the observer should be played boss music in the end dimension.
     */
    public val music: PlayerSpecificValue<Boolean> = this.data.register(false)

    /**
     * Whether the observer's world should have fog.
     */
    public val fog: PlayerSpecificValue<Boolean> = this.data.register(false)

    private val style = this.color.bit or this.overlay.bit
    private val properties = this.dark.bit or this.music.bit or this.fog.bit

    override fun tick() {
        val base = this.data.clean()
        this.observers.broadcast { observer ->
            this.sendDirtyPackets(observer, base)
        }
    }

    override fun sendSpawnPackets(observer: Observer, sender: PacketSender) {
        sender.send(ClientboundBossEventPacket.createAddPacket(this.createBossEvent(observer)))
    }

    override fun sendDespawnPackets(observer: Observer, sender: PacketSender) {
        sender.send(ClientboundBossEventPacket.createRemovePacket(this.uuid))
    }

    override fun onStartObserving(observer: Observer) {
        val player = observer.asPlayerOrNull()
        if (player != null) {
            this.data.clean(player.uuid)
        }

        observer.context.getOrSet(CURRENT_BOSSBARS, ::LinkedHashSet).add(this)
    }

    override fun onStopObserving(observer: Observer) {
        observer.context.get(CURRENT_BOSSBARS)?.remove(this)
    }

    /**
     * Sends the given [observer] the packets for any of this bossbar's
     * values which have changed for them since the last tick.
     *
     * @param observer The observer to send packets to.
     * @param baseDirty The mask returned by [PlayerSpecificVisualData.clean].
     */
    protected open fun sendDirtyPackets(observer: Observer, baseDirty: Int) {
        val player = observer.asPlayerOrNull()
        val dirty = if (player != null) this.data.clean(player.uuid, baseDirty) else baseDirty
        if (dirty == 0) {
            return
        }

        val event = this.createBossEvent(observer)
        if (dirty and this.title.bit != 0) {
            observer.send(ClientboundBossEventPacket.createUpdateNamePacket(event))
        }
        if (dirty and this.progress.bit != 0) {
            observer.send(ClientboundBossEventPacket.createUpdateProgressPacket(event))
        }
        if (dirty and this.style != 0) {
            observer.send(ClientboundBossEventPacket.createUpdateStylePacket(event))
        }
        if (dirty and this.properties != 0) {
            observer.send(ClientboundBossEventPacket.createUpdatePropertiesPacket(event))
        }
    }

    /**
     * Creates a [BossEvent] representing what the given [observer]
     * should currently be shown.
     *
     * This is only used to write packets; it is not retained, so this
     * bossbar never holds a reference to any observer's state.
     *
     * @param observer The observer to resolve this bossbar's values for.
     * @return The resolved [BossEvent].
     */
    protected open fun createBossEvent(observer: Observer): BossEvent {
        return VirtualBossEvent(
            this.uuid,
            this.title.get(observer),
            this.color.get(observer),
            this.overlay.get(observer),
            this.progress.get(observer),
            this.dark.get(observer),
            this.music.get(observer),
            this.fog.get(observer)
        )
    }

    private class VirtualBossEvent(
        uuid: UUID,
        name: Component,
        color: BossBarColor,
        overlay: BossBarOverlay,
        progress: Float,
        dark: Boolean,
        music: Boolean,
        fog: Boolean
    ): BossEvent(uuid, name, color, overlay) {
        init {
            this.setProgress(progress)
            this.setDarkenScreen(dark)
            this.setPlayBossMusic(music)
            this.setCreateWorldFog(fog)
        }
    }

    internal companion object {
        val CURRENT_BOSSBARS = Observer.Context.Key<LinkedHashSet<VirtualBossbar>>(arcade("virtual_bossbars"))
    }
}

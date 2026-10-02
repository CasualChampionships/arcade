/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.model.virtual

import it.unimi.dsi.fastutil.ints.IntArrayList
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap
import net.casual.arcade.model.animation.ModelAnimationInstance
import net.casual.arcade.model.animation.ModelAnimator
import net.casual.arcade.model.animation.molang.MolangScope
import net.casual.arcade.model.animation.pose.BonePose
import net.casual.arcade.model.animation.pose.ModelPoser
import net.casual.arcade.model.animation.timeline.EffectKeyframe
import net.casual.arcade.model.definition.ModelBounds
import net.casual.arcade.model.definition.ModelDefinition
import net.casual.arcade.model.definition.ModelNode
import net.casual.arcade.observer.Observer
import net.casual.arcade.observer.tracker.ObserverTracker
import net.casual.arcade.utils.ClientboundSetPassengersPacket
import net.casual.arcade.utils.network.PacketSender
import net.casual.arcade.virtual.entity.SimpleParentVirtualEntity
import net.casual.arcade.virtual.entity.VirtualEntity
import net.casual.arcade.virtual.entity.attachment.VirtualEntityAttachment
import net.casual.arcade.virtual.entity.attachment.anchor.AttachmentAnchor
import net.casual.arcade.virtual.entity.display.SimpleVirtualItemDisplay
import net.casual.arcade.virtual.entity.interaction.SimpleVirtualInteractionEntity
import net.casual.arcade.virtual.entity.location.VirtualPosition
import net.casual.arcade.virtual.entity.location.VirtualRotation
import net.casual.arcade.virtual.entity.utils.attachWithParentObservers
import net.casual.arcade.virtual.entity.utils.location
import net.minecraft.core.Holder
import net.minecraft.network.protocol.game.ClientboundSoundPacket
import net.minecraft.sounds.SoundEvent
import net.minecraft.sounds.SoundSource
import net.minecraft.util.Mth
import net.minecraft.world.phys.Vec2
import net.minecraft.world.phys.Vec3
import org.joml.Quaternionf
import org.joml.Vector3f
import java.util.UUID
import kotlin.random.Random

public class ModelVirtualEntity(
    private val definition: ModelDefinition,
    attachment: VirtualEntityAttachment,
    observers: ObserverTracker
): SimpleParentVirtualEntity(attachment, observers) {
    private val root = this.attachWithParentObservers(::SimpleVirtualItemDisplay)

    private val bones = Object2ObjectLinkedOpenHashMap<UUID, BoneVirtualEntity>()
    private val locators = Object2ObjectLinkedOpenHashMap<UUID, Locator>()
    private val poses = Object2ObjectLinkedOpenHashMap<UUID, BonePose>()

    private val scope = MolangScope()

    private val animator = ModelAnimator(this.definition)
    private val poser = ModelPoser(this.definition)

    private var transformInterpolation = DEFAULT_INTERPOLATION
    private var teleportInterpolation = DEFAULT_INTERPOLATION

    private var age = 0

    public var hitbox: SimpleVirtualInteractionEntity? = null
        private set

    public var soundHandler: SoundEffectHandler = SoundEffectHandler(this)
    public var commandHandler: CommandEffectHandler = CommandEffectHandler.Noop

    init {
        this.initialize()
    }

    public fun bone(name: String): BoneVirtualEntity? {
        val node = this.definition.bone(name) ?: return null
        return this.bones[node.uuid]
    }

    public fun bones(): Collection<BoneVirtualEntity> {
        return this.bones.values
    }

    public fun locator(name: String): Locator? {
        val node = this.definition.locator(name) ?: return null
        return this.locators[node.uuid]
    }

    public fun locators(): Collection<Locator> {
        return this.locators.values
    }

    public fun animator(): ModelAnimator {
        return this.animator
    }

    public fun setHitbox(
        bounds: ModelBounds = this.definition.bounds,
        interaction: VirtualEntity.InteractionHandler? = null
    ): SimpleVirtualInteractionEntity {
        val hitbox = this.hitbox ?: this.attachWithParentObservers(::SimpleVirtualInteractionEntity)
        hitbox.isPassenger = true
        hitbox.setWidth(bounds.width)
        hitbox.setWidth(bounds.height)
        hitbox.setInteractionHandlerProvider { interaction }

        this.hitbox = hitbox
        return hitbox
    }

    public fun removeHitbox() {
        val hitbox = this.hitbox ?: return
        this.detach(hitbox)
        this.hitbox = null
    }

    override fun sendSpawnPackets(observer: Observer, sender: PacketSender) {
        super.sendSpawnPackets(observer, sender)
        sender.send(ClientboundSetPassengersPacket(this.root.id, this.getPassengerIds()))
    }

    override fun updateChildren() {
        this.age++
        this.scope.lifetime = this.age / 20.0F

        this.animator.tick(this::handleEffectKeyframe)
        this.updatePoses()
    }

    private fun updatePoses() {
        this.poser.pose(this.animator, this.scope, 1.0F, this.poses)

        for ((uuid, bone) in this.bones) {
            bone.pose(this.poses.getValue(uuid))
            bone.startInterpolationIfBaseDirty()
        }

        val rotation = this.rotation.get(this.anchor.location().rotation)
        for ((uuid, locator) in this.locators) {
            locator.update(this.poses.getValue(uuid), rotation)
        }
    }

    private fun initialize() {
        this.root.setInvisible(true)
        this.root.setTeleportationInterpolation(this.teleportInterpolation)

        for (node in this.definition.nodes()) {
            this.poses[node.uuid] = BonePose()

            when (node) {
                is ModelNode.Bone -> this.addBoneNode(node)
                is ModelNode.Locator -> this.addLocatorNode(node)
            }
        }

        this.updatePoses()
    }

    private fun addBoneNode(node: ModelNode.Bone) {
        val geometry = node.geometry
        if (geometry != null) {
            val bone = this.attachWithParentObservers { attachment, observers ->
                BoneVirtualEntity(geometry, attachment, observers)
            }
            bone.setTransformationInterpolation(this.transformInterpolation)
            this.bones[node.uuid] = bone
        }
    }

    private fun addLocatorNode(locator: ModelNode.Locator) {
        this.locators[locator.uuid] = Locator()
    }

    private fun getPassengerIds(): IntArray {
        val ids = IntArrayList(this.bones.size + 1)
        for (bone in this.bones()) {
            ids.add(bone.id)
        }
        val hitbox = this.hitbox
        if (hitbox != null) {
            ids.add(hitbox.id)
        }
        return ids.toIntArray()
    }

    private fun handleEffectKeyframe(animation: ModelAnimationInstance, effect: EffectKeyframe) {
        when (effect) {
            is EffectKeyframe.Sound -> this.soundHandler.handle(effect)
            is EffectKeyframe.Command -> this.commandHandler.handle(animation, effect)
        }
    }

    public class Locator {
        private val orientation = Quaternionf()
        private val offset = Vector3f()

        public var position: Vec3 = Vec3.ZERO
        public var rotation: Vec2 = Vec2.ZERO

        internal fun update(pose: BonePose, parentRot: Vec2) {
            this.orientation.rotationYXZ(-Mth.DEG_TO_RAD * parentRot.y, Mth.DEG_TO_RAD * parentRot.x, 0.0F)
            pose.position(this.offset).rotate(this.orientation)
            this.position = Vec3(this.offset)

            val rotation = pose.rotation()
            val euler = rotation.getEulerAnglesYXZ(Vector3f())
            this.rotation = Vec2(Mth.RAD_TO_DEG * euler.x, -Mth.RAD_TO_DEG * euler.y)
        }
    }

    public class SoundEffectHandler(
        private val model: ModelVirtualEntity,
        public var source: SoundSource = SoundSource.MASTER,
        public var volume: Float = 1.0F,
        public var pitch: Float = 1.0F
    ) {
        public fun handle(effect: EffectKeyframe.Sound) {
            val sound = Holder.direct(SoundEvent.createVariableRangeEvent(effect.sound))
            val position = this.model.location().position
            this.model.observers.broadcast(
                ClientboundSoundPacket(sound, this.source, position.x, position.y, position.z, this.volume, this.pitch, Random.nextLong())
            )
        }
    }

    public interface CommandEffectHandler {
        public fun handle(animation: ModelAnimationInstance, effect: EffectKeyframe.Command)

        public object Noop: CommandEffectHandler {
            override fun handle(animation: ModelAnimationInstance, effect: EffectKeyframe.Command) {

            }
        }
    }

    public companion object {
        public const val DEFAULT_INTERPOLATION: Int = 2
    }
}
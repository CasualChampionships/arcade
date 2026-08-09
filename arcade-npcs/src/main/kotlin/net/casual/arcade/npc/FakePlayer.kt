/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.npc

import com.mojang.authlib.GameProfile
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import net.casual.arcade.npc.ai.NPCInput
import net.casual.arcade.npc.ai.NPCLookControl
import net.casual.arcade.npc.ai.NPCMoveControl
import net.casual.arcade.npc.configuration.FakePlayerConfigurationTasks
import net.casual.arcade.npc.configuration.FakePlayerConstructor
import net.casual.arcade.npc.mixins.LivingEntityAccessor
import net.casual.arcade.npc.network.FakeConnection
import net.casual.arcade.npc.network.FakeGamePacketListenerImpl
import net.casual.arcade.npc.network.FakeLoginPacketListenerImpl
import net.casual.arcade.npc.pathfinding.navigation.NPCAmphibiousPathNavigation
import net.casual.arcade.npc.pathfinding.navigation.NPCPathNavigation
import net.casual.arcade.utils.ArcadeUtils
import net.casual.arcade.utils.player.DynamicResolvableProfile
import net.casual.arcade.utils.player.server
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.component.DataComponents
import net.minecraft.network.Connection
import net.minecraft.network.protocol.game.ServerboundClientCommandPacket
import net.minecraft.network.protocol.game.ServerboundPlayerInputPacket
import net.minecraft.network.protocol.game.ServerboundPlayerLoadedPacket
import net.minecraft.network.protocol.login.LoginProtocols
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ClientInformation
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.network.CommonListenerCookie
import net.minecraft.util.Mth
import net.minecraft.util.debug.DebugBrainDump
import net.minecraft.util.debug.DebugPathInfo
import net.minecraft.util.debug.DebugSubscriptions
import net.minecraft.util.debug.DebugValueSource
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityTypes
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.PlayerRideableJumping
import net.minecraft.world.entity.ai.attributes.AttributeSupplier
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.entity.ai.attributes.DefaultAttributes
import net.minecraft.world.item.ProjectileWeaponItem
import net.minecraft.world.item.component.UseEffects
import net.minecraft.world.level.pathfinder.PathType
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec2
import net.minecraft.world.phys.Vec3
import java.util.*
import java.util.concurrent.CompletableFuture
import kotlin.math.abs
import kotlin.math.min

@Suppress("LeakingThis")
public open class FakePlayer(
    server: MinecraftServer,
    level: ServerLevel,
    profile: GameProfile,
    info: ClientInformation
): ServerPlayer(server, level, profile, info) {
    private val pathfindingMalus = Object2FloatOpenHashMap<PathType>()

    public val moveControl: NPCMoveControl = NPCMoveControl(this)
    public val lookControl: NPCLookControl = NPCLookControl(this)
    public val navigation: NPCPathNavigation = this.createNavigation()

    public val input: NPCInput = NPCInput()

    public open fun createRespawned(
        server: MinecraftServer,
        level: ServerLevel,
        profile: GameProfile,
        info: ClientInformation
    ): FakePlayer {
        return FakePlayer(server, level, profile, info)
    }

    public open fun createConnection(
        server: MinecraftServer,
        connection: Connection,
        cookie: CommonListenerCookie
    ): FakeGamePacketListenerImpl {
        return FakeGamePacketListenerImpl(server, connection, this, cookie)
    }

    public open fun connection(): FakeGamePacketListenerImpl {
        return this.connection as FakeGamePacketListenerImpl
    }

    public open fun createAttributeSupplier(): AttributeSupplier {
        return DefaultAttributes.getSupplier(EntityTypes.PLAYER)
    }

    public open fun createNavigation(): NPCPathNavigation {
        return NPCAmphibiousPathNavigation(this)
    }

    public open fun setPathfindingMalus(type: PathType, float: Float) {
        if (type.malus == float) {
            this.pathfindingMalus.removeFloat(type)
        } else {
            this.pathfindingMalus.put(type, float)
        }
    }

    public open fun getPathfindingMalus(type: PathType): Float {
        return this.pathfindingMalus.getOrDefault(type as Any, type.malus)
    }

    public open fun canFireProjectileWeapon(weapon: ProjectileWeaponItem): Boolean {
        return true
    }

    public open fun isWithinMeleeAttackRange(
        target: LivingEntity,
        modifier: Double = 0.0
    ): Boolean {
        return this.getAttackBoundingBox(modifier).intersects((target as LivingEntityAccessor).arcade_getHitbox())
    }

    public open fun getAttackBoundingBox(modifier: Double = 0.0): AABB {
        return this.boundingBox.inflate(this.entityInteractionRange() + modifier)
    }

    override fun tick() {
        // The player will never send move packets,
        // so we need to manually move the player.
        // This keeps the ticket manager updated
        if (this.server.tickCount % 10 == 0) {
            this.connection.resetPosition()
            this.level().chunkSource.move(this)
        }
        super.tick()
    }

    override fun aiStep() {
        val wasJumping = this.input.jump

        if (this.isImmobile) {
            this.input.reset()
        } else {
            this.navigation.tick()

            this.customServerAiStep(this.level())

            this.moveControl.tick()
            this.lookControl.tick()
        }

        this.connection.handlePlayerInput(ServerboundPlayerInputPacket(this.input.keyPresses))

        if (!this.noPhysics) {
            this.moveTowardsClosestSpace(this.x - this.bbWidth * 0.35, this.z + this.bbWidth * 0.35)
            this.moveTowardsClosestSpace(this.x - this.bbWidth * 0.35, this.z - this.bbWidth * 0.35)
            this.moveTowardsClosestSpace(this.x + this.bbWidth * 0.35, this.z - this.bbWidth * 0.35)
            this.moveTowardsClosestSpace(this.x + this.bbWidth * 0.35, this.z + this.bbWidth * 0.35)
        }

        if (this.canStartSprinting() && this.input.sprint) {
            this.isSprinting = true
        }

        if (this.isSprinting) {
            if (this.isSwimming) {
                if (this.shouldStopSwimSprinting()) {
                    this.isSprinting = false
                }
            } else if (this.shouldStopRunSprinting()) {
                this.isSprinting = false
            }
        }

        val justToggledFlight = this.tryToggleFlight(wasJumping)

        if (this.input.jump && !justToggledFlight && !wasJumping && !this.onClimbable()) {
            this.tryToStartFallFlying()
        }

        if (this.isInWater && this.input.shift && this.isAffectedByFluids) {
            this.goDownInWater()
        }

        if (this.abilities.flying) {
            var vertical = 0
            if (this.input.shift) {
                vertical--
            }
            if (this.input.jump) {
                vertical++
            }
            if (vertical != 0) {
                val speed = vertical * this.abilities.flyingSpeed * 3.0F
                this.deltaMovement = this.deltaMovement.add(0.0, speed.toDouble(), 0.0)
            }
        }

        super.aiStep()

        if (this.onGround() && this.abilities.flying && !this.isSpectator) {
            this.abilities.flying = false
            this.onUpdateAbilities()
        }
    }

    override fun applyInput() {
        val modified = this.modifyInput(this.input.moveVector)
        this.xxa = modified.x
        this.zza = modified.y
        this.isJumping = this.input.jump
    }

    public open fun customServerAiStep(level: ServerLevel) {

    }

    override fun tickDeath() {
        super.tickDeath()
        this.tryRespawnAfterDeath()
    }

    override fun isClientAuthoritative(): Boolean {
        return false
    }

    override fun showEndCredits() {
        this.wonGame = true
        super.showEndCredits()
    }

    protected open fun tryRespawnAfterDeath() {
        this.connection.handleClientCommand(
            ServerboundClientCommandPacket(ServerboundClientCommandPacket.Action.PERFORM_RESPAWN)
        )

        this.connection.handleAcceptPlayerLoad(ServerboundPlayerLoadedPacket())
    }

    override fun registerDebugValues(level: ServerLevel, registration: DebugValueSource.Registration) {
        registration.register(DebugSubscriptions.ENTITY_PATHS) {
            val path = this.navigation.path
            when {
                path == null || path.debugData() == null -> null
                else -> DebugPathInfo(path.copy(), this.navigation.maxDistanceToWaypoint.toFloat())
            }
        }
        if (!this.brain.isBrainDead) {
            registration.register(DebugSubscriptions.BRAINS) {
                DebugBrainDump.takeBrainDump(level, this)
            }
        }
    }

    private fun isMovingSlowly(): Boolean {
        return this.isCrouching || this.isVisuallyCrawling
    }

    private fun tryToggleFlight(wasJumping: Boolean): Boolean {
        if (!this.abilities.mayfly) {
            return false
        }

        if (this.isSpectator) {
            if (!this.abilities.flying) {
                this.abilities.flying = true
                this.onUpdateAbilities()
                return true
            }
            return false
        }

        if (wasJumping || !this.input.jump) {
            return false
        }

        if (this.jumpTriggerTime == 0) {
            this.jumpTriggerTime = 7
            return false
        }
        if (this.isSwimming || this.vehicle != null && this.jumpableVehicle() == null) {
            return false
        }

        this.abilities.flying = !this.abilities.flying
        if (this.abilities.flying && this.onGround()) {
            this.jumpFromGround()
        }
        this.onUpdateAbilities()
        this.jumpTriggerTime = 0
        return true
    }

    private fun jumpableVehicle(): PlayerRideableJumping? {
        val vehicle = this.controlledVehicle
        return if (vehicle is PlayerRideableJumping && vehicle.canJump()) vehicle else null
    }

    private fun modifyInput(input: Vec2): Vec2 {
        if (input.lengthSquared() == 0.0F) {
            return input
        }

        var modified = input.scale(0.98F)
        if (this.isUsingItem && !this.isPassenger) {
            modified = modified.scale(this.itemUseSpeedMultiplier())
        }
        if (this.isMovingSlowly()) {
            modified = modified.scale(this.getAttributeValue(Attributes.SNEAKING_SPEED).toFloat())
        }
        return modifyInputSpeedForSquareMovement(modified)
    }

    private fun itemUseSpeedMultiplier(): Float {
        return this.useItem.getOrDefault(DataComponents.USE_EFFECTS, UseEffects.DEFAULT).speedMultiplier()
    }

    private fun isSlowDueToUsingItem(): Boolean {
        return this.isUsingItem
            && !this.useItem.getOrDefault(DataComponents.USE_EFFECTS, UseEffects.DEFAULT).canSprint()
    }

    private fun canStartSprinting(): Boolean {
        return !this.isSprinting
            && this.input.hasForwardImpulse()
            && this.isSprintingPossible(this.abilities.flying)
            && !this.isSlowDueToUsingItem()
            && (!this.isFallFlying || this.isUnderWater)
            && (!this.isMovingSlowly() || this.isUnderWater)
    }

    private fun isSprintingPossible(allowedInShallowWater: Boolean): Boolean {
        val vehicle = this.vehicle
        return !this.isMobilityRestricted
            && (if (vehicle != null) this.vehicleCanSprint(vehicle) else this.hasEnoughFoodToDoExhaustiveManoeuvres())
            && (allowedInShallowWater || !this.isInShallowWater)
    }

    private fun vehicleCanSprint(vehicle: Entity): Boolean {
        return vehicle.canSprint() && vehicle.isLocalInstanceAuthoritative
    }

    private fun shouldStopRunSprinting(): Boolean {
        return !this.isSprintingPossible(this.abilities.flying)
            || !this.input.hasForwardImpulse()
            || this.horizontalCollision && !this.minorHorizontalCollision
    }

    private fun shouldStopSwimSprinting(): Boolean {
        return !this.isSprintingPossible(true)
            || !this.isInWater
            || !this.input.hasForwardImpulse() && !this.onGround() && !this.input.shift
    }

    private fun moveTowardsClosestSpace(x: Double, z: Double) {
        val pos = BlockPos.containing(x, this.y, z)
        if (!this.suffocatesAt(pos)) {
            return
        }

        val xd = x - pos.x
        val zd = z - pos.z
        var closestDirection: Direction? = null
        var closest = Double.MAX_VALUE
        for (direction in HORIZONTAL_ESCAPE_DIRECTIONS) {
            val axisDistance = direction.axis.choose(xd, 0.0, zd)
            val distanceToEdge = if (direction.axisDirection == Direction.AxisDirection.POSITIVE) {
                1.0 - axisDistance
            } else {
                axisDistance
            }
            if (distanceToEdge < closest && !this.suffocatesAt(pos.relative(direction))) {
                closest = distanceToEdge
                closestDirection = direction
            }
        }

        if (closestDirection != null) {
            val movement = this.deltaMovement
            if (closestDirection.axis == Direction.Axis.X) {
                this.deltaMovement = Vec3(0.1 * closestDirection.stepX, movement.y, movement.z)
            } else {
                this.deltaMovement = Vec3(movement.x, movement.y, 0.1 * closestDirection.stepZ)
            }
        }
    }

    private fun suffocatesAt(pos: BlockPos): Boolean {
        val bounds = this.boundingBox
        val area = AABB(
            pos.x.toDouble(),
            bounds.minY,
            pos.z.toDouble(),
            (pos.x + 1).toDouble(),
            bounds.maxY,
            (pos.z + 1).toDouble()
        ).deflate(1.0E-7)
        return this.level().collidesWithSuffocatingBlock(this, area)
    }

    public companion object {
        private val HORIZONTAL_ESCAPE_DIRECTIONS = arrayOf(
            Direction.WEST, Direction.EAST, Direction.NORTH, Direction.SOUTH
        )

        private val joining = Object2ObjectOpenHashMap<String, CompletableFuture<out FakePlayer>>()

        private fun modifyInputSpeedForSquareMovement(input: Vec2): Vec2 {
            val length = input.length()
            if (length <= 0.0F) {
                return input
            }

            val direction = input.scale(1.0F / length)
            val modified = min(length * this.distanceToUnitSquare(direction), 1.0F)
            return direction.scale(modified)
        }

        private fun distanceToUnitSquare(direction: Vec2): Float {
            val x = abs(direction.x)
            val y = abs(direction.y)
            val tan = if (y > x) x / y else y / x
            return Mth.sqrt(1.0F + Mth.square(tan))
        }

        public fun join(server: MinecraftServer, profile: GameProfile): CompletableFuture<FakePlayer> {
            return this.join(server, profile, ::FakePlayer)
        }

        public fun <T: FakePlayer> join(
            server: MinecraftServer,
            profile: GameProfile,
            constructor: FakePlayerConstructor<T>
        ): CompletableFuture<T> {
            val connection = FakeConnection()
            // We simulate the fake login packet listener for luckperms compatability
            val login = FakeLoginPacketListenerImpl(server, connection, profile)
            connection.setupInboundProtocol(LoginProtocols.SERVERBOUND, login)
            return login.handleQueries().thenComposeAsync({
                val cookies = CommonListenerCookie.createInitial(profile, false)
                FakePlayerConfigurationTasks.prepareAndSpawnPlayer(server, profile, connection, cookies, constructor)
            }, server).thenApply { player ->
                player.entityData.set(DATA_PLAYER_MODE_CUSTOMISATION, 0x7F)
                server.connection.connections.add(connection)
                player.connection.handleAcceptPlayerLoad(ServerboundPlayerLoadedPacket())
                @Suppress("UNCHECKED_CAST")
                player as T
            }.whenComplete { _, throwable ->
                if (throwable != null) {
                    ArcadeUtils.logger.error("FakePlayer ${profile.name} failed to join", throwable)
                }
            }
        }

        public fun join(server: MinecraftServer, username: String): CompletableFuture<FakePlayer> {
            return this.join(server, username, ::FakePlayer)
        }

        public fun <T: FakePlayer> join(
            server: MinecraftServer,
            username: String,
            constructor: FakePlayerConstructor<T>
        ): CompletableFuture<T> {
            @Suppress("UNCHECKED_CAST")
            val future = this.joining.getOrPut(username) {
                val resolvable = DynamicResolvableProfile(username)
                resolvable.resolveProfile(server.services().profileResolver).thenComposeAsync({ resolved ->
                    this.join(server, resolved, constructor)
                }, server)
            }

            future.whenCompleteAsync({ _, throwable ->
                this.joining.remove(username, future)
                if (throwable != null) {
                    ArcadeUtils.logger.error("Couldn't resolve FakePlayer username: $username", throwable)
                }
            }, server)

            @Suppress("UNCHECKED_CAST")
            return future as CompletableFuture<T>
        }

        public fun <T: FakePlayer> join(
            server: MinecraftServer,
            uuid: UUID,
            constructor: FakePlayerConstructor<T>
        ): CompletableFuture<T> {
            val resolvable = DynamicResolvableProfile(uuid)
            return resolvable.resolveProfile(server.services().profileResolver).thenComposeAsync({ resolved ->
                if (resolved.name.isEmpty()) {
                    throw IllegalStateException("Resolved name was empty")
                }
                this.join(server, resolved, constructor)
            }, server)
        }

        public fun isJoining(username: String): Boolean {
            return this.joining.containsKey(username)
        }
    }
}
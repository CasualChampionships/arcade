# Usage

> Return to [table of contents](getting-started.md)

# Creating a Virtual Entity

Let's start by creating a virtual entity for a given dimension.
In order to create a virtual entity we need an `VirtualEntityAttachment`,
we can create an attachment that attaches our virtual entities
to a given dimension if we have a reference to a `ServerLevel`:
```kotlin
val level: ServerLevel = // ...
val attachment = level.createVirtualEntityAttachment(::SimpleVirtualEntityAttachment)

// later...
level.removeVirtualEntityAttachment(attachment)
```
We'll just use `SimpleVirtualEntityAttachment` for now, but we will
later discuss implementing our own attachment implementation.

Once we have an attachment instance we can create a virtual entity,
so let's create a virtual zombie entity:
```kotlin
val level: ServerLevel = // ...
val attachment = level.createVirtualEntityAttachment(::SimpleVirtualEntityAttachment)
val zombie = attachment.attach { SimpleVirtualEntity(EntityType.ZOMBIE, it) }
```
Again we're using a `SimpleVirtualEntity` for now, but we will discuss other
virtual entity types later.

We can modify some of the basic data for this virtual entity:
```kotlin
zombie.position = VirtualPosition.Absolute(Vec3(0.0, 100.0, 0.0))
zombie.rotation = VirtualRotation.Absolute(Vec2(0.0F, 90.0F))

zombie.setOnFire(true)
zombie.setDataEntry(EntityDataAccessors.LivingEntity.ARROW_COUNT, 10)

val observer: ServerPlayer = // ...
zombie.setInvisibleFor(observer, true)
```
The position and rotation of the virtual entity can be absolute or relative. In this example
it doesn't matter since the origin of the world is set to be `(0, 0, 0)` facing `(0, 0)`, but
if our attachment was a specific entity then we can make the position/rotation relative to that entity.

We can set specific entity data, `SimpleVirtualEntity` provides methods for entity data
that is shared between all entity instances, e.g. `setOnFire`, but we can also set entity data
as long as we have a reference to its accessor. Arcade provides `EntityDataAccessors` which ships
with some of the entity data accessors, but you may need to mixin to access specific accessors.
We can also set entity data on a per-player basis, as shown by the `setInvisibleFor` method.

## Entity Attachments

Other than being able to attach your virtual entities to a dimension arcade also provides
a way for you to attach virtual entities to real entities, it's exactly the same as you
would with a dimension, except on an `Entity` instance:
```kotlin
val entity: Entity = // ...
val attachment = entity.createVirtualEntityAttachment(::SimpleVirtualEntityAttachment)
```

# Virtual Display Entities

Display entities are some of Minecraft's most powerful entities since they allow the rendering
of arbitrary models (with a client resource pack). If you're wanting to add new server-side
content it's likely you'll want to use custom models. Virtual entities mesh perfectly with
display entities as they aren't persistent and there is no need for them to exist in the 'real'
world, as well as the ability for virtual entities to have per-player specific data.

Arcade provides 4 classes, an abstract `SimpleVirtualDisplay` and then the subclasses 
`SimpleVirtualBlockDisplay`, `SimpleVirtualItemDisplay`, and `SimpleVirtualTextDisplay`,
which represent block, item, and text displays respectively. They work very similarly
to our previous example:
```kotlin
val attachment: VirtualEntityAttachment = // ...
val text = attachment.attach(::SimpleVirtualTextDisplay)
text.setText(Component.literal("Example text!"))
```

# Parent Entities

We previously mentioned that virtual entities support relative locations, this is because
grouping multiple virtual entities to compose more complex visuals is common.
We can achieve this behavior with parent virtual entities, where multiple child virtual
entities can be attached, and they will 'inherit' their position and rotation from the parent
(given they use relative position/rotations).

The other benefit of a virtual entity hierarchy is that it becomes easier to detach
grouped virtual entities from an attachment instead of needing to keep track of each 
entity individually and removing each one.

Arcade provides a simple implementation; `SimpleParentVirualEntity`. This implementation
doesn't have the parent exist as a 'fake' entity like other virtual entities, in that it
is not sent to the client, so you cannot modify its entity data. You can modify its position
and rotation, however.

```kotlin
val parent = attachment.attach(::SimpleParentVirtualEntity)
parent.position = VirtualPosition.Absolute(Vec3(300.0, 100.0, 0.0))
val shape = RegularPolygonShape(Vec3.ZERO, 3.0, 10)
for (point in shape) {
    val slime = parent.attach { SimpleVirtualEntity(EntityType.SLIME, it) }
    slime.position = VirtualPosition.Relative(point)
    slime.rotation = VirtualRotation.Absolute(point.rotationAnglesTowards(Vec3.ZERO))
}

// later...
parent.position = VirtualPosition.Absolute(Vec3(0.0, 100.0, 300.0))
```
In the above example we create a parent virtual entity, set its position to be `(300, 100, 0)`,
then we create a polygon shape (essentially a circle) with a 3 block radius centered around 
`(0, 0, 0)`, which when iterated gives points around the circle (the `RegularPolygonShape` class
is part of the `arcade-visuals` module). For each of these points on the polygon we create a
virtual slime entity and set its position to be relative at the point and facing towards the
center of the polygon.

When a player starts observing the entities the slimes will be arranged in a circle around the
parents location facing inwards, when we update the parents position later the slimes will move
with it.

# Custom Attachments

Custom attachments allow you to encapsulate your virtual entity behaviour, and provide methods
for modifying your virtual entities.
The benefit of doing this in your attachment implementation and not a parent virtual entity is
because you'll usually want to keep track of your attachment to be able to remove it once
you're done with it. Having to also then keep a reference to a specific virtual entity can
start to get messy.

```kotlin
class CustomVirtualEntityAttachment(anchor: AttachmentAnchor): SimpleVirtualEntityAttachment(anchor) {
    private val warnings = Object2ObjectOpenHashMap<BlockPos, SimpleVirtualTextDisplay>()

    private var tick = 0

    fun warn(position: BlockPos) {
        if (!this.warnings.containsKey(position)) {
            val entity = this.attach(::SimpleVirtualTextDisplay)
            entity.position = VirtualPosition.Absolute(position.center)
            entity.setText(Component.literal("!!").yellow())
            this.warnings[position] = entity
        }
    }

    fun unwarn(position: BlockPos) {
        val entity = this.warnings[position] ?: return
        this.detach(entity)
    }

    override fun updateAttached() {
        this.tick += 1
        val isBold = (this.tick / 20) % 2 == 0
        for (entity in this.warnings.values) {
            entity.modifyText { original ->
                original.copy().withStyle { s -> s.withBold(isBold) }
            }
        }
    }
}
```
In the example above we can place warnings (virtual text entity) at block positions.
We cannot place multiple warnings at the same position and every second the warnings
change **boldness**.

The example attachment could then be used as follows:
```kotlin
object OverworldWarningHandler {
    private lateinit var attachment: CustomVirtualEntityAttachment
    
    fun foo() {
        this.attachment.warn(BlockPos.ZERO)
    }
    
    fun registerEvents() {
        GlobalEventHandler.Server.register<ServerStartEvent> { (server) ->
            this.attachment = server.overworld().createVirtualEntityAttachment(::CustomVirtualEntityAttachment)
        }
    }
}
```
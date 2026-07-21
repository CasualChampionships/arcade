# Virtual Entities

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

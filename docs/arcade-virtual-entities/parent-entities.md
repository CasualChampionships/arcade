# Parent Entities

We previously mentioned that virtual entities support relative locations, this is because
grouping multiple virtual entities to compose more complex visuals is common.
We can achieve this behavior with parent virtual entities, where multiple child virtual
entities can be attached, and they will 'inherit' their position and rotation from the parent
(given they use relative position/rotations).

The other benefit of a virtual entity hierarchy is that it becomes easier to detach
grouped virtual entities from an attachment instead of needing to keep track of each 
entity individually and removing each one.

Arcade provides a simple implementation; `SimpleParentVirtualEntity`. This implementation
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
is part of the `arcade-utils` module). For each of these points on the polygon we create a
virtual slime entity and set its position to be relative at the point and facing towards the
center of the polygon.

When a player starts observing the entities the slimes will be arranged in a circle around the
parents location facing inwards, when we update the parents position later the slimes will move
with it.

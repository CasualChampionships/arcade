# Entity and Player Extensions

Entity and player extensions need additional behavior because they can be re-constructed
when travelling between dimensions, respawning, or converting to other entity types.
In these cases we need to effectively transfer our extension to the new entity.

Both `EntityExtension` and `PlayerExtension` implement `TransferableEntityExtension`
which defines a `transfer` method to override. `EntityExtension`s require you to
override this method, `PlayerExtension`s however have a default implementation which
is to transfer the extension as-is. This can be done because the `PlayerExtension`
doesn't hold a reference to the `ServerPlayer` object itself but the 
`ServerGamePacketListenerImpl` which holds a reference to the current player.

Here's an example:
```kotlin
class MyEntityExtension(entity: Entity): EntityExtension(entity) {
    var persistentData = 0
    var transientData = 0

    override fun transfer(
        entity: Entity,
        reason: EntityTransferReason,
        delayed: DelayedActions
    ): Extension {
        val transferred = MyEntityExtension(entity)
        transferred.persistentData = this.persistentData
        return transferred
    }
}
```

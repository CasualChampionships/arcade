# Custom Attachments

Custom attachments allow you to encapsulate your virtual entity behaviour, and provide methods
for modifying your virtual entities.
The benefit of doing this in your attachment implementation and not a parent virtual entity is
because you'll usually want to keep track of your attachment to be able to remove it once
you're done with it. Having to also then keep a reference to a specific virtual entity can
start to get messy.

```kotlin
class CustomVirtualEntityAttachment(
    anchor: AttachmentAnchor,
    observers: ObserverTracker
): SimpleVirtualEntityAttachment(anchor, observers) {
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

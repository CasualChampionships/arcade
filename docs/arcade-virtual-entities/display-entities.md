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

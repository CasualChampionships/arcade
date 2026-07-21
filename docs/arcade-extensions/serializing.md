# Serializing Extensions

If we want data in our extension to be serializable and to be able to be
saved and reloaded then we need to implement the `SerializableExtension`
which itself also extends `Extension`.

Our extension now needs to override three methods, `serialize()`, `deserialize()`, and
`id()`:

```kotlin
class MyLevelExtension: SerializableExtension {
    var lastModifiedBlockPos: BlockPos? = null
    
    override fun serialize(output: ValueOutput) {
        output.storeNullable("last_modified_pos", BlockPos.CODEC, this.lastModifiedBlockPos)
    }
    
    override fun deserialize(input: ValueInput) {
        this.lastModifiedBlockPos = input.read("last_modified_pos", BlockPos.CODEC).getOrNull()
    }
    
    override fun id(): Identifier {
        return Identifier("example", "my_level_extension")
    }
    
    // ...
}
```

Registering these extensions are exactly the same, any class that can be extended
will automatically handle calling the methods for serializing and deserializing your data.

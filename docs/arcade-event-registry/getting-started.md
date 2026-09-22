# Event Registry

The event registry is the foundation of Arcade's event system. It provides the
`GlobalEventHandler`, the `ListenerRegistry`, and the core `Event` types that the
rest of Arcade's modules build upon.

This module only contains the event dispatching infrastructure, the actual
server and client events are provided by the
[Server Events](../arcade-events-server/getting-started.md) and
[Client Events](../arcade-events-client/getting-started.md) modules respectively.

## Adding to Dependencies

<!--@include: ../joystick.md#usage-->

See [Joystick](../joystick.md) for what the plugin does and its options.

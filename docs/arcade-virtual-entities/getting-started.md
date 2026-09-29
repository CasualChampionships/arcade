# Virtual Entities

Arcade's virtual entity api adds support for creating 'fake' entities that
don't really in the server-side world, but does properly emulate 'real'
entities on the client.

Virtual entities are essentially just a lightweight shell that allow for
full customizability, perfect for non-persisting visual entities,
also allowing for per-player customization.

This api has many overlapping features with [Polymer](https://github.com/Patbox/polymer)'s 
virtual entity api, but aims to fix some of the complexities with polymer's implementation.

## Adding to Dependencies

<!--@include: ../joystick.md#usage-->

See [Joystick](../joystick.md) for what the plugin does and its options.

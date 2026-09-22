# Observers

The observers module keeps track of which "observers" are currently observing
a given entity or level. This includes all players who are within tracking
range, but also other observers who wish to listen in on packets being sent
by the target.
This module is useful when working with packet-based visuals (e.g. virtual entities).

## Adding to Dependencies

<!--@include: ../joystick.md#usage-->

See [Joystick](../joystick.md) for what the plugin does and its options.

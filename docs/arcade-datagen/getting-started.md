# Datagen

Arcade's datagen api currently is aimed towards generating data which will be used in
resource packs which can't be done at the server's runtime, this mainly includes handling
translations.

## Adding to Dependencies

<!--@include: ../joystick.md#usage-->

See [Joystick](../joystick.md) for what the plugin does and its options.

You probably do not want to depend on this in your main server project but instead
have a separate project dedicated to data generation as this api contains client code.

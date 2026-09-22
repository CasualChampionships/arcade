# Interceptor

The interceptor module allows you to intercept inbound traffic on a connection's
Netty pipeline before it reaches Minecraft's own handlers. This can be used, for
example, to serve HTTP responses over the same port as the Minecraft server.

## Adding to Dependencies

<!--@include: ../joystick.md#usage-->

See [Joystick](../joystick.md) for what the plugin does and its options.

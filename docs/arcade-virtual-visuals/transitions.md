# Transitions

There are some other small miscellaneous visual elements that Arcade provides.

In minigames, it's common to want a countdown. Arcade provides an interface for 
easily creating these. There is a generic `Transitions` interface, as well as a 
`TitledCountdown` interface.

To create a titled countdown we can simply call `TitledCountdown#titled`, you can 
optionally pass in your title if you wish it to be different from the default:

```kotlin
val countdown = TitledCountdown.titled()
```

Then to initiate the countdown you call `Transition#transition` which is a suspending
function which takes a duration and interval between transition updates (counts in this case),
and suspends until the transition is fully complete.
By default, the duration will be 10 seconds, the interval will be 1 second.

```kotlin
val server: MinecraftServer = // ...
server.launch {
    countdown.transition(5.Seconds, 1.Seconds) { server.players }
    println("Countdown finished!")
}
```

If you want more control over the formatting and display of the `TitledCountdown` 
you can create your own implementation and override the respective methods.

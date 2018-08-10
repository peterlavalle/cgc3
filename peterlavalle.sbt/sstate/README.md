
Source mirror; https://github.com/g-pechorin/sstate.scala

Releases (tags) https://github.com/g-pechorin/sstate.scala/tags

```
	"com.peterlavalle" %% "sstate" % ???

	'com.peterlavalle:sstate_2.12:???'
```


# [SState][ghSState]

[SState][ghSState] is a cute little pure-functional FSM class in Scala that I wrote after seeing [JEasyStates][jes].
The only real advantage of mine is that it can (probably) be dropped into a [Scala.JS][jsScala] project without any magic.

There's a unit test at the foot of this doc showing how I test it.

[ghSState]: https://github.com/g-pechorin/sstate.scala
[jsScala]: https://www.scala-js.org/
[jes]: https://github.com/j-easy/easy-states

## terminology

- **machine** sort of the *class* or *kind* defining the nature of the FSM
- **ghost** some value that's carried around by *shell* objects of the *machine*
	- *ghost* values can be changed when the shell reacts to an event
- **shell** an instance of a *machine* which carries a *ghost*
	- yes; i'm proud of this naming
- **state** a state which a *shell* can be in
	- I use `case object` in the demo, but, could use `case class`
- **event** something that is/can be sent to the machine to trigger a change
	- I use `case object` in the demo, but, could use `case class`

Machines are immutable, so, when replacing a ghost durring a transition there's no way to directly[^indirectRaise] raise another event.

[^indirectRaise]: Various approaches which wrap this system are feasible, but, none are interesting to the author.

## usage

1. create a machine with appropriate generic parameters 
1. add some states with `addState(...)` so that they can be used in transitions
1. define what to do on transitions `onEvent(...)` to define actions to take on transitions
1. use `.transition` as a shorthand for situations where the `ghost` doesn't need to be changed
1. keep adding events and handlers until you've captured the entirity of the FSM which you're creating
1. create a shell with `apply(...)` giving it an initial state and an intial ghost
1. do what you will with this thing

In theory this system would be super-fun to orchestrate through a DSL or to use as the endpoint of some fearsome threaded goddess.

```
			// start a new FSM
			Machine[Int, TEvent, TState]()

				// we can be in a state of LOCKED
				.addState(MachineTest.Locked)

				// we can be in a state of UNLOCKED
				.addState(MachineTest.UnLocked)

				// when we're LOCKED, if COIN then become UNLOCKED and modify our ghost
				.onEvent(MachineTest.Locked, MachineTest.Coin, MachineTest.UnLocked)(_ + 1)

				// when we're UNLOCKED, if PUSH, become LOCKED and don't do anything
				.transition(MachineTest.UnLocked, MachineTest.Push, MachineTest.Locked)

				// create an instance of the machine with a ghost=0
				// ... the machine
				.apply(MachineTest.Locked, 0)
```

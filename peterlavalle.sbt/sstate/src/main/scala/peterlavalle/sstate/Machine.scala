package peterlavalle.sstate

trait Machine[G, S <: Machine.TState, E <: Machine.TEvent] extends ((S, G) => Machine.Shell[G, S, E]) {

	private type Handler = Option[(S, (G, S, E, S) => G)]

	/**
		* add a state that the machine may assume
		*/
	def addState(state: S): Machine[G, S, E] = {
		assume(!states.contains(state))
		val self: Machine[G, S, E] = this
		new Machine[G, S, E] {
			override protected def parent: Machine[G, S, E] = self

			override protected val states: Set[S] = super.states + state
		}
	}

	/**
		* defines a simple transition with exact matches and quick replacement
		*/
	def onEvent(from: S, event: E, into: S)(handler: G => G): Machine[G, S, E] = {
		assume(states.contains(from))
		assume(handle(from, event).isEmpty)
		onEvent(
			(_: S) == from,
			(_: E) == event,
			into
		) {
			(g: G, _: S, _: E, _: S) =>
				handler(g)
		}
	}

	/**
		* defines a transition with less exacting matches and a more nuanced replacement option
		*/
	def onEvent(active: S => Boolean, filter: E => Boolean, into: S)(handler: (G, S, E, S) => G): Machine[G, S, E] = {

		assume(states.contains(into))

		val self: Machine[G, S, E] = this
		new Machine[G, S, E] {
			override protected def parent: Machine[G, S, E] = self

			override protected def handle(f: S, e: E): Handler =
				if (active(f) && filter(e))
					Some((into, handler))
				else
					parent.handle(f, e)
		}
	}

	def transition(from: S, event: E, into: S): Machine[G, S, E] = {
		onEvent(from, event, into) {
			old: G =>
				old
		}
	}

	def apply(state: S, ghost: G): Machine.Shell[G, S, E] = {
		Machine.Shell(
			this,
			state,
			ghost
		)
	}

	protected def parent: Machine[G, S, E]

	protected def states: Set[S] = parent.states

	protected def handle(from: S, event: E): Handler = parent.handle(from, event)
}

object Machine {

	/**
		* Lets you match on the state/ghost
		*
		* See http://peterlavalle.com/post/scala-extractor/
		*/
	def unapply[G, E <: TEvent, S <: TState](value: Shell[G, S, E]): Option[(S, G)] = {
		Some(
			value.state,
			value.ghost
		)
	}

	def apply[G, S <: TState, E <: TEvent](): Machine[G, S, E] = {
		new Machine[G, S, E] {
			override protected val states: Set[S] = Set()

			override protected def handle(from: S, event: E): Option[(S, (G, S, E, S) => G)] = None

			override protected def parent: Machine[G, S, E] =
				sys.error("This is the root-machine. This accessor shouldn't be invoked.")
		}
	}

	trait TEvent

	trait TState

	case class Shell[G, S <: TState, E <: TEvent]
	(
		machine: Machine[G, S, E],
		state: S,
		ghost: G
	) {
		def !(event: E): Shell[G, S, E] =
			machine.handle(state, event) match {
				case None =>
					this
				case Some((into: S, handler)) =>
					Shell[G, S, E](
						machine,
						into,
						handler(ghost, state, event, into)
					)
			}
	}
}

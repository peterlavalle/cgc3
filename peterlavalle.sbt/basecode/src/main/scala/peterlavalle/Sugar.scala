package peterlavalle

/**
  * defines base traits that're used for syntactical sugar
  */
object Sugar {

  trait FSlash[I, O] {
    def /(i: I): O
  }

  trait BSlash[I, O] {
    def \(i: I): O
  }

  trait PlusEquals[I, O] {
    def +=(i: I): O
  }

  trait Bang[I, O] {
    def !(i: I): O
  }

  trait Question[I, O] {
    def ?(i: I): O
  }

  trait InTo[I, O] {
    def <<(i: I): O
  }

  trait OutOf[I, O] {
    def >>(i: I): O
  }

}

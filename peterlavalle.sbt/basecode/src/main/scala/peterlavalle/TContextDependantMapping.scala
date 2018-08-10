package peterlavalle

trait TContextDependantMapping {

  import scala.collection.immutable.Stream.Empty

  type FMapping[S, O] = (List[O], S, Stream[S]) => O
  type FExpansion[S] = (List[S], S, Stream[S]) => Option[Iterable[S]]

  implicit class ContextDependantWrapping[S](todo: Iterable[S]) {
    def contextMapping[O](mapping: FMapping[S, O]): Stream[O] =
      contextMap(Nil)(mapping)

    def contextMap[O](start: List[O])(mapping: FMapping[S, O]): Stream[O] =
      todo match {
        case input: Stream[S] =>
          def recur(done: List[O], todo: Stream[S]): Stream[O] =
            todo match {
              case Empty => Empty
              case head #:: tail =>
                val next = mapping(done, head, tail)
                Stream.cons(
                  next,
                  recur(done ++ List(next), tail)
                )
            }

          recur(start, input)

        case _ =>
          // convert it to a stream
          todo.toStream.contextMapping(mapping)
      }


    /**
      * Expands an iterable, returns a stream to the expanded elements
      *
      * @param mapping method to check and see if we need more entries in this
      * @return None if this element can be processed, Some(stream) if the current stream needs to be replaces
      */
    def contextExpand(mapping: FExpansion[S]): Stream[S] =
      contextExpand(Nil)(mapping)

    /**
      * Expands an iterable, returns a stream to the expanded elements
      *
      * @param start   starting "done" list which will not be in the output
      * @param mapping method to check and see if we need more entries in this, or if we should change the done list
      * @return None if this element can be processed, Some(stream) if the current stream needs to be replaces
      */
    def contextExpand(start: List[S])(mapping: FExpansion[S]): Stream[S] =
      todo.toStream match {
        case Empty => Empty

        case input =>
          def recur(done: List[S], todo: Stream[S]): Stream[S] =
            todo match {
              case Empty => Empty

              case head #:: tail =>
                mapping(done, head, tail) match {
                  case None =>
                    Stream.cons(
                      head,
                      recur(done ++ List(head), tail)
                    )

                  case Some(replace: Stream[S]) =>
                    recur(done, replace)

                  case Some(list: List[S]) =>
                    recur(list, todo)
                }
            }

          recur(start, input)
      }
  }

}

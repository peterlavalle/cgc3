package peterlavalle

import scala.collection.immutable.Stream.Empty
import scala.util.matching.Regex

object CNode {

	sealed trait TNode

	sealed trait TValue extends TNode {
		def Access(propertyName: String): TValue =
			new Access(this, propertyName)
	}

	case class Access(node: TNode, propertyName: String) extends TValue

	case class Block(nodes: Stream[TNode]) extends TNode

	def Block(nodes: TNode*): Block =
		Block(nodes.toStream)

	case class Assign(into: LiteralIdentifier, value: TValue) extends TNode

	case class Call(nodes: Stream[TNode]) extends TValue

	def Call(what: TNode, tail: TNode*): Call =
		Call(what #:: tail.toStream)

	case class LiteralIdentifier(name: String) extends TValue

	case class LiteralNumber(value: String) extends TValue

	case class Value(value: TValue) extends TValue

	// def Value(value: TValue): TValue = value

	case object LiteralPassThrough extends TNode

	case class LiteralString(value: String) extends TValue

	def parse(stream: Stream[_]): TNode = {


		stream.head match {
			case _: Char =>

				def lines(done: List[Char], todo: Stream[Char]): Stream[String] =
					todo match {
						case Empty =>
							Stream(
								new String(done.reverse.toArray)
							)

						case ('\n' #:: tail) =>
							new String(done.reverse.toArray) #:: lines(Nil, tail)

						case (head #:: tail) =>
							lines(head :: done, tail)
					}


				parse(lines(Nil, stream.asInstanceOf[Stream[Char]]))


			case _: String =>
				def process(lines: Stream[(Int, String, String)]): TNode =
					lines match {
						case (_: Int, "Value", "PassthroughLiteral:") #:: Empty =>
							LiteralPassThrough

						case (depth: Int, "Block", "") #:: tail =>

							require(tail.nonEmpty)
							tail.foreach {
								case (d, a, c) =>
									require(depth < d, s"a = `$a`")
							}

							val args: Stream[List[(Int, String, String)]] = {

								implicit class PStream[T](value: Stream[T]) {
									def splitHeads(select: T => Boolean): Stream[List[T]] = {

										def recur(done: List[T], todo: Stream[T]): Stream[List[T]] =
											todo match {
												case Empty =>
													Stream(done.reverse)

												case h #:: t if select(h) =>
													if (done.nonEmpty)
														done.reverse #:: recur(List(h), t)
													else
														recur(List(h), t)

												case h #:: t =>
													recur(h :: done, t)
											}

										recur(Nil, value)
									}
								}


								tail.splitHeads(_._1 == (depth + 1))
							}

							val nodes: Stream[TNode] = {
								args.map(_.toStream).map(process)
							}

							Block(
								nodes
							)
					}


				val rLine: Regex = "(\t*)(\\w+)(.*?)[\r\t ]*" r

				process(stream.asInstanceOf[Stream[String]].filter("" != (_: String).trim).map {
					case rLine(indent, tag, data) =>
						(indent.length, tag, data.trim)
				})
		}
	}
}

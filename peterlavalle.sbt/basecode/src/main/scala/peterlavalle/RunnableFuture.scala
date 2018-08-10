package peterlavalle

import java.util

/**
  * Collects `Runnable` into a list that's then run at a later date
  */
class RunnableFuture extends Runnable {
  private var list: Option[util.List[Runnable]] = Some(new util.LinkedList[Runnable]())

  /**
    * pimp method, adds a lambda as a runnable
    *
    * @param lambda code to run
    * @throws RunnableFuture.AlreadyStartedException if the task was already started
    */
  def !(lambda: => Unit): Unit =
    add(
      new Runnable {
        override def run(): Unit = lambda
      }
    )

  /**
    * adds a runnable to the list if possible
    *
    * @param runnable the code
    * @throws RunnableFuture.AlreadyStartedException if the task was already started
    */
  def add(runnable: Runnable): Unit = {
    if (list.isEmpty)
      throw new RunnableFuture.AlreadyStartedException()
    else
      list.get.add(runnable)
  }

  /**
    * run everything via extraction
    */
  override def run(): Unit =
    extract().run()

  /**
    * mark us as dead and extract all of my runnable tasks into a runnable object
    *
    * @return a Runnable which runs all of our runnables
    */
  def extract(): Runnable =
    if (list.isEmpty)
      throw new RunnableFuture.AlreadyStartedException()
    else {
      val list = this.list.get
      this.list = None
      new Runnable {
        override def run(): Unit =
          list.foreach(_.run())
      }
    }
}

object RunnableFuture {

  def unapply(arg: RunnableFuture): Option[Runnable] =
    Some(arg.extract())

  class AlreadyStartedException() extends Exception("This has already been .extract()'ed")

}

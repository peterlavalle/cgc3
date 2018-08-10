package peterlavalle

@Deprecated()
class Feedback(prefix: String) {
  @Deprecated()
  def out(line: String): Unit = System.out.println(s"$prefix > $line")

  @Deprecated()
  def err(line: String): Unit = System.err.println(s"$prefix ! $line")
}

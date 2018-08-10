package peterlavalle.cgc3

import java.io.File

import peterlavalle.{TreeFolder, _}

import scala.language.implicitConversions

trait Yggdrasil {

	// TODO; should I require source languages to be explicitly declared? ... probably

	import Yggdrasil._

	/** Bind+Read
		*
		* builds the whatnot, and, returns the resulting binary directory
		*/
	def compile(obj: String): File

	/** Bind
		* adds a source folder to this model
		*/
	def src(key: String, dir: TreeFolder): Unit

	/** B
		* adds another project/thing to us
		*/
	def lib(get: Yggdrasil): Unit

	/** B
		* generate some source code ... or whatever ... before the named src is run
		*/
	def gen(src: String)(action: File => Unit): Unit

	/** Bind
		*
		* compile source language into something that's treated as generated source for output language
		*
		* @param src    is the name of the source language
		* @param out    is the name of the output language
		* @param action what we want performed on each step
		*/
	def run(src: String, out: String)(action: Compiler): Unit

	/** Bind
		*
		* asm - assemble something into the resulting output directory using the named source as input object code
		*
		* intent; you call it with "obj" to link all C/++ into an EXE
		* maybe; you call it with "class" to jar up all classfiles
		*/
	def asm(obj: String)(action: Assembler): Unit

	/** Bind
		*
		* eat - consume the results of assembling something
		*
		* @param obj    the name of the language to work on
		* @param action the thing to do on the output folder
		*/
	def eat(obj: String)(action: File => Unit): Unit

	/** Read
		* read all local sources for a given name. should trigger generation.
		*
		* @param key the key for the root
		* @return an iterable to scan the roots
		*/
	def sources(key: String): Iterable[TreeFolder]

	/** Read
		*
		* newest timestamp in
		* - named sources
		* - sources that they depend upon
		* - project configuration
		*/
	def lastModified(key: String): Long


	/** Read
		*
		* read all source (local or not) for a given name
		*
		* @param key
		* @return
		*/
	final def sourceTransitive(key: String): Iterable[TreeFolder] =
		sources(key) ++ imported.flatMap(_ sourceTransitive key)

	/** Read
		*
		* read all linked projects
		*
		* @return a collection of all things that we're linking into ourself
		*/
	def imported: Iterable[Yggdrasil]
}

object Yggdrasil {

	/**
		* takes an output folder and compiles a single (local) entry
		*/
	@deprecated(
		"switch to `type Compiler = (File, Yggdrasil, File, String) => Option[Error: String]`",
		"2018-07-10"
	)
	type Compiler = (File, Yggdrasil, File, String) => Unit
	/**
		* takes an output folder and builds someting in it from all source (everywhere!)
		*/
	@deprecated(
		"switch to `type Assembler = (File, Yggdrasil) => Unit`",
		"2018-07-10"
	)
	type Assembler = (File, Iterable[TreeFolder]) => Unit

	def apply(mkdir: String => File, last: => Long): Yggdrasil =
		new Yggdrasil {


			trait Source {
				def tree: TreeFolder
			}

			trait Leaf {
				def name: String

				srcNames(name) {

				}

				val bin: Veil.Value[File => Unit] = Veil.value((_: File) => ())

				val src: Veil.Value[List[Source]] = Veil.value(List())

				lazy val built: File = {

					val leaf = this

					// execute all generation tasks
					src.read.foreach(_.tree)

					// make our output directory
					val out: File = mkdir(s"${leaf.name}/out") EnsureExists

					// run our binary task!
					leaf.bin.read(out)

					// hey! we're DONE!
					out
				}
			}

			val leaf: String => Leaf =
				MemoMap.of {
					key: String =>
						new Leaf {
							override def name: String = key
						}
				}

			val libs: Veil.Value[List[Yggdrasil]] = Veil.value(List())

			/** Bind+Read
				*
				* builds the whatnot, and, returns the resulting binary directory
				*/
			override def compile(obj: String): File =
				leaf(obj).built

			/** Bind
				* adds a source folder to this model
				*/
			override def src(key: String, dir: TreeFolder): Unit =
				leaf(key).src.swap(new Source {
					override def tree: TreeFolder = dir
				} :: _)

			/** B
				* adds another project/thing to us
				*/
			override def lib(get: Yggdrasil): Unit =
				libs.swap(get :: _)

			/** B
				* generate some source code ... or whatever ... before the named src is run
				*/
			override def gen(src: String)(action: File => Unit): Unit =
				leaf(src).src.swap(new Source {
					override lazy val tree: TreeFolder = {

						// make us a folder
						val gen: File = mkdir(s"$src/gen") EnsureExists

						// run the generator
						action(gen)

						// add the folder as an output
						gen
					}
				} :: _)

			/** Bind
				*
				* compile source language into something that's treated as generated source for output language
				*
				* @param src    is the name of the source language
				* @param out    is the name of the output language
				* @param action what we want performed on each step
				*/
			override def run(src: String, out: String)(action: Compiler): Unit = srcNames(src, out) {

				System.err.println(s"TODO; prevent name collisionts on ($src,$out)")

				// make us a folder
				val gen: File = mkdir(s"$src/src") EnsureExists

				// add the folder as an output
				this.src(out, gen)

				// build the source
				this.gen(out) {
					_ =>
						sources(src).flatten.foreach {
							case (root, path) =>
								action(
									gen,
									this: Yggdrasil,
									root.root,
									path
								)
						}
				}
			}

			/** Bind
				*
				* asm - assemble something into the resulting output directory using the named source as input object code
				*
				* intent; you call it with "obj" to link all C/++ into an EXE
				* maybe; you call it with "class" to jar up all classfiles
				*/
			override def asm(obj: String)(action: Assembler): Unit =
				leaf(obj).bin.swap(_ >> {
					out: File =>
						libs.read.foreach(_ compile obj)
						action(out, sourceTransitive(obj))
				})

			/** Bind
				*
				* eat - consume the results of assembling something
				*
				* @param obj    the name of the language to work on
				* @param action the thing to do on the output folder
				*/
			override def eat(obj: String)(action: File => Unit): Unit =
				leaf(obj).bin.swap((_: File => Unit) << action)

			/** Read
				* read all local sources for a given name. should trigger generation.
				*
				* @param key the key for the root
				* @return an iterable to scan the roots
				*/
			override def sources(key: String): Iterable[TreeFolder] =
				leaf(key).src.read.map((_: Source).tree)

			/** Read
				*
				* read all linked projects
				*
				* @return a collection of all things that we're linking into ourself
				*/
			override def imported: Iterable[Yggdrasil] = libs.read

			override def lastModified(key: String): Long = {

				val leafs: Stream[Leaf] =
					sys.error("lookup leafs")

				leafs.foldLeft(imported.map((_: Yggdrasil).lastModified(key)).foldLeft(last)(Math.max)) {
					case (last: Long, next) =>
						next.src.value.map((_: Source).tree.lastModified)
							.filter((_: Option[Long]).nonEmpty).map((_: Option[Long]).get).foldLeft(last)(Math.max)
				}
			}
		}

	def srcNames[O](src: String*)(action: => O): O = {

		// check that names are okay
		src.foreach {
			src =>
				require("out" != src)
				require("gen" != src)
				require("src" != src)
		}

		action
	}
}

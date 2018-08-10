package peterlavalle.cgc3

import junit.framework.TestCase
import org.easymock.{EasyMock, IExpectationSetters}

import scala.reflect.ClassTag

trait TTestCase {
	this: TestCase =>

	implicit class pMockClass[T](clazz: Class[T]) {
		def createMock: T = EasyMock.createMock(clazz)

		def createMock(name: String): T = EasyMock.createMock(name, clazz)

		def returnOnce[T](value: T): IExpectationSetters[T] = EasyMock.expectLastCall[T]().andReturn(value).once()
	}

	implicit class pMockObject[T](value: T) {

		def returnOnce: IExpectationSetters[T] = EasyMock.expectLastCall[T]().andReturn(value).once()
	}

	implicit class pEquality[T](expected: T) {
		def ===(actual: T): Unit = {
			org.junit.Assert.assertEquals(
				expected,
				actual
			)
		}
	}

	def assertThrows[T <: Throwable](block: => Unit)(implicit exceptionTag: ClassTag[T]): T = {
		try {
			block
			failed("an exception should have been thrown")
		} catch {
			case e: T if exceptionTag.runtimeClass.isInstance(e) =>
				e
		}
	}

	def failed(message: String): Nothing = {
		val e: AssertionError = new AssertionError(message)
		e.setStackTrace(e.getStackTrace.drop(3))
		throw e
	}

	def assertEquals[T](expected: => T, actual: T, message: String = null): T = {
		val e: T = expected
		try {
			if (e != actual)
				org.junit.Assert.assertEquals(
					message,
					e,
					actual
				)
			actual
		} catch {
			case e: AssertionError =>
				e.setStackTrace(e.getStackTrace)
				throw e
		}
	}
}



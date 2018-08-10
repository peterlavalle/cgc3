package peterlavalle.cgc3.signal

import org.junit.Assert._
import peterlavalle.ATestCase

class SignalDefinitionTest extends ATestCase {

	def testBasic(): Unit = {

		def source: String =
			"""
				|
				|#pragma once
				|
				|#include <pal-signal-node.hpp>
				|
				|SIGNAL(foo)
				|{
				|	INPUT(a, int32_t)
				|	OUTPUT(b, float)
				|	OUTPUT(c, double)
				|
				|	EVENT(labdoop)
				|};
				|
			""".stripMargin

		def expected: SignalDefinition =
			SignalDefinition(
				"foo",
				Set(
					SignalDefinition.Input("a", "int32_t")
				),
				Set(
					SignalDefinition.Output("b", "float"),
					SignalDefinition.Output("c", "double")
				),
				Set(
					SignalDefinition.Event("labdoop")
				)
			)

		assertEquals(
			SignalDefinition.parse(source).open,
			expected
		)
	}
}

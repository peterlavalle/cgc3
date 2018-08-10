package peterlavalle

class TestParseNodes extends ATestCase with TTestCase {
	override def nodeTree: CNode.TNode =
		CNode.Block(
			CNode.LiteralPassThrough,
			CNode.Value(
				CNode.Call(
					CNode.LiteralIdentifier("require"),
					CNode.LiteralString("std")
				)
			),

			CNode.Assign(
				CNode.LiteralIdentifier("points"),
				CNode.Value(
					CNode.Call(
						CNode.LiteralIdentifier("std")
							.Access("in")
							.Access("readline"),
						CNode.LiteralIdentifier("std")
							.Access("int32")
					)
				)
			),

			CNode.Assign(
				CNode.LiteralIdentifier("points"),
				CNode.Value(
					CNode.Call(
						CNode.Value(
							"""
								|          Range
								|            Value IdentifierLiteral: points
								|            Value NumberLiteral: 1
								|          Access PropertyName: map
							""".stripMargin.halt
						),
						"""
							|        Code
							|          Param IdentifierLiteral: point
							|          Block
							|            Value
							|              Call
							|                Value IdentifierLiteral: std
							|                  Access PropertyName: in
							|                  Access PropertyName: readline
							|                Value IdentifierLiteral: std
							|                  Access PropertyName: int32
							|                Value IdentifierLiteral: std
							|                  Access PropertyName: int32
						""".stripMargin.halt
					)
				)
			)
		)
}

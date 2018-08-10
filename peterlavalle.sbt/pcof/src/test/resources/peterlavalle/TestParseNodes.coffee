
#
# first part of codinggame's lander example, in my idealised pure-functional-coffeescript
# λ coffee -n lander.coffee > lander.n

# need to require if we're going to use this
require 'std'

# reads an int32 and (implicitly) updates the std monad
points = std.in.readline(std.int32)

# calls a function on the named range
points =
	[points .. 1].map (point)->
		# reads two numbers and returns them as a tuple
		# compilation causes this function to be lifted from `int -> (int, int)` to `std -> int -> (std, (int, int))` include and update the std.in monad
		std.in.readline(std.int32, std.int32)


#pragma once

#include "signal-node.hpp"

SIGNAL(foo, bar)
{
	INPUT(a, int32_t)
	OUTPUT(b, float)
	OUTPUT(c, double)

	EVENT(labdoop)
};

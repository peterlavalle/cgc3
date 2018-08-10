
#pragma once

#include <duktape.h>

namespace coffee
{
	void compile(duk_context*);
	void install(duk_context*, const size_t, const void*);
}


#include <iostream>

#include <vector>

#include <stdint.h>
#include <string.h>
#include <string>

#include <list>
#include <string>
#include <fstream>
#include <streambuf>

#include <duktape.hpp>

#include <tin.h>

#include <coffee.hpp>

#include "coffeescript-test.hpp"


int main(int argc, char* argv[])
{
	std::cerr << "TODO; run the coffee and test some thing/s" << std::endl;
	{
		// create a duktape
		duk::vm vm;

		// install coffee-script
		tin("coffeescript-2.3.1.js", &vm, [](void* _vm, const char* name, const size_t size, const void* data)
		{
			duk::vm& vm = *((duk::vm*)_vm);
			coffee::install(vm, size, data);
		});

		// create our log object
		std::list<std::string> logged;
		vm.object_push<std::list<std::string>>(&logged, [](std::list<std::string>* logged) { /*don't actually do anything here*/ } );
		vm.object_method<std::list<std::string>>("out", 1, [](std::list<std::string>& logged, duk::vm& vm)
		{
			FAILURE("TODO; whatever");
			return 0;
		});
		duk_put_global_string(vm, "log");

		// cool ... now load the coffeescript
		tin("script.coffee", &vm, [](void* _vm, const char* name, const size_t size, const void* data)
		{
			duk::vm& vm = *((duk::vm*)_vm);

			// push the coffee-script source
			duk_push_lstring(vm, (const char*) data, size);

			// push the name
			duk_push_string(vm, name);

			// run the compile
			coffee::compile(vm);
		});
		
		// right ... run it
		duk_call(vm, 0);

		FAILURE("TODO; check for results having been logged");
	}
	return EXIT_SUCCESS;
}

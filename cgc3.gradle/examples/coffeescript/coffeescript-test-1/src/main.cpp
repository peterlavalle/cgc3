
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

#define TIN_BEGIN(NAME, SIZE, SPAN) const size_t coffeescript_zip_span = SPAN; const uint8_t coffeescript_zip_data[] = {
#define TIN_CLOSE(NAME, SIZE, SPAN) }; const size_t coffeescript_src_size = SIZE;

#include "coffeescript-2.3.1.js.h"

#include "tinfl.h"

int main(int argc, char* argv[])
{
	{
		auto failure_message = "tin'ed data was the wrong size";
		ASSERT_EQUAL(397121, coffeescript_src_size);
	}

	size_t coffeescript_len;
	auto coffeescript_txt = (const char*)
			tinfl_decompress_mem_to_heap(
			coffeescript_zip_data, coffeescript_zip_span,
			&coffeescript_len, 0);

	ASSERT_EQUAL(coffeescript_src_size, coffeescript_len);

	std::string coffeescript_src = std::string(coffeescript_txt, coffeescript_len);
	
	ASSERT_EQUAL(coffeescript_src_size, strlen(coffeescript_src.c_str()));
	ASSERT_EQUAL(coffeescript_src_size, coffeescript_src.size());

	size_t expected_size;
	std::string expected_text = fstring("coffeescript/src/coffeescript-2.3.1.js", expected_size);

	ASSERT_EQUAL(expected_size, coffeescript_src_size);
	ASSERT_EQUAL(expected_size, expected_text.size());

	ASSERT_SEQUAL(expected_text, coffeescript_src);

	std::cerr << "TODO; tes the un-tinning of coffee-script" << std::endl;

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
	return EXIT_FAILURE;
}

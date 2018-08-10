
#pragma once

#include "duktape.h"

namespace duk
{
	/// kind of a smart-pointer for the duktape vm
	class vm final
	{
		duk_context* _vm;
	public:
		vm(const vm&) = delete;
		void operator=(const vm&) = delete;

		vm(void) :
			_vm(duk_create_heap_default())
		{	
		}

		~vm(void)
		{
			duk_destroy_heap(_vm);
		}

		template<typename T, typename ... ARGS>
		void object_new(ARGS&&...);

		template<typename T>
		void object_push(T* pointer, void(*cleanup)(T*) = [](T* self) { delete self; });

		template<typename T>
		void object_method(const char*, duk_int_t nargs, duk_ret_t (*method)(T&, vm&));
		
		operator duk_context*(void) { return _vm; }
	};
}

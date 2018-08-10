package peterlavalle.smolir

class ArrayTest extends TTestCase {
	val foo =
		SmolIr.Prototype(
			"foo",
			List(
				SmolIr.TCall.Arg("arg0_size", SmolIr.KindSize),
				SmolIr.TCall.Arg("arg0_head", SmolIr.KIntU16.Ptr)
			),
			SmolIr.KVoid
		)

	override def smol =
		"""
						|module arr: ay
						|{
						| def foo(:[size_t uint16])
						| def bar(:[uint32 *uint64, real32])
						|}
				""".stripTrim

	override def treeCode =
		SmolIr.Module(
			"arr", "ay",
			// don't do this "in general"
			expandedCalls
		)

	override def expandedCalls =
		List(
			foo,
			SmolIr.Prototype(
				"bar",
				List(
					SmolIr.TCall.Arg("arg0_size", SmolIr.KindIntU32),
					SmolIr.TCall.Arg("arg0_head_0", SmolIr.KindIntU64.Ptr.Ptr),
					SmolIr.TCall.Arg("arg0_head_1", SmolIr.KindReal32.Ptr)
				),
				SmolIr.KVoid
			)
		)

	override def header =
		'\n' +
			"""
								|#pragma once
								|
								|#include <stdint.h>
								|
								|struct arr
								|{
								|// calls
								|	void* _foo;
								|	static void foo(size_t arg0_size, uint16_t* arg0_head);
								|	void* _bar;
								|	static void bar(uint32_t arg0_size, uint64_t** arg0_head_0, float* arg0_head_1);
								|// initialiser
								|	static void def(void*, void*(*)(void*, const char*), const char*);
								|};
								|#if defined(smol_cpp)
						""".stripTrim

	override def expandedEnums =
		Nil

	override def labels =
		List(
			"foo",
			"bar"
		)

	override def loader =
		'\n' +
			"""
								|#include "arr.hpp"
								|
								|void smol_code(size_t count, const char* prefix, void** ptr, void* userdata, void*(*callback)(void*, const char*), const char* allnames);
								|
								|arr _arr;
								|
								|void arr::def(void* userdata, void*(*callback)(void*, const char*), const char* allnames)
								|{
								|	smol_code(
								|		2,
								|		"ay",
								|		reinterpret_cast<void**>(&(_arr)),
								|		userdata,
								|		callback,
								|		allnames
								|	);
								|}
								|
								|#include <assert.h>
								|#ifdef WIN32
								|#	define SMOL_CALL __stdcall*
								|#else
								|#	define SMOL_CALL *
								|#endif
								|void arr::bar(uint32_t arg0_size, uint64_t** arg0_head_0, float* arg0_head_1)
								|{
								|	(reinterpret_cast<void(SMOL_CALL)(uint32_t, uint64_t**, float*)>(_arr._bar))(arg0_size, arg0_head_0, arg0_head_1);
								|}
								|void arr::foo(size_t arg0_size, uint16_t* arg0_head)
								|{
								|	(reinterpret_cast<void(SMOL_CALL)(size_t, uint16_t*)>(_arr._foo))(arg0_size, arg0_head);
								|}
						""".stripTrim
}

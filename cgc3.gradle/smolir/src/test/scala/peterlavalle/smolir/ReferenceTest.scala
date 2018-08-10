package peterlavalle.smolir

class ReferenceTest extends TTestCase {
	override def smol =
		"""
						|module ref:err
						|{
						| def bar(foo:&sint32)
						|}
				""".stripMargin

	override def treeCode =
		SmolIr.Module(
			"ref", "err",
			// don't do this "in general"
			expandedCalls
		)

	override def expandedCalls =
		List(
			SmolIr.Prototype(
				"bar",
				List(
					SmolIr.TCall.Arg("foo", SmolIr.KIntS32.Ref)
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
								|struct ref
								|{
								|// calls
								|	void* _bar;
								|	static void bar(int32_t& foo);
								|// initialiser
								|	static void def(void*, void*(*)(void*, const char*), const char*);
								|};
								|#if defined(smol_cpp)
						""".stripTrim

	override def expandedEnums =
		Nil

	override def labels =
		List(
			"bar"
		)

	override def loader =
		'\n' +
			"""
								|#include "ref.hpp"
								|
								|void smol_code(size_t count, const char* prefix, void** ptr, void* userdata, void*(*callback)(void*, const char*), const char* allnames);
								|
								|ref _ref;
								|
								|void ref::def(void* userdata, void*(*callback)(void*, const char*), const char* allnames)
								|{
								|	smol_code(
								|		1,
								|		"err",
								|		reinterpret_cast<void**>(&(_ref)),
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
								|void ref::bar(int32_t& foo)
								|{
								|	(reinterpret_cast<void(SMOL_CALL)(int32_t&)>(_ref._bar))(foo);
								|}
								|
						""".stripTrim
}

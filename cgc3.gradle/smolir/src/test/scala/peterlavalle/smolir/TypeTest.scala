package peterlavalle.smolir


/**
	* basic test of type aliasing
	*/
class TypeTest extends TTestCase {
	val another =
		SmolIr.TypeDef("another", SmolIr.KindReal64, "{{3.14f}}",
			List(
			))
	val alias =
		SmolIr.TypeDef("alias", SmolIr.KindIntU32, null, List(
		))

	override def expandedCalls =
		List(
			SmolIr.Prototype("Gar", List(
				SmolIr.TCall.Arg("arg0", alias),
				SmolIr.TCall.Arg("arg1", SmolIr.KIntU8)
			), another)
		)

	override def smol =
		"""
						|module foo: bar
						|{
						| type alias: uint32
						| type another: real64 = {{3.14f}}
						|
						| def Gar(:alias, :uint8): another
						|}
				""".stripMargin

	override def expandedEnums: List[SmolIr.EnumKind] =
		Nil

	override def treeCode =
		SmolIr.Module(
			"foo", "bar",
			List(
				alias,
				another,
				SmolIr.Prototype("Gar", List(
					SmolIr.TCall.Arg("arg0", alias),
					SmolIr.TCall.Arg("arg1", SmolIr.KIntU8)
				), another)
			)
		)

	override def header =
		'\n' +
			"""
								|#pragma once
								|
								|#include <stdint.h>
								|
								|struct foo
								|{
								|// class types
								|	struct alias final
								|	{
								|		uint32_t _this;
								|		inline alias(uint32_t _) : _this(_) {}
								|		inline operator uint32_t(void) const { return _this; }
								|	};
								|	struct another final
								|	{
								|		double _this;
								|		inline another(double _ = (3.14f)) : _this(_) {}
								|		inline operator double(void) const { return _this; }
								|	};
								|// calls
								|	void* _Gar;
								|	static foo::another Gar(foo::alias arg0, uint8_t arg1);
								|// initialiser
								|	static void def(void*, void*(*)(void*, const char*), const char*);
								|};
								|#if defined(smol_cpp)
						""".stripTrim

	override def labels =
		List(
			"Gar"
		)

	override def loader =
		'\n' +
			"""
								|#include "foo.hpp"
								|
								|void smol_code(size_t count, const char* prefix, void** ptr, void* userdata, void*(*callback)(void*, const char*), const char* allnames);
								|
								|foo _foo;
								|
								|void foo::def(void* userdata, void*(*callback)(void*, const char*), const char* allnames)
								|{
								|	smol_code(
								|		1,
								|		"bar",
								|		reinterpret_cast<void**>(&(_foo)),
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
								|foo::another foo::Gar(foo::alias arg0, uint8_t arg1)
								|{
								|	return foo::another((reinterpret_cast<double(SMOL_CALL)(foo::alias, uint8_t)>(_foo._Gar))(arg0, arg1));
								|}
								|
						""".stripTrim
}

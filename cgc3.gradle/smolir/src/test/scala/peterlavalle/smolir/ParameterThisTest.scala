package peterlavalle.smolir

class ParameterThisTest extends TTestCase {
	val usages =
		SmolIr.EnumKind("usages",
			SmolIr.Flex.Hard,
			SmolIr.KindIntS64,
			List(
				SmolIr.Enumerant("STREAM_DRAW", "0x88E0"),
				SmolIr.Enumerant("DYNAMIC_COPY", "0x88EA")
			)
		)
	val target =
		SmolIr.EnumKind(
			"target",
			SmolIr.Flex.Hard,
			SmolIr.KIntU16,
			List(
				SmolIr.Enumerant("ELEMENT", "0x8893")
			)
		)
	val load =
		SmolIr.TypeDef.Method(
			"NamedBufferDataEXT",
			List(
				SmolIr.TCall.ThisArg,
				SmolIr.TCall.Arg(
					"data_size",
					SmolIr.KindIntU32
				),
				SmolIr.TCall.Arg(
					"data_data",
					SmolIr.KindPointer(SmolIr.KVoid)
				),
				SmolIr.TCall.Arg(
					"arg2",
					usages
				)
			),
			SmolIr.KVoid
		)
	val bind =
		SmolIr.TypeDef.Method(
			"BindBuffer",
			List(
				SmolIr.TCall.Arg("arg0", target),
				SmolIr.TCall.ThisArg,
				SmolIr.TCall.Arg("arg2", usages)
			),
			SmolIr.KVoid
		)
	val buffer: SmolIr.TypeDef = SmolIr.TypeDef(
		"buffer", SmolIr.KindIntU32, "{{0}}",
		List(
			load,
			bind
		)
	)

	override def smol =
		"""
						|module test: ay
						|{
						|
						|	enum usages: sint64
						|	{
						|		STREAM_DRAW 0x88E0,
						|		DYNAMIC_COPY 0x88EA,
						|	}
						|
						|	enum target: uint16
						|	{
						|		// ARRAY 0x8892,
						|		ELEMENT 0x8893,
						|	}
						|
						|	type buffer: uint32 = {{0}}
						|	{
						|		def NamedBufferDataEXT(data_size:uint32, data_data:*void, :usages)
						|		def BindBuffer(:target, this, :usages)
						|	}
						|}
				""".stripTrim

	override def treeCode = {
		SmolIr.Module(
			"test", "ay",
			List(
				usages,
				target,
				buffer
			)
		)
	}

	override def expandedCalls =
		List(
			SmolIr.Member(buffer, load),
			SmolIr.Member(buffer, bind)
		)

	override def header =
		'\n' +
			"""
								|
								|#pragma once
								|
								|#include <stdint.h>
								|
								|struct test
								|{
								|// enumeration types
								|	enum class usages: int64_t
								|	{
								|		STREAM_DRAW = 0x88E0,
								|		DYNAMIC_COPY = 0x88EA,
								|	};
								|	enum class target: uint16_t
								|	{
								|		ELEMENT = 0x8893,
								|	};
								|// class types
								|	struct buffer final
								|	{
								|		uint32_t _this;
								|		inline buffer(uint32_t _ = (0)) : _this(_) {}
								|		inline operator uint32_t(void) const { return _this; }
								|		void NamedBufferDataEXT(uint32_t data_size, void* data_data, test::usages arg2);
								|		void BindBuffer(test::target arg0, test::usages arg2);
								|	};
								|// calls
								|	// buffer
								|		void* _NamedBufferDataEXT;
								|		void* _BindBuffer;
								|// initialiser
								|	static void def(void*, void*(*)(void*, const char*), const char*);
								|};
								|#if defined(smol_cpp)
						""".stripTrim

	override def expandedEnums =
		List(
			usages,
			target
		)

	override def labels =
		List(
			"NamedBufferDataEXT",
			"BindBuffer"
		)

	override def loader =
		'\n' +
			"""
								|
								|#include "test.hpp"
								|
								|void smol_code(size_t count, const char* prefix, void** ptr, void* userdata, void*(*callback)(void*, const char*), const char* allnames);
								|
								|test _test;
								|
								|void test::def(void* userdata, void*(*callback)(void*, const char*), const char* allnames)
								|{
								|	smol_code(
								|		2,
								|		"ay",
								|		reinterpret_cast<void**>(&(_test)),
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
								|
								|
								|//
								|// buffer
								|void test::buffer::BindBuffer(test::target arg0, test::usages arg2)
								|{
								|	assert(
								|		(test::target::ELEMENT == arg0)
								|	);
								|	assert(
								|		(test::usages::STREAM_DRAW == arg2) ||
								|		(test::usages::DYNAMIC_COPY == arg2)
								|	);
								|	(reinterpret_cast<void(SMOL_CALL)(test::target, uint32_t, test::usages)>(_test._BindBuffer))(arg0, _this, arg2);
								|}
								|void test::buffer::NamedBufferDataEXT(uint32_t data_size, void* data_data, test::usages arg2)
								|{
								|	assert(
								|		(test::usages::STREAM_DRAW == arg2) ||
								|		(test::usages::DYNAMIC_COPY == arg2)
								|	);
								|	(reinterpret_cast<void(SMOL_CALL)(uint32_t, uint32_t, void*, test::usages)>(_test._NamedBufferDataEXT))(_this, data_size, data_data, arg2);
								|}
								|
						""".stripTrim
}


package peterlavalle.smolir

import org.junit.Assert._

/**
	* tests in-place enum
	*/
class EnumTest extends TTestCase {
	val doeFlags =
		SmolIr.EnumKind(
			"doe_f",
			SmolIr.Flex.Soft,
			SmolIr.KIntU8,
			List(
				SmolIr.Enumerant("T", "0x0"),
				SmolIr.Enumerant("F", "0x03"),
				SmolIr.Enumerant("O", "0xA")
			)
		)

	val bufferBits =
		SmolIr.EnumKind(
			"buffer_bits",
			SmolIr.Flex.Soft,
			SmolIr.KindIntU32,
			List(
				SmolIr.Enumerant("COLOR", "0x00000100"),
				SmolIr.Enumerant("STENCIL", "0x00000400"),
				SmolIr.Enumerant("DEPTH", "0x00004000")
			)
		)
	val foo =
		SmolIr.EnumKind(
			"Foo",
			SmolIr.Flex.Hard,
			SmolIr.KindIntS64,
			List(
				SmolIr.Enumerant("BAR", "0x0"),
				SmolIr.Enumerant("GAYME", "0x213")
			)
		)
	var bar =
		SmolIr.EnumKind(
			"Bar",
			SmolIr.Flex.Hard,
			SmolIr.KIntU16,
			List(
				SmolIr.Enumerant("BAR", "0x0"),
				SmolIr.Enumerant("GAYME", "0x213"),
				SmolIr.Enumerant("NOPE", "0x1"),
				SmolIr.Enumerant("GONE", "0x2")
			)
		)

	def testEmitBufferBitsAssert(): Unit =
		assertEmitEnumAssert(
			"""
								|assert(0 == (
								|		((
								|			gl243::buffer_bits::COLOR |
								|			gl243::buffer_bits::STENCIL |
								|			gl243::buffer_bits::DEPTH
								|		) | testEmitBufferBitsAssert) ^ (
								|			gl243::buffer_bits::COLOR |
								|			gl243::buffer_bits::STENCIL |
								|			gl243::buffer_bits::DEPTH
								|		))
								|	);
						""".stripMargin,
			"gl243",
			bufferBits
		)

	def testEmitFooAssert(): Unit =
		assertEmitEnumAssert(
			"""
								|	assert(
								|		(glgdf43::Foo::BAR == testEmitFooAssert) ||
								|		(glgdf43::Foo::GAYME == testEmitFooAssert)
								|	);
						""".stripMargin,
			"glgdf43",
			foo
		)

	def assertEmitEnumAssert(text: String, module: String, enum: SmolIr.EnumKind): Unit =
		assertEquals(
			text.trim,
			Cpp.emitAssertEnumOrFlag(module, enum, getName).trim
		)

	def testEmitBarAssert(): Unit =
		assertEmitEnumAssert(
			"""
								|	assert(
								|		(gl43::Bar::BAR == testEmitBarAssert) ||
								|		(gl43::Bar::GAYME == testEmitBarAssert) ||
								|		(gl43::Bar::NOPE == testEmitBarAssert) ||
								|		(gl43::Bar::GONE == testEmitBarAssert)
								|	);
						""".stripMargin,
			"gl43",
			bar
		)

	override def expandedCalls =
		List(
			SmolIr.Prototype(
				"CreateShader", List(
					SmolIr.TCall.Arg(
						"stage",
						SmolIr.EnumKind(
							"CreateShader_stage_e",
							SmolIr.Flex.Hard,
							SmolIr.KindIntU32,
							List(
								SmolIr.Enumerant("FRAGMENT_SHADER", "0x8B30"),
								SmolIr.Enumerant("VERTEX_SHADER", "0x8B31")
							)
						)
					)
				),
				SmolIr.KIntS32
			),

			SmolIr.Prototype(
				"doe",
				List(
					SmolIr.TCall.Arg(
						"arg0",
						SmolIr.EnumKind(
							"Foo",
							SmolIr.Flex.Hard,
							SmolIr.KindIntS64,
							List(
								SmolIr.Enumerant("BAR", "0x0"),
								SmolIr.Enumerant("GAYME", "0x213")
							)
						)
					)
				),
				doeFlags
			),

			SmolIr.Prototype(
				"GetError", List(),
				SmolIr.EnumKind(
					"GetError_e",
					SmolIr.Flex.Hard,
					SmolIr.KIntU16,
					List(
						SmolIr.Enumerant("GL_NO_ERROR", "0x0"),
						SmolIr.Enumerant("GL_INVALID_ENUM", "0x0500"),
						SmolIr.Enumerant("GL_INVALID_VALUE", "0x0501"),
						SmolIr.Enumerant("GL_INVALID_OPERATION", "0x0502"),
						SmolIr.Enumerant("GL_INVALID_FRAMEBUFFER_OPERATION", "0x0506"),
						SmolIr.Enumerant("GL_OUT_OF_MEMORY", "0x0505")
					)
				)
			)
		)

	override def smol =
		"""
						|module gl43: gl
						|{
						|		def CreateShader(stage:enum{FRAGMENT_SHADER = 0x8B30, VERTEX_SHADER = 0x8B31}:uint32): sint32
						|
						|			enum Foo: sint64
						|		{BAR = 0x0,
						|		GAYME = 0x213
						|		}
						|
						|enum Bar:uint16 with Foo
						|{
						|        NOPE 0x1,
						| GONE 0x2,
						|}
						|
						|    flag buffer_bits: uint32
						|    {
						|        COLOR = 0x00000100
						|        STENCIL = 0x00000400
						|        DEPTH = 0x00004000
						|    }
						|
						|
						|
						|def doe(:Foo):flag
						|  {
						|  T = 0x0
						|  F=0x03
						|  O=0xA
						|  }:uint8
						|
						|		def GetError(): enum {
						|				GL_NO_ERROR = 0x0,
						|				GL_INVALID_ENUM = 0x0500,
						|				GL_INVALID_VALUE=0x0501,
						|				GL_INVALID_OPERATION=0x0502,
						|				GL_INVALID_FRAMEBUFFER_OPERATION=0x0506,
						|				GL_OUT_OF_MEMORY=0x0505,
						|		}: uint16
						|}
				""".stripMargin

	override def treeCode =
		SmolIr.Module(
			"gl43", "gl",
			List(
				SmolIr.Prototype(
					"CreateShader", List(
						SmolIr.TCall.Arg(
							"stage",
							SmolIr.EnumKind(
								"CreateShader_stage_e",
								SmolIr.Flex.Hard,
								SmolIr.KindIntU32,
								List(
									SmolIr.Enumerant("FRAGMENT_SHADER", "0x8B30"),
									SmolIr.Enumerant("VERTEX_SHADER", "0x8B31")
								)
							)
						)
					),
					SmolIr.KIntS32
				),

				foo,

				bar,

				bufferBits,

				SmolIr.Prototype(
					"doe",
					List(
						SmolIr.TCall.Arg(
							"arg0",
							foo
						)
					),
					doeFlags
				),

				SmolIr.Prototype(
					"GetError", List(),
					SmolIr.EnumKind(
						"GetError_e",
						SmolIr.Flex.Hard,
						SmolIr.KIntU16,
						List(
							SmolIr.Enumerant("GL_NO_ERROR", "0x0"),
							SmolIr.Enumerant("GL_INVALID_ENUM", "0x0500"),
							SmolIr.Enumerant("GL_INVALID_VALUE", "0x0501"),
							SmolIr.Enumerant("GL_INVALID_OPERATION", "0x0502"),
							SmolIr.Enumerant("GL_INVALID_FRAMEBUFFER_OPERATION", "0x0506"),
							SmolIr.Enumerant("GL_OUT_OF_MEMORY", "0x0505")
						)
					)
				)
			)
		)

	override def header =
		'\n' +
			"""
								|#pragma once
								|
								|#include <stdint.h>
								|
								|struct gl43
								|{
								|// enumeration types
								|	enum class CreateShader_stage_e: uint32_t
								|	{
								|		FRAGMENT_SHADER = 0x8B30,
								|		VERTEX_SHADER = 0x8B31,
								|	};
								|	enum class Foo: int64_t
								|	{
								|		BAR = 0x0,
								|		GAYME = 0x213,
								|	};
								|	enum class Bar: uint16_t
								|	{
								|		BAR = 0x0,
								|		GAYME = 0x213,
								|		NOPE = 0x1,
								|		GONE = 0x2,
								|	};
								|	enum buffer_bits: uint32_t
								|	{
								|		COLOR = 0x00000100,
								|		STENCIL = 0x00000400,
								|		DEPTH = 0x00004000,
								|	};
								|	enum doe_f: uint8_t
								|	{
								|		T = 0x0,
								|		F = 0x03,
								|		O = 0xA,
								|	};
								|	enum class GetError_e: uint16_t
								|	{
								|		GL_NO_ERROR = 0x0,
								|		GL_INVALID_ENUM = 0x0500,
								|		GL_INVALID_VALUE = 0x0501,
								|		GL_INVALID_OPERATION = 0x0502,
								|		GL_INVALID_FRAMEBUFFER_OPERATION = 0x0506,
								|		GL_OUT_OF_MEMORY = 0x0505,
								|	};
								|// calls
								|	void* _CreateShader;
								|	static int32_t CreateShader(gl43::CreateShader_stage_e stage);
								|	void* _doe;
								|	static gl43::doe_f doe(gl43::Foo arg0);
								|	void* _GetError;
								|	static gl43::GetError_e GetError(void);
								|// initialiser
								|	static void def(void*, void*(*)(void*, const char*), const char*);
								|};
								|#if defined(smol_cpp)
						""".stripMarginTail

	override def labels =
		List(
			"CreateShader",
			"doe",
			"GetError"
		)

	override def loader = '\n' +
		"""
						|#include "gl43.hpp"
						|
						|void smol_code(size_t count, const char* prefix, void** ptr, void* userdata, void*(*callback)(void*, const char*), const char* allnames);
						|
						|gl43 _gl43;
						|
						|void gl43::def(void* userdata, void*(*callback)(void*, const char*), const char* allnames)
						|{
						|	smol_code(
						|		3,
						|		"gl",
						|		reinterpret_cast<void**>(&(_gl43)),
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
						|int32_t gl43::CreateShader(gl43::CreateShader_stage_e stage)
						|{
						|	assert(
						|		(gl43::CreateShader_stage_e::FRAGMENT_SHADER == stage) ||
						|		(gl43::CreateShader_stage_e::VERTEX_SHADER == stage)
						|	);
						|	return (reinterpret_cast<int32_t(SMOL_CALL)(gl43::CreateShader_stage_e)>(_gl43._CreateShader))(stage);
						|}
						|gl43::GetError_e gl43::GetError(void)
						|{
						|	auto _out = (reinterpret_cast<gl43::GetError_e(SMOL_CALL)(void)>(_gl43._GetError))();
						|	assert(
						|		(gl43::GetError_e::GL_NO_ERROR == _out) ||
						|		(gl43::GetError_e::GL_INVALID_ENUM == _out) ||
						|		(gl43::GetError_e::GL_INVALID_VALUE == _out) ||
						|		(gl43::GetError_e::GL_INVALID_OPERATION == _out) ||
						|		(gl43::GetError_e::GL_INVALID_FRAMEBUFFER_OPERATION == _out) ||
						|		(gl43::GetError_e::GL_OUT_OF_MEMORY == _out)
						|	);
						|	return _out;
						|}
						|gl43::doe_f gl43::doe(gl43::Foo arg0)
						|{
						|	assert(
						|		(gl43::Foo::BAR == arg0) ||
						|		(gl43::Foo::GAYME == arg0)
						|	);
						|	auto _out = (reinterpret_cast<gl43::doe_f(SMOL_CALL)(gl43::Foo)>(_gl43._doe))(arg0);
						|	assert(0 == (
						|		((
						|			gl43::doe_f::T |
						|			gl43::doe_f::F |
						|			gl43::doe_f::O
						|		) | _out) ^ (
						|			gl43::doe_f::T |
						|			gl43::doe_f::F |
						|			gl43::doe_f::O
						|		))
						|	);
						|	return _out;
						|}
				""".stripTrim

	override def expandedEnums: List[SmolIr.EnumKind] =
		List(
			SmolIr.EnumKind(
				"CreateShader_stage_e",
				SmolIr.Flex.Hard,
				SmolIr.KindIntU32,
				List(
					SmolIr.Enumerant("FRAGMENT_SHADER", "0x8B30"),
					SmolIr.Enumerant("VERTEX_SHADER", "0x8B31")
				)
			),

			foo,

			bar,

			bufferBits,

			doeFlags,

			SmolIr.EnumKind(
				"GetError_e",
				SmolIr.Flex.Hard,
				SmolIr.KIntU16,
				List(
					SmolIr.Enumerant("GL_NO_ERROR", "0x0"),
					SmolIr.Enumerant("GL_INVALID_ENUM", "0x0500"),
					SmolIr.Enumerant("GL_INVALID_VALUE", "0x0501"),
					SmolIr.Enumerant("GL_INVALID_OPERATION", "0x0502"),
					SmolIr.Enumerant("GL_INVALID_FRAMEBUFFER_OPERATION", "0x0506"),
					SmolIr.Enumerant("GL_OUT_OF_MEMORY", "0x0505")
				)
			)
		)
}

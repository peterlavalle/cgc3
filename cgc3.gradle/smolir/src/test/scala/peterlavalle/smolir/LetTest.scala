package peterlavalle.smolir


/**
	* tests the object-like abstractions
	*/
class LetTest extends TTestCase {
	val shader: SmolIr.TypeDef =
		SmolIr.TypeDef(
			"shader", SmolIr.KIntU8,
			null,
			List(
			)
		)
	val program: SmolIr.TypeDef =
		SmolIr.TypeDef(
			"program", SmolIr.KindIntU32,
			null,
			List(
				SmolIr.TypeDef.Constructor("CreateProgram", List()),
				SmolIr.TypeDef.Constructor("ImportProgram", List(SmolIr.TCall.Arg("arg0", SmolIr.KIntU16))),
				SmolIr.TypeDef.Method("nope", "Bar", List(SmolIr.TCall.Arg("a", SmolIr.KIntU8), SmolIr.TCall.ThisArg, SmolIr.TCall.Value("0x19", SmolIr.KIntS32)), SmolIr.KVoid),
				SmolIr.TypeDef.Method("gone", "Bar", List(SmolIr.TCall.Arg("a", SmolIr.KIntU8), SmolIr.TCall.ThisArg, SmolIr.TCall.Value("0x10", SmolIr.KIntS32)), SmolIr.KVoid),
				SmolIr.TypeDef.Method("DeleteProgram", List(SmolIr.TCall.ThisArg), SmolIr.KVoid),
				SmolIr.TypeDef.Method("AttachShader", List(
					SmolIr.TCall.ThisArg,
					SmolIr.TCall.Arg("arg0", shader)
				), SmolIr.KVoid),
				SmolIr.TypeDef.Method("DetachShader", List(
					SmolIr.TCall.ThisArg,
					SmolIr.TCall.Arg("arg0", shader)
				), SmolIr.KIntU8),
				SmolIr.TypeDef.Method("LinkProgram", List(
					SmolIr.TCall.ThisArg,
					SmolIr.TCall.Arg(
						"deets",
						SmolIr.EnumKind(
							"program_LinkProgram_deets_e",
							SmolIr.Flex.Hard,
							SmolIr.KindIntU64,
							List(
								SmolIr.Enumerant("HARD", "0x0"),
								SmolIr.Enumerant("SOFT", "0x1")
							)
						)
					)
				), shader),

				SmolIr.TypeDef.Method("release", "DeleteProgram", List(SmolIr.TCall.ThisArg), SmolIr.KVoid)

			)
		)

	override def expandedCalls =
		treeCode.contents.flatMap {
			case program: SmolIr.TypeDef =>
				program.members.map {
					case constructor: SmolIr.TypeDef.Constructor => SmolIr.Member(program, constructor)
					case destructor: SmolIr.TypeDef.Destructor => SmolIr.Member(program, destructor)
					case method: SmolIr.TypeDef.Method => SmolIr.Member(program, method)
				}
		}

	override def treeCode = {
		SmolIr.Module(
			"gl43", "gl",
			List(
				shader,
				program
			)
		)
	}

	override def smol =
		"""
						|module gl43: gl
						|{
						|			type shader: uint8
						|
						|			type program: uint32
						|			{
						|				// test a constructor with no args
						|					new CreateProgram()
						|
						|				// test a constructor with an arg
						|					new ImportProgram(:uint16)
						|
						|         def nope = Bar(a: uint8, this, sint32 := 0x19)
						|         def gone = Bar(a: uint8, this, sint32 := 0x10)
						|
						|					def DeleteProgram()
						|
						|					def AttachShader(:shader)
						|					def DetachShader(:shader): uint8
						|
						|					def LinkProgram(deets:enum{HARD=0x0,SOFT=0x1}:uint64): shader
						|
						|					def release = DeleteProgram()
						|			}
						|}
				""".stripMargin

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
								|	enum class program_LinkProgram_deets_e: uint64_t
								|	{
								|		HARD = 0x0,
								|		SOFT = 0x1,
								|	};
								|// class types
								|	struct shader final
								|	{
								|		uint8_t _this;
								|		inline shader(uint8_t _) : _this(_) {}
								|		inline operator uint8_t(void) const { return _this; }
								|	};
								|	struct program final
								|	{
								|		uint32_t _this;
								|		inline program(uint32_t _) : _this(_) {}
								|		inline operator uint32_t(void) const { return _this; }
								|		static program CreateProgram(void);
								|		static program ImportProgram(uint16_t arg0);
								|		void nope(uint8_t a);
								|		void gone(uint8_t a);
								|		void DeleteProgram(void);
								|		void AttachShader(gl43::shader arg0);
								|		uint8_t DetachShader(gl43::shader arg0);
								|		gl43::shader LinkProgram(gl43::program_LinkProgram_deets_e deets);
								|		void release(void);
								|	};
								|// calls
								|	// program
								|		void* _CreateProgram;
								|		void* _ImportProgram;
								|		void* _Bar;
								|		void* _DeleteProgram;
								|		void* _AttachShader;
								|		void* _DetachShader;
								|		void* _LinkProgram;
								|// initialiser
								|	static void def(void*, void*(*)(void*, const char*), const char*);
								|};
								|#if defined(smol_cpp)
						""".stripTrim

	override def labels =
		List(
			"CreateProgram",
			"ImportProgram",
			"Bar",
			"DeleteProgram",
			"AttachShader",
			"DetachShader",
			"LinkProgram"
		)

	override def loader =
		'\n' +
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
								|		7,
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
								|
								|
								|//
								|// program
								|void gl43::program::AttachShader(gl43::shader arg0)
								|{
								|	(reinterpret_cast<void(SMOL_CALL)(uint32_t, gl43::shader)>(_gl43._AttachShader))(_this, arg0);
								|}
								|void gl43::program::nope(uint8_t a)
								|{
								|	(reinterpret_cast<void(SMOL_CALL)(uint8_t, uint32_t, int32_t)>(_gl43._Bar))(a, _this, 0x19);
								|}
								|void gl43::program::gone(uint8_t a)
								|{
								|	(reinterpret_cast<void(SMOL_CALL)(uint8_t, uint32_t, int32_t)>(_gl43._Bar))(a, _this, 0x10);
								|}
								|gl43::program::program(void)
								|{
								|	_this = (reinterpret_cast<uint32_t(SMOL_CALL)(void)>(_gl43._CreateProgram))();
								|}
								|void gl43::program::DeleteProgram(void)
								|{
								|	(reinterpret_cast<void(SMOL_CALL)(uint32_t)>(_gl43._DeleteProgram))(_this);
								|}
								|void gl43::program::release(void)
								|{
								|	(reinterpret_cast<void(SMOL_CALL)(uint32_t)>(_gl43._DeleteProgram))(_this);
								|}
								|uint8_t gl43::program::DetachShader(gl43::shader arg0)
								|{
								|	return (reinterpret_cast<uint8_t(SMOL_CALL)(uint32_t, gl43::shader)>(_gl43._DetachShader))(_this, arg0);
								|}
								|gl43::program::program(uint16_t arg0)
								|{
								|	_this = (reinterpret_cast<uint32_t(SMOL_CALL)(uint16_t)>(_gl43._ImportProgram))(arg0);
								|}
								|gl43::shader gl43::program::LinkProgram(gl43::program_LinkProgram_deets_e deets)
								|{
								|	assert(
								|		(gl43::program_LinkProgram_deets_e::HARD == deets) ||
								|		(gl43::program_LinkProgram_deets_e::SOFT == deets)
								|	);
								|	return gl43::shader((reinterpret_cast<uint8_t(SMOL_CALL)(uint32_t, gl43::program_LinkProgram_deets_e)>(_gl43._LinkProgram))(_this, deets));
								|}
						""".stripTrim

	override def expandedEnums: List[SmolIr.EnumKind] =
		List(
			SmolIr.EnumKind(
				"program_LinkProgram_deets_e",
				SmolIr.Flex.Hard,
				SmolIr.KindIntU64,
				List(
					SmolIr.Enumerant("HARD", "0x0"),
					SmolIr.Enumerant("SOFT", "0x1")
				)
			)
		)
}

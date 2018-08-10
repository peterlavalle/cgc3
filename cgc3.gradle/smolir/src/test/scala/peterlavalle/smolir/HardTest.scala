package peterlavalle.smolir

class HardTest extends TTestCase {

	lazy val thingie =
		SmolIr.TypeDef(
			"thingie", SmolIr.KindIntU64, "{{0xFFFFFFFFFFFFFFFF}}",
			List(
				SmolIr.TypeDef.Destructor(
					"DoNoLose"
				),
				SmolIr.TypeDef.Method(
					"Foo", List(
						SmolIr.TCall.ThisArg,
						SmolIr.TCall.Arg("arg0", SmolIr.KindReal32)
					),
					SmolIr.KVoid
				),
				SmolIr.TypeDef.Method(
					"Bar",
					List(
						SmolIr.TCall.ThisArg
					),
					SmolIr.KIntU16
				)
			)
		)
	lazy val boofer =
		SmolIr.TypeDef(
			"boofer", SmolIr.KIntU8, "{{0x314}}",
			List(
				SmolIr.TypeDef.Constructor(
					"ConstructNone", Nil
				),
				SmolIr.TypeDef.Constructor(
					"ConstructPointer", List(
						SmolIr.TCall.Value("0x1983", SmolIr.KindIntU32),
						SmolIr.TCall.ThisArg
					)
				),
				SmolIr.TypeDef.Destructor(
					"Release", List(
						SmolIr.TCall.Value("0x83", SmolIr.KindIntU32),
						SmolIr.TCall.ThisArg
					)
				)
			)
		)

	lazy val Make = SmolIr.Prototype("Make", List(), thingie)
	lazy val Pass = SmolIr.Prototype("Pass", List(SmolIr.TCall.Arg("arg0", thingie)), SmolIr.KVoid)

	override def smol =
		"""
						|module hardtest: ay
						|{
						|	type thingie: uint64 = {{0xFFFFFFFFFFFFFFFF}}
						|	{
						|		del DoNoLose
						|		def Foo(:real32)
						|		def Bar(): uint16
						|	}
						|	def Make(): thingie
						|	def Pass(:thingie): void ;hards as args
						|
						|	type boofer: uint8 = {{0x314}}
						|	{
						|		new ConstructNone()
						|		new ConstructPointer(uint32 := 0x1983, this)
						|		del Release(uint32 := 0x83, this)
						|	}
						|}
				""".stripTrim

	override def treeCode: SmolIr.Module =
		SmolIr.Module(
			"hardtest", "ay",
			List(
				thingie,
				Make,
				Pass,
				boofer
			)
		)

	override def expandedCalls =
		Pass :: Make :: thingie.members.map((member: TypeDef.TMember) => SmolIr.Member(thingie, member)) ++ boofer.members.map {
			(member: TypeDef.TMember) => SmolIr.Member(boofer, member)
		}

	override def header =
		'\n' +
			"""
								|#pragma once
								|
								|#include <stdint.h>
								|
								|struct hardtest
								|{
								|// class types
								|	class thingie final // hard
								|	{
								|		uint64_t _this;
								|		thingie(uint64_t);
								|	public:
								|		thingie(void);
								|		thingie(const thingie&) = delete;
								|		thingie& operator=(const thingie&) = delete;
								|		thingie(thingie&&);
								|		void operator=(thingie&&);
								|
								|		~thingie(void); // DoNoLose
								|		void Foo(float arg0);
								|		uint16_t Bar(void);
								|	};
								|	class boofer final // hard
								|	{
								|		uint8_t _this;
								|		boofer(uint8_t);
								|	public:
								|		boofer(void);
								|		boofer(const boofer&) = delete;
								|		boofer& operator=(const boofer&) = delete;
								|		boofer(boofer&&);
								|		void operator=(boofer&&);
								|
								|		static boofer ConstructNone(void);
								|		static boofer ConstructPointer(void);
								|		~boofer(void); // Release
								|	};
								|// calls
								|	// thingie
								|		void* _DoNoLose;
								|		void* _Foo;
								|		void* _Bar;
								|	void* _Make;
								|	static hardtest::thingie Make(void);
								|	void* _Pass;
								|	static void Pass(const hardtest::thingie& arg0);
								|	// boofer
								|		void* _ConstructNone;
								|		void* _ConstructPointer;
								|		void* _Release;
								|// initialiser
								|	static void def(void*, void*(*)(void*, const char*), const char*);
								|};
								|#if defined(smol_cpp)
						""".stripTrim

	override def expandedEnums =
		Nil

	override def labels =
		List(
			"DoNoLose",
			"Foo",
			"Bar",
			"Make",
			"Pass",
			"ConstructNone",
			"ConstructPointer",
			"Release"
		)

	override def loader =
		'\n' +
			"""
								|
								|#include "hardtest.hpp"
								|
								|void smol_code(size_t count, const char* prefix, void** ptr, void* userdata, void*(*callback)(void*, const char*), const char* allnames);
								|
								|hardtest _hardtest;
								|
								|void hardtest::def(void* userdata, void*(*callback)(void*, const char*), const char* allnames)
								|{
								|	smol_code(
								|		8,
								|		"ay",
								|		reinterpret_cast<void**>(&(_hardtest)),
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
								|hardtest::thingie hardtest::Make(void)
								|{
								|	return hardtest::thingie((reinterpret_cast<uint64_t(SMOL_CALL)(void)>(_hardtest._Make))());
								|}
								|void hardtest::Pass(const hardtest::thingie& arg0)
								|{
								|	assert((0xFFFFFFFFFFFFFFFF) != reinterpret_cast<const uint64_t&>(arg0));
								|	(reinterpret_cast<void(SMOL_CALL)(uint64_t)>(_hardtest._Pass))(reinterpret_cast<const uint64_t&>(arg0));
								|}
								|
								|
								|//
								|// boofer
								|hardtest::boofer::boofer(uint8_t _) :
								|	_this(_)
								|{}
								|hardtest::boofer::boofer(void) :
								|	_this(0x314)
								|{}
								|hardtest::boofer::boofer(hardtest::boofer&& them) :
								|	_this(them._this)
								|{
								|	them._this = 0x314;
								|}
								|void hardtest::boofer::operator=(hardtest::boofer&& them)
								|{
								|	this->~boofer();
								|	_this = them._this;
								|	them._this = 0x314;
								|}
								|hardtest::boofer hardtest::boofer::ConstructNone(void)
								|{
								|	hardtest::boofer _this;
								|	_this._this = (reinterpret_cast<uint8_t(SMOL_CALL)(void)>(_hardtest._ConstructNone))();
								|	return _this;
								|}
								|hardtest::boofer hardtest::boofer::ConstructPointer(void)
								|{
								|	hardtest::boofer _this;
								|	(reinterpret_cast<void(SMOL_CALL)(uint32_t, uint8_t*)>(_hardtest._ConstructPointer))(0x1983, &(_this._this));
								|	return _this;
								|}
								|hardtest::boofer::~boofer(void)
								|{
								|	if (_this == (0x314))
								|		return;
								|	(reinterpret_cast<void(SMOL_CALL)(uint32_t, uint8_t*)>(_hardtest._Release))(0x83, &(_this));
								|}
								|
								|
								|//
								|// thingie
								|hardtest::thingie::thingie(uint64_t _) :
								|	_this(_)
								|{}
								|hardtest::thingie::thingie(void) :
								|	_this(0xFFFFFFFFFFFFFFFF)
								|{}
								|hardtest::thingie::thingie(hardtest::thingie&& them) :
								|	_this(them._this)
								|{
								|	them._this = 0xFFFFFFFFFFFFFFFF;
								|}
								|void hardtest::thingie::operator=(hardtest::thingie&& them)
								|{
								|	this->~thingie();
								|	_this = them._this;
								|	them._this = 0xFFFFFFFFFFFFFFFF;
								|}
								|uint16_t hardtest::thingie::Bar(void)
								|{
								|	assert((0xFFFFFFFFFFFFFFFF) != _this);
								|	return (reinterpret_cast<uint16_t(SMOL_CALL)(uint64_t)>(_hardtest._Bar))(_this);
								|}
								|hardtest::thingie::~thingie(void)
								|{
								|	if (_this == (0xFFFFFFFFFFFFFFFF))
								|		return;
								|	(reinterpret_cast<void(SMOL_CALL)(uint64_t)>(_hardtest._DoNoLose))(_this);
								|}
								|void hardtest::thingie::Foo(float arg0)
								|{
								|	assert((0xFFFFFFFFFFFFFFFF) != _this);
								|	(reinterpret_cast<void(SMOL_CALL)(uint64_t, float)>(_hardtest._Foo))(_this, arg0);
								|}
								|
						""".stripTrim
}

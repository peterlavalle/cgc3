package peterlavalle.smolir

import peterlavalle.TS

class EmptyModule extends TTestCase {


	override def smol: String =
		"""
						|module empty: none {
						|}
				""".stripMargin

	override def expandedCalls =
		Nil

	override def treeCode: SmolIr.Module =
		SmolIr.Module(
			TS("empty"),
			TS("none"),
			Nil
		)

	override def header: String =
		'\n' +
			"""
								|#pragma once
								|
								|#include <stdint.h>
								|
								|struct empty
								|{
								|// initialiser
								|	static void def(void*, void*(*)(void*, const char*), const char*);
								|};
								|#if defined(smol_cpp)
						""".stripTrim

	override def loader: String =
		'\n' +
			"""
								|#include "empty.hpp"
								|
								|void smol_code(size_t count, const char* prefix, void** ptr, void* userdata, void*(*callback)(void*, const char*), const char* allnames);
								|
								|empty _empty;
								|
								|void empty::def(void* userdata, void*(*callback)(void*, const char*), const char* allnames)
								|{
								|	smol_code(
								|		0,
								|		"none",
								|		reinterpret_cast<void**>(&(_empty)),
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
						""".stripTrim

	override def labels: List[String] = Nil

	override def expandedEnums: List[SmolIr.EnumKind] =
		Nil

}

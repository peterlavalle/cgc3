package peterlavalle.smolir

import peterlavalle.TS

class TwoBit extends TTestCase {
	override def expandedCalls =
		List(
			SmolIr.Prototype(
				TS("DeleteBuffers"),
				List(
					SmolIr.TCall.Arg("arg0", SmolIr.KindSize),
					SmolIr.TCall.Arg("arg1", SmolIr.KindPointer(SmolIr.KindChar))
				),
				SmolIr.KVoid
			)
		)

	override def smol: String =
		"""
						|module gl43: gl {
						|	def DeleteBuffers(:size_t, :*char)
						|}
				""".stripMargin

	override def treeCode: SmolIr.Module =
		SmolIr.Module(
			"gl43", "gl",
			List(
				SmolIr.Prototype(
					TS("DeleteBuffers"),
					List(
						SmolIr.TCall.Arg("arg0", SmolIr.KindSize),
						SmolIr.TCall.Arg("arg1", SmolIr.KindPointer(SmolIr.KindChar))
					),
					SmolIr.KVoid
				)
			)
		)

	override def header: String =
		'\n' +
			"""
								|#pragma once
								|
								|#include <stdint.h>
								|
								|struct gl43
								|{
								|// calls
								|	void* _DeleteBuffers;
								|	static void DeleteBuffers(size_t arg0, char* arg1);
								|// initialiser
								|	static void def(void*, void*(*)(void*, const char*), const char*);
								|};
								|#if defined(smol_cpp)
						""".stripMarginTail

	override def loader: String =
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
								|		1,
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
								|void gl43::DeleteBuffers(size_t arg0, char* arg1)
								|{
								|	(reinterpret_cast<void(SMOL_CALL)(size_t, char*)>(_gl43._DeleteBuffers))(arg0, arg1);
								|}
								|
						""".stripTrim

	override def labels: List[String] =
		List(
			"DeleteBuffers"
		)

	override def expandedEnums: List[SmolIr.EnumKind] =
		Nil
}

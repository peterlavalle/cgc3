grammar SignalHeader;

definition :
	'#pragma' 'once'

	'#include' '<pal-signal-node.hpp>'

	'SIGNAL' '(' kindname ')'
	'{'
		member+
	'}' ';'
	EOF
	;

kindname : NAME (':' NAME)?;

member
	: 'INPUT' '(' NAME ',' kind ')' #input
	| 'OUTPUT' '(' NAME ',' kind ')' #output
	| 'EVENT' '(' NAME ')' #event
	;

kind
	: TYPE #atomic
	;

TYPE
	: 'u'?'int'('8'|'16'|'32'|'64')'_t'
	| 'float'|'double'
	;

NAME : ([a-z]|[A-Z]) WChar*;

fragment WChar : [a-z]|[A-Z]|[0-9]|'_';

// only allow single line comments
WS : ((('//') (~'\n')*)|([, \r\n\t])) -> skip ;

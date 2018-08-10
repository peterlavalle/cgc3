grammar SmolIr;

ThisToken: 'this';

module : 'module' Name (':' Name)? '{' content* '}';

EnumFlag : 'enum' | 'flag';

content
	: EnumFlag Name ':' AtomicWhole with* '{' enumerant* '}' #enumerant_content // TODO; this is a wee bit redundant with type aliases and the in-place enum
	| 'def' code prototype #prototype_content
	| whinge Name kin ('=' HereSource)? ('{' member+ '}')? #alias
	;

whinge : 'type' | 'hard' | 'soft' | 'firm' | 'auto';

with : 'with' Name;

member
	: 'def' code prototype #method
	| 'new' Name '(' (args (',' args)*)? ')' #constructor
	| 'del' Name ('(' (args (',' args)*)? ')')? #destructor
	;

code
	: Name #direct
	| Name '=' Name #rename
	;

HereSource: '{{' .*? '}}';

prototype : '(' (args (',' args)*)? ')' kin? ;

args
	: Name? kin #normal
	| ThisToken #selfie
	| AtomicWhole ':=' HexVal #hardcoded
	| Name '.' Name #enumval
	;

kin: ':' kind;

kind
	: '*' kind #pointer
	| ('const'|'#') kind #constant
	| '&' kind #reference
	| '[' AtomicWhole kind (',' kind)* ']' #vector
	| (AtomicReal|AtomicWhole|AtomixVoid|AtomicChar) #primitive
	| EnumFlag '{' enumerant+ '}' ':' AtomicWhole #inplace_enum
	| Name #named
	| prototype #callback
	;

enumerant
	: Name '=' HexVal ','?
	| Name HexVal ','
	;


//	: Name '=' value=(HexVal|IntVal) ','?;
//IntVal: [1-9][0-9]*;

HexVal: '0x' ([0-9]|[A-F])+;

AtomixVoid:		'void';
AtomicWhole:	('u'|'s')'int'('8'|'16'|'32'|'64')|'size_t';
AtomicReal:		'real'('32'|'64');
AtomicChar:		'char';

Name : ([a-z]|[A-Z]) WChar*;

fragment WChar : [a-z]|[A-Z]|[0-9]|'_';

WS : ((('//'|'#'|';') (~'\n')*)|([, \r\n\t])) -> skip ;

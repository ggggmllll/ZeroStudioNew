; --------------------------------------
; Preprocessor
; --------------------------------------
(preproc_directive) @keyword.directive
(preproc_def name: (identifier) @constant.macro)
(preproc_function_def name: (identifier) @function.macro)

(preproc_include
  path: (string_literal) @string.special)
(preproc_include
  path: (system_lib_string) @string.special.system)

; --------------------------------------
; Namespaces & Scopes
; --------------------------------------
(namespace_identifier) @namespace

; --------------------------------------
; Types
; --------------------------------------
(primitive_type) @type.builtin
(type_identifier) @type
(auto) @type.builtin

(struct_specifier name: (type_identifier) @type)
(class_specifier name: (type_identifier) @type)
(enum_specifier name: (type_identifier) @type)
(union_specifier name: (type_identifier) @type)

; --------------------------------------
; Functions
; --------------------------------------
(function_declarator
  declarator: (identifier) @function)

(function_declarator
  declarator: (qualified_identifier
    name: (identifier) @function))

(function_declarator
  declarator: (field_identifier) @function.method)

(call_expression
  function: (identifier) @function.call)

(call_expression
  function: (qualified_identifier
    name: (identifier) @function.call))

(call_expression
  function: (field_expression
    field: (field_identifier) @function.method.call))

(template_function name: (identifier) @function)

; --------------------------------------
; Variables & Fields
; --------------------------------------
(field_identifier) @variable.field
(this) @variable.builtin

(parameter_declaration
  declarator: (identifier) @variable.parameter)

(parameter_declaration
  declarator: (reference_declarator (identifier) @variable.parameter))

(parameter_declaration
  declarator: (pointer_declarator (identifier) @variable.parameter))

(identifier) @variable

; --------------------------------------
; Literals & Constants
; --------------------------------------
(number_literal) @number
(string_literal) @string
(raw_string_literal) @string
(char_literal) @string
(escape_sequence) @string.escape
(comment) @comment

(null) @constant.builtin
(true) @constant.builtin
(false) @constant.builtin

; --------------------------------------
; Keywords
; --------------------------------------
[
  "if" "else" "for" "while" "do" "break" "continue" "return" 
  "switch" "case" "default" "goto"
] @keyword.control[
  "const" "static" "extern" "volatile" "mutable" "register" 
  "inline" "virtual" "explicit" "friend" "constexpr" 
  "consteval" "constinit" "unsigned" "signed"
] @keyword.modifier[
  "public" "protected" "private"
] @keyword.visibility[
  "class" "struct" "union" "enum" "template" "typename" 
  "namespace" "using" "typedef" "concept" "requires" 
  "new" "delete" "try" "catch" "throw" "noexcept" 
  "sizeof" "decltype" "alignof" "alignas" "typeid" "operator"
  "co_await" "co_return" "co_yield" "import" "export" "module"
] @keyword[
  "static_cast" "dynamic_cast" "reinterpret_cast" "const_cast"
] @keyword.cast

; --------------------------------------
; Operators & Punctuation
; --------------------------------------
[
  "+" "-" "*" "/" "%" "++" "--"
  "==" "!=" "<" ">" "<=" ">="
  "&&" "||" "!"
  "&" "|" "^" "~" "<<" ">>"
  "=" "+=" "-=" "*=" "/=" "%=" "<<=" ">>=" "&=" "|=" "^="
  "?" ":"
] @operator

[
  "::" "->" "."
] @punctuation.special

[
  "(" ")" "[" "]" "{" "}"
] @punctuation.bracket

[
  "," ";"
] @punctuation.delimiter
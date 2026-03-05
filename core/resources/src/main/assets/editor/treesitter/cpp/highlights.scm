; --------------------------------------
; Preprocessor
; --------------------------------------
(preproc_directive) @keyword.directive
(preproc_def name: (identifier) @constant.macro)
(preproc_function_def name: (identifier) @function.macro)
(preproc_arg) @variable

(preproc_include
  path: (string_literal) @string.special)
(preproc_include
  path: (system_lib_string) @string.special)

; --------------------------------------
; Namespaces & Scopes
; --------------------------------------
(namespace_identifier) @namespace

; Capture "std" in "std::string"
(qualified_identifier
  scope: (namespace_identifier) @namespace)

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
(template_type name: (type_identifier) @type)

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

; --------------------------------------
; Variables, Fields & Parameters
; --------------------------------------
(field_identifier) @variable.field
(this) @variable.builtin

(parameter_declaration
  declarator: (identifier) @variable.parameter)

(parameter_declaration
  declarator: (reference_declarator (identifier) @variable.parameter))

(parameter_declaration
  declarator: (pointer_declarator (identifier) @variable.parameter))

; Catch-all for variables used in code (fixes black text for variables)
(identifier) @variable

; --------------------------------------
; Literals
; --------------------------------------
(number_literal) @number
(string_literal) @string
(char_literal) @string
(escape_sequence) @string.escape
(comment) @comment
(true) @constant.builtin
(false) @constant.builtin
(null) @constant.builtin
(nullptr) @constant.builtin

; --------------------------------------
; Keywords
; --------------------------------------
[
  "if" "else" "for" "while" "do" "break" "continue" "return" 
  "switch" "case" "default" "goto"
] @keyword.control

[
  "const" "static" "extern" "volatile" "mutable" "register" 
  "inline" "virtual" "explicit" "friend" "constexpr" 
  "consteval" "constinit" "unsigned" "signed"
] @keyword.modifier

[
  "public" "protected" "private"
] @keyword.visibility

[
  "class" "struct" "union" "enum" "template" "typename" 
  "namespace" "using" "typedef" "concept" "requires" 
  "new" "delete" "try" "catch" "throw" "noexcept" 
  "sizeof" "decltype" "alignof" "alignas" "typeid" "operator"
] @keyword

[
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
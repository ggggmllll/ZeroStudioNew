



  
; Preprocessing branch keywords
["#ifndef" "#endif" "#define"] @keyword.directive



; Match the identifier following #ifndef
(preproc_ifdef
  name: (identifier) @constant)




; Matching parameter type (e.g., GLenum)
(parameter_declaration
  type: (type_identifier) @type)

; Match function names (e.g., glGetError)
(function_declarator
  declarator: (identifier) @function)

; Macro-defined values (e.g., 0x0B21)
(_
  value: (number_literal) @number)

; Macro modifiers before matching function declaration
(declaration
  [
    (identifier) @keyword.modifier
    (type_qualifier) @keyword.modifier
  ])

; Macro definition name (e.g., GL_LINE_WIDTH)
(preproc_def
  name: (identifier) @constant)

; Force all all-uppercase identifiers containing underscores to be set to a constant color.
((identifier) @constant
 (#match? @constant "^[A-Z_][A-Z0-9_]*$"))

; --------------------------------------
; Preprocessor (Headers & Directives)
; --------------------------------------
(preproc_include
  path: (string_literal) @string.special)

(preproc_directive) @keyword.directive

; Matching function/macro name
(call_expression
  function: [
    (identifier) @function
    (field_identifier) @function
  ])

; Matches common identifiers (such as the variable name g) in the parameter list.
(argument_list (identifier) @variable)

; Match constants/macro definitions (usually uppercase) in the parameter list.
((identifier) @constant
 (#match? @constant "^[A-Z0-9_]+$"))

; Match object name (indexedVBO or g)
(field_expression
  argument: (identifier) @variable)

; Match member names (vboID or numVertices)
(field_expression
  field: (field_identifier) @property)

; Matches the address-of operator (&) or dereferences pointers (*).
(pointer_expression ["&" "*"] @operator)

; Matching arithmetic operations (such as g->numVertices * sizeof(...))
(binary_expression operator: "*" @operator)

(sizeof_expression "sizeof" @keyword)

; Function and macro calls
(call_expression function: (identifier) @function)

; Member access: object @variable, member @property
(field_expression 
  argument: (identifier) @variable
  field: (field_identifier) @property)
  
  
  ; ordinary macro definition name
(preproc_def name: (identifier) @constant)

; Macro function names with parameters
(preproc_function_def name: (identifier) @function.macro)

; defined Keywords
(preproc_defined "defined" @function.builtin)

; defined Macro name in parentheses
(preproc_defined (identifier) @constant)

; Built-in functions in preprocessing logic
(preproc_defined) @function.builtin

; Assignment expression
(assignment_expression
  left: _ @variable
  right: _ @variable)

; Conditional operator (ternary operator)
(conditional_expression
  consequence: _ @variable
  alternative: _ @variable)

; Binary operations (such as now - lastTick)
(binary_expression
  left: _ @variable
  right: _ @variable)

; Constructor/New Expression
(new_expression
  type: (type_identifier) @type
  arguments: (argument_list) @operator)

; Recursive matching member access
(field_expression
  argument: (field_expression) @variable
  field: (field_identifier) @property)

; Matching explicit destructor calls
(field_expression
  field: (destructor_name (identifier) @function.destructor))



;Matching pointer type declarations with *
(pointer_declarator
  "*" @operator
  declarator: (identifier) @variable
)

;Match the types in the parameter list
(parameter_declaration
  type: (type_identifier) @type
  declarator: (identifier) @variable)



; Matches any identifier that contains a double colon.
(_
  [
    (identifier) @type
    (type_identifier) @type
  ]
  "::"
  (identifier) @function)


; Scope resolution
(_
  path: (identifier) @type
  name: (identifier) @constant
)

; Nested scope fallback
(_
  path: (identifier) @type
  name: (type_identifier) @type
)

; Case Keywords
"case" @keyword
"default" @keyword

; The constant identifier following Case (such as BlankType)
(case_statement
  value: (identifier) @constant)

; Match the keyword "new"
"new" @keyword

; Matches the bracketed part (&blank_) of placement new.
(new_expression
  placement: (argument_list
    (pointer_expression
      "&" @operator
      (identifier) @variable)))

; Match constructor names (such as Blank).
(new_expression
  type: (type_identifier) @type)


; Member access (e.g., other.type)
(field_expression
  argument: (identifier) @variable
  field: (field_identifier) @property)


; Matches any expression containing a field.
(field_expression
  argument: [
    (identifier) @variable
    (field_expression) @variable
  ]
  field: (field_identifier) @property)

; Recursive matching of multi-level dot or arrow access
(field_expression
  argument: [
    (identifier) @variable
    (field_expression) @variable
    (call_expression) @function
  ]
  field: (field_identifier) @property)

; Match the `return` keyword and subsequent comparisons/operations.
"return" @keyword



; --------------------------------------
; Operators & Punctuation (Members, Scopes)
; --------------------------------------
"::" @punctuation.special
"->" @punctuation.special
"." @punctuation.special

; Match all preprocessing keywords
(preproc_if "#if" @keyword.directive)
(preproc_elif "#elif" @keyword.directive)
(preproc_else "#else" @keyword.directive)
(preproc_ifdef "#ifdef" @keyword.directive)
(preproc_def "#define" @keyword.directive)
(preproc_function_def "#define" @keyword.directive)


[
  "static"
  "extern"
  "const"
  "struct"
] @keyword.modifier


; Functions

(declaration
  declarator: (identifier) @variable)

(init_declarator
  declarator: (identifier) @variable)

(struct_specifier
  name: (type_identifier) @type)

(field_declaration
  declarator: [
    (field_identifier) @property
    (pointer_declarator declarator: (field_identifier) @property)
    (array_declarator declarator: (field_identifier) @property)
  ])





(preproc_include
  path: (string_literal) @string)

(call_expression
  function: (identifier) @function)

(call_expression
  function: (qualified_identifier
    name: (identifier) @function))

(template_function
  name: (identifier) @function)

(template_method
  name: (field_identifier) @function)

(template_function
  name: (identifier) @function)

(function_declarator
  declarator: (qualified_identifier
    name: (identifier) @function))

(function_declarator
  declarator: (qualified_identifier
    name: (identifier) @function))

(function_declarator
  declarator: (field_identifier) @function)

; Types

((namespace_identifier) @type
 (#match? @type "^[A-Z]"))

(auto) @type

; Constants

(this) @variable.builtin
(null "nullptr" @constant)


; Keywords

[
 "catch"
 "class"
 "co_await"
 "co_return"
 "co_yield"
 "constexpr"
 "constinit"
 "consteval"
 "delete"
 "explicit"
 "final"
 "friend"
 "mutable"
 "namespace"
 "noexcept"
 "new"
 "override"
 "private"
 "protected"
 "public"
 "template"
 "throw"
 "try"
 "typename"
 "using"
 "virtual"
 "concept"
 "requires"
] @keyword

; Strings

(raw_string_literal) @string

; Literal quantity
(number_literal) @number
(string_literal) @string
(comment) @comment
(type_identifier) @type
(primitive_type) @type
(preproc_include "#include" @keyword.directive)
(system_lib_string) @string.special

; Variables and fields
(identifier) @variable
(field_identifier) @property

; type
(type_identifier) @type
(primitive_type) @type
(auto) @type

; Structure definition
(struct_specifier "struct" @keyword name: (type_identifier) @type)

; Keywords and qualifiers
["static" "const" "extern" "struct"] @keyword

; symbol
["[" "]" "{" "}" "(" ")" ";" "*" "=" "->" "+" "-" "|" ","] @operator

"return" @keyword
":" @operator
"~" @operator


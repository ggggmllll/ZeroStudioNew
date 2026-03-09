; ===================================================
; Auto-generated from parser.c source code analysis
; Validated against ts_symbol_names to ensure NO CRASHES
; @author android_zero
; ===================================================

; --- Keywords ---
[
  "NULL"
  "alignas"
  "alignof"
  "and"
  "asm"
  "bitand"
  "bitor"
  "break"
  "case"
  "catch"
  "class"
  "compl"
  "concept"
  "const"
  "consteval"
  "constexpr"
  "constinit"
  "continue"
  "decltype"
  "default"
  "defined"
  "delete"
  "do"
  "else"
  "enum"
  "explicit"
  "extern"
  "final"
  "for"
  "friend"
  "goto"
  "if"
  "inline"
  "long"
  "mutable"
  "namespace"
  "new"
  "noexcept"
  "noreturn"
  "not"
  "nullptr"
  "offsetof"
  "operator"
  "or"
  "override"
  "private"
  "protected"
  "public"
  "register"
  "requires"
  "restrict"
  "return"
  "short"
  "signed"
  "sizeof"
  "static"
  "struct"
  "switch"
  "template"
  "throw"
  "try"
  "typedef"
  "typename"
  "union"
  "unsigned"
  "using"
  "virtual"
  "volatile"
  "while"
  "xor"
] @keyword

; --- Operators & Punctuation ---
[
  "
"
  "!"
  "!="
  "\""
  "\"\""
  "%"
  "%="
  "&"
  "&&"
  "&="
  "'"
  "()"
  "*"
  "*="
  "+"
  "++"
  "+="
  ","
  "-"
  "--"
  "-="
  "->"
  "->*"
  "."
  ".*"
  "..."
  "/"
  "/="
  ":"
  "::"
  ";"
  "<"
  "<<"
  "<<="
  "<="
  "<=>"
  "="
  "=="
  ">"
  ">="
  ">>"
  ">>="
  "L\""
  "L'"
  "LR\""
  "R\""
  "U\""
  "U'"
  "UR\""
  "[["
  "[]"
  "\\?"
  "]]"
  "^"
  "^="
  "and_eq"
  "co_await"
  "co_return"
  "co_yield"
  "field_identifier"
  "namespace_identifier"
  "not_eq"
  "or_eq"
  "simple_requirement"
  "statement_identifier"
  "static_assert"
  "thread_local"
  "type_identifier"
  "u\""
  "u'"
  "u8\""
  "u8'"
  "u8R\""
  "uR\""
  "xor_eq"
  "|"
  "|="
  "||"
  "~"
] @operator

; --- Brackets ---
[
  "("
  ")"
  "["
  "]"
  "{"
  "}"
] @punctuation.bracket

; --- Syntax Nodes (Validated) ---
(true) @constant.builtin
(null) @constant.builtin
(false) @constant.builtin
(abstract_function_declarator) @function
(argument_list) @variable.parameter
(attribute) @attribute
(attribute_declaration) @attribute
(attribute_specifier) @attribute
(attributed_declarator) @attribute
(attributed_statement) @attribute
(base_class_clause) @type
(bitfield_clause) @property
(char_literal) @string
(class_specifier) @type
(comment) @comment
(concatenated_string) @string
(decltype) @type
(default_method_clause) @function
(delete_method_clause) @function
(dependent_name) @variable
(dependent_type) @type
(destructor_name) @variable
(enum_specifier) @type
(enumerator) @type
(enumerator_list) @type
(explicit_function_specifier) @function
(field_declaration) @property
(field_declaration_list) @property
(field_designator) @property
(field_expression) @property
(field_initializer) @property
(field_initializer_list) @property
(function_declarator) @function
(function_definition) @function
(identifier) @variable
(labeled_statement) @label
(namespace_alias_definition) @variable
(namespace_definition) @variable
(nested_namespace_specifier) @variable
(number_literal) @number
(operator_name) @variable
(optional_parameter_declaration) @variable.parameter
(optional_type_parameter_declaration) @type
(parameter_declaration) @variable.parameter
(parameter_list) @variable.parameter
(parameter_pack_expansion) @variable.parameter
(placeholder_type_specifier) @type
(pointer_type_declarator) @type
(preproc_arg) @keyword.directive
(preproc_call) @keyword.directive
(preproc_def) @keyword.directive
(preproc_defined) @keyword.directive
(preproc_directive) @keyword.directive
(preproc_elif) @keyword.directive
(preproc_elifdef) @keyword.directive
(preproc_else) @keyword.directive
(preproc_function_def) @function
(preproc_if) @keyword.directive
(preproc_ifdef) @keyword.directive
(preproc_include) @keyword.directive
(preproc_params) @keyword.directive
(primitive_type) @type
(qualified_identifier) @variable
(raw_string_content) @string
(raw_string_delimiter) @string
(raw_string_literal) @string
(sized_type_specifier) @type
(storage_class_specifier) @type
(string_literal) @string
(struct_specifier) @type
(structured_binding_declarator) @type
(subscript_argument_list) @variable.parameter
(system_lib_string) @string
(template_argument_list) @variable.parameter
(template_function) @function
(template_method) @function
(template_parameter_list) @variable.parameter
(template_template_parameter_declaration) @variable.parameter
(template_type) @type
(trailing_return_type) @type
(type_definition) @type
(type_descriptor) @type
(type_parameter_declaration) @type
(type_qualifier) @type
(type_requirement) @type
(variadic_parameter_declaration) @variable.parameter
(variadic_type_parameter_declaration) @type



(scoped_type_identifier) @type
(generic_type) @type
(array_type) @type
(boolean_type) @type.builtin
(void_type) @type.builtin
(integral_type) @type.builtin
(floating_point_type) @type.builtin

(formal_parameter) @variable.parameter
(variable_declarator) @variable
(parcelable_declaration) @type

(identifier) @variable

(string_literal) @string
(line_comment) @comment
(block_comment) @comment


[
  (decimal_integer_literal)
  (hex_integer_literal)
  (octal_integer_literal)
  (binary_integer_literal)
  (decimal_floating_point_literal)
  (hex_floating_point_literal)
] @number


(true) @constant.builtin
(false) @constant.builtin
(null_literal) @constant.builtin

[
  "class" "interface" "parcelable" "import" "package" 
  "oneway" "in" "out" "inout" "static" "extends" 
  "return" "yield" "byte" "short" "int" "long" 
  "char" "float" "double" "throws" "default" "enum"
] @keyword

[
  "=" "==" "!=" ">" "<" ">=" "<=" 
  "&&" "||" "+" "-" "*" "/" "%" 
  "&" "|" "^" "<<" ">>" ">>>"
] @operator

[
  "(" ")" "[" "]" "{" "}"
  "," ":" ";" "." "..." "@"
] @punctuation




; --------------------------------------
; Functions & Methods
; --------------------------------------


(call_expression
  function: (field_expression
    field: (field_identifier) @function))

(function_declarator
  declarator: (identifier) @function)

(function_declarator
  declarator: (qualified_identifier
    name: (identifier) @function))

(function_declarator
  declarator: (field_identifier) @function)

; --------------------------------------
; Variables & Fields
; --------------------------------------
(field_identifier) @field
(this) @variable.builtin

; --------------------------------------
; Types & Classes
; --------------------------------------
(type_identifier) @type
(primitive_type) @type

(struct_specifier name: (type_identifier) @type)
(class_specifier name: (type_identifier) @type)

; --------------------------------------
; Keywords
; --------------------------------------
[
 "catch" "class" "constexpr" "delete" "explicit" "final" "friend" 
 "mutable" "namespace" "noexcept" "new" "override" "private" 
 "protected" "public" "template" "throw" "try" "typename" 
 "using" "virtual" "concept" "requires" "default" "const" "static"
] @keyword

; --------------------------------------
; Literals & Comments
; --------------------------------------
(string_literal) @string
(number_literal) @number
(comment) @comment

injections.scm：(raw_string_literal
  delimiter: (raw_string_delimiter) @injection.language
  (raw_string_content) @injection.content)


tags.scm：
(struct_specifier name: (type_identifier) @name body:(_)) @definition.class

(declaration type: (union_specifier name: (type_identifier) @name)) @definition.class

(function_declarator declarator: (identifier) @name) @definition.function

(function_declarator declarator: (field_identifier) @name) @definition.function

(function_declarator declarator: (qualified_identifier scope: (namespace_identifier) @local.scope name: (identifier) @name)) @definition.method

(type_definition declarator: (type_identifier) @name) @definition.type

(enum_specifier name: (type_identifier) @name) @definition.type

(class_specifier name: (type_identifier) @name) @definition.class




根据scm@{key name} 央射key来制作的cpplang.json的高亮配置文件：{
  "types": ["cpp", "C", "h", "H", "hpp", "cp", "cc", "hh", "cxx", "c++", "hxx", "h++", "cppm", "mpp", "mm"],
  "styles": {
    "keyword": { "fg": "@keyword", "bold": true },
    "keyword.directive": { "fg": "@kt.preproc", "bold": true },
    "function": { "fg": "@func.call" },
    "type": { "fg": "@type", "bold": true },
    "field": { "fg": "@field" },
    "variable": { "fg": "@variable" },
    "variable.builtin": { "fg": "@keyword", "bold": true },
    "string": { "fg": "@string" },
    "string.special": { "fg": "@func.decl", "italic": true },
    "number": { "fg": "@number" },
    "comment": { "fg": "@comment", "italic": true },
    "punctuation.special": { "fg": "@kt.punctuation.special" },
    "punctuation.bracket": { "fg": "@onSurface" },
    "operator": { "fg": "@operator" }
  }
}

问题：当前scm的定义不够全面，每一种/每一个符号类型，成员，变量，括号内的引用或代码没有高亮，class成员对象引用，定义，对象，实例等各类符号和其它都还没有完全有scm查询央射配置，导致很多还是黑字，比如#include <atomic>，又或者#define GL_FUNC(retVal, name, args) { name = (retVal (GL_APIENTRY *)args) SDL_GL_GetProcAddress("gl" #name); assert(name); }
又或者

#ifndef GL_APICALL
#define GL_APICALL  KHRONOS_APICALL
#endif

等各类都是黑字没有对应央射查询配置，所以继续补充更多更完善的scm定义去精细化/完整全面的查询扫描语法语义定义等在scm里。然后根据新增@{key name}来全部提取出来添加制作到cpplang.json里配置对应颜色高亮。

PS：具体没有高亮部分符号你可以查看上传图片
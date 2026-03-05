cmake_minimum_required(VERSION 3.22.1)

project("tree-sitter-markdown")

# 从 src/main/cpp (当前目录) 向上跳 4 级到达 tree-sitter-ndk 目录
get_filename_component(NDK_COMMON_DIR "${CMAKE_CURRENT_SOURCE_DIR}/../../../.." ABSOLUTE)

# 引入通用配置 (设置 TS_DIR 和 include 路径)
include("${NDK_COMMON_DIR}/common-config.cmake")

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# 让编译器知道去哪里找 #include "tree_sitter/parser.h" 或 #include "lexer.h" 等
include_directories(${CMAKE_CURRENT_SOURCE_DIR})
include_directories(${CMAKE_CURRENT_SOURCE_DIR}/tree_sitter)
include_directories(${CMAKE_CURRENT_SOURCE_DIR}/tree_sitter_markdown)

# 使用 GLOB 可以自动捕获该目录下所有的 C++ 源文件，避免手动一个个手写导致遗漏
file(GLOB MARKDOWN_HELPER_SRCS "${CMAKE_CURRENT_SOURCE_DIR}/tree_sitter_markdown/*.cc")

add_library(${CMAKE_PROJECT_NAME} SHARED
        "${TS_DIR}/lib/src/lib.c"
        parser.c
        scanner.cc
        ${MARKDOWN_HELPER_SRCS}
        tree-sitter-markdown.cpp
)

# 链接 Android 日志库
if (${CMAKE_SYSTEM_NAME} STREQUAL Android)
    find_library(log-lib log)
    target_link_libraries(${CMAKE_PROJECT_NAME} ${log-lib})
endif()
cmake_minimum_required(VERSION 3.22.1)

project("tree-sitter-php")

# 获取 NDK_COMMON_DIR (跳 3 级到达 tree-sitter-ndk)
# 路径推演: cpp -> main -> src -> php -> tree-sitter-ndk
get_filename_component(NDK_COMMON_DIR "${CMAKE_CURRENT_SOURCE_DIR}/../../.." ABSOLUTE)

include("${NDK_COMMON_DIR}/common-config.cmake")

include_directories(${CMAKE_CURRENT_SOURCE_DIR})
# 允许 scanner.c 找到自己的头文件和 common 目录的头文件
include_directories(${CMAKE_CURRENT_SOURCE_DIR}/common)
include_directories(${CMAKE_CURRENT_SOURCE_DIR}/php/src)
include_directories(${CMAKE_CURRENT_SOURCE_DIR}/php_only/src)

add_library(${CMAKE_PROJECT_NAME} SHARED
        "${TS_DIR}/lib/src/lib.c"
        
        # PHP 混合解析器
        php/src/parser.c
        php/src/scanner.c
        
        # PHP_ONLY 纯代码解析器
        php_only/src/parser.c
        php_only/src/scanner.c
        
        # JNI 绑定代码
        tree-sitter-php.cpp
)

if (${CMAKE_SYSTEM_NAME} STREQUAL Android)
    find_library(log-lib log)
    target_link_libraries(${CMAKE_PROJECT_NAME} ${log-lib})
endif()
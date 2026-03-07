#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import os
import re
import json
import argparse
from pathlib import Path

"""
AndroidIDE Tree-sitter 高亮配置全自动生成引擎
@author android_zero
 run py：python3 generate_ts_theme.py -i typeScript/queries/highlights.scm -o TypeScript.json -l TypeScript -e ts tsx mts cts
功能:
1. 准确无误地从 .scm 文件中提取所有 @capture 变量。
2. 完美处理包含代码逻辑、字符串中的分号 `;`（防止误判为注释）。
3. 自动忽略 Tree-sitter 内置查询指令（如 @match?, @eq?, @injection.*, @local.*）。
4. 智能多级回退匹配颜色（如匹配不到 property.class，会自动回退寻找 property）。
"""

# ==============================================================================
# 智能颜色映射表字典 (与 AndroidIDE default.json 完全对应)
# SCM 节点名 : { JSON 样式定义 }
# ==============================================================================
SMART_COLOR_MAP = {
    # 关键字 (Keywords)
    "keyword": {"fg": "@keyword", "bold": True},
    "keyword.control": {"fg": "@keyword", "bold": True},
    "keyword.directive": {"fg": "@kt.preproc", "bold": True},
    "keyword.modifier": {"fg": "@keyword", "italic": True},
    "keyword.visibility": {"fg": "@keyword", "bold": True},
    "keyword.operator": {"fg": "@operator", "bold": True},
    "keyword.return": {"fg": "@keyword", "bold": True},
    "keyword.function": {"fg": "@keyword", "bold": True},
    "keyword.import": {"fg": "@java.package", "bold": True},
    
    # 类型、类、命名空间 (Types & Namespaces)
    "type": {"fg": "@type", "bold": True},
    "type.builtin": {"fg": "@keyword", "bold": True},
    "type.qualifier": {"fg": "@keyword", "bold": True},
    "constructor": {"fg": "@kt.constructor", "bold": True},
    "namespace": {"fg": "@java.package"},
    "module": {"fg": "@java.package"},
    
    # 变量、属性、字段 (Variables & Properties)
    "variable": {"fg": "@variable"},
    "variable.parameter": {"fg": "@variable", "italic": True},
    "variable.builtin": {"fg": "@keyword", "bold": True},
    "variable.other.member": {"fg": "@field"},
    "property": {"fg": "@field"},
    "field": {"fg": "@field"},
    "attribute": {"fg": "@attribute", "italic": True},
    "annotation": {"fg": "@attribute", "italic": True},
    
    # 函数与方法 (Functions & Methods)
    "function": {"fg": "@func.decl", "bold": True},
    "function.call": {"fg": "@func.call"},
    "function.builtin": {"fg": "@func.call", "bold": True, "italic": True},
    "function.macro": {"fg": "@func.call", "bold": True},
    "method": {"fg": "@func.decl", "bold": True},
    "method.call": {"fg": "@func.call"},
    
    # 字面量与数据 (Literals)
    "string": {"fg": "@string", "maybeHexColor": True},
    "string.regex": {"fg": "@string"},
    "string.escape": {"fg": "@kt.string.esc", "bold": True},
    "string.special": {"fg": "@string", "bold": True},
    "number": {"fg": "@number"},
    "float": {"fg": "@number"},
    "boolean": {"fg": "@keyword", "bold": True},
    "constant": {"fg": "@constant", "bold": True},
    "constant.builtin": {"fg": "@keyword", "bold": True},
    "constant.macro": {"fg": "@constant", "bold": True},
    
    # 注释 (Comments)
    "comment": {"fg": "@comment", "italic": True},
    "comment.documentation": {"fg": "@comment", "italic": True, "bold": True},
    
    # 操作符与标点符号 (Operators & Punctuation)
    "operator": {"fg": "@operator"},
    "punctuation.delimiter": {"fg": "@onSurfaceVariant"},
    "punctuation.bracket": {"fg": "@outline"},
    "punctuation.special": {"fg": "@kt.punctuation.special"},
    
    # 其他 (Others)
    "label": {"fg": "@keyword", "bold": True},
    "tag": {"fg": "@xml.tag", "bold": True},
    "embedded": {"fg": "@onSurface"},
    "none": {"fg": "@onSurface"},
    "error": {"fg": "@log.err.text", "bold": True, "strikethrough": True}
}

def remove_comments_safely(scm_content: str) -> str:
    """
    安全地移除 Lisp 风格注释 (; comment)，但防止误删字符串内部的 ";"。
    """
    clean_lines =[]
    for line in scm_content.splitlines():
        in_string = False
        clean_line = ""
        for i, char in enumerate(line):
            if char == '"' and (i == 0 or line[i-1] != '\\'):
                in_string = not in_string
            
            if char == ';' and not in_string:
                break # 遇到真正的注释起始符，舍弃本行后面的内容
                
            clean_line += char
        clean_lines.append(clean_line)
    return "\n".join(clean_lines)

def extract_captures(scm_content: str) -> set:
    """
    使用正则精确提取符合 Tree-sitter 标准的 @capture 名字。
    """
    # 过滤掉注释
    code_only = remove_comments_safely(scm_content)
    
    # 匹配 @ 开头的合法字符[a-zA-Z0-9_.-]
    pattern = re.compile(r'@([\w.-]+)')
    matches = pattern.findall(code_only)
    
    valid_captures = set()
    for match in matches:
        # 1. 忽略断言谓词 (如 @match?, @eq?)
        if match.endswith('?'):
            continue
        # 2. 忽略注入指令和局部变量指令 (如 @injection.content, @local.definition)
        if match.startswith(('injection.', 'local.', 'spell', 'conceal', 'fold')):
            continue
        valid_captures.add(match)
        
    return valid_captures

def resolve_style(capture_name: str) -> dict:
    """
    多级智能回退匹配算法 (Fallback Algorithm)。
    例如传入: 'punctuation.special.html'
    查找顺序: 
      1. 'punctuation.special.html'
      2. 'punctuation.special'
      3. 'punctuation'
    匹配到即返回。如果都没有，回退到默认颜色。
    """
    parts = capture_name.split('.')
    
    while parts:
        key = '.'.join(parts)
        if key in SMART_COLOR_MAP:
            return SMART_COLOR_MAP[key]
        parts.pop() # 删除最后一级，尝试匹配上一级
        
    # 终极保底兜底颜色 (普通文本色)
    return {"fg": "@onSurface"}

def generate(input_path: str, output_file: str, extensions: list):
    """
    主生成逻辑。支持输入单个文件或包含 .scm 的目录。
    """
    all_captures = set()
    
    target_path = Path(input_path)
    if target_path.is_file():
        print(f"[*] 扫描单文件: {target_path}")
        with open(target_path, 'r', encoding='utf-8') as f:
            all_captures.update(extract_captures(f.read()))
    elif target_path.is_dir():
        print(f"[*] 扫描文件夹: {target_path}")
        for scm_file in target_path.glob('**/*.scm'):
            print(f"  -> 读取: {scm_file.name}")
            with open(scm_file, 'r', encoding='utf-8') as f:
                all_captures.update(extract_captures(f.read()))
    else:
        print(f"[!] 路径无效: {input_path}")
        return

    # 按字母顺序排序，保证 JSON 输出干净整洁
    sorted_captures = sorted(list(all_captures))
    
    styles = {}
    for cap in sorted_captures:
        styles[cap] = resolve_style(cap)

    result_json = {
        "types": extensions,
        "styles": styles
    }

    with open(output_file, 'w', encoding='utf-8') as f:
        json.dump(result_json, f, indent=2, ensure_ascii=False)
        
    print(f"\n[√] 成功! 共提取了 {len(sorted_captures)} 个语法节点映射。")
    print(f"[√] 配置文件已保存至: {output_file}\n")

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="AndroidIDE Tree-sitter 高亮配置全自动生成工具")
    parser.add_argument("-i", "--input", required=True, help="SCM文件路径，或包含SCM文件的文件夹路径")
    parser.add_argument("-o", "--output", required=True, help="输出的 .json 文件名 (如 cpplang.json)")
    parser.add_argument("-e", "--ext", required=True, nargs='+', help="文件扩展名列表 (以空格分隔，如 cpp h hpp cxx)")
    
    args = parser.parse_args()
    generate(args.input, args.output, args.ext)
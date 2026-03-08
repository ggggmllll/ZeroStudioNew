#!/usr/bin/env python3
# -*- coding: utf-8 -*-

"""
Tree-sitter Parser Analyzer & SCM Generator
@author android_zero
"""

import re
import sys
import os
import argparse

class ParserAnalyzer:
    def __init__(self, c_source_path):
        self.path = c_source_path
        self.content = ""
        # 存储 (enum_name, string_value)
        self.symbols = [] 
        # 分类容器
        self.keywords = set()
        self.operators = set()
        self.brackets = set()
        self.named_nodes = set()
        self.literals = set()
        
        # 预定义的高亮映射规则 (关键词匹配 -> Capture)
        self.heuristics = [
            (r'comment', 'comment'),
            (r'string', 'string'),
            (r'char', 'string'),
            (r'number', 'number'),
            (r'integer', 'number'),
            (r'float', 'number'),
            (r'bool', 'boolean'),
            (r'identifier', 'variable'),
            (r'name', 'variable'),
            (r'type', 'type'),
            (r'class', 'type'),
            (r'struct', 'type'),
            (r'enum', 'type'),
            (r'interface', 'type'),
            (r'function', 'function'),
            (r'method', 'function'),
            (r'parameter', 'variable.parameter'),
            (r'argument', 'variable.parameter'),
            (r'field', 'property'),
            (r'property', 'property'),
            (r'label', 'label'),
            (r'constant', 'constant'),
            (r'attribute', 'attribute'),
            (r'annotation', 'attribute'),
            (r'decorator', 'attribute'),
            (r'macro', 'function.macro'),
            (r'preproc', 'keyword.directive'),
        ]

    def load_and_parse(self):
        print(f"[-] 正在读取文件: {self.path}")
        try:
            with open(self.path, 'r', encoding='utf-8', errors='ignore') as f:
                self.content = f.read()
        except Exception as e:
            print(f"[!] 读取失败: {e}")
            sys.exit(1)

        self._extract_symbol_names()
        self._classify_symbols()

    def _extract_symbol_names(self):
        print("[-] 正在提取符号表 (ts_symbol_names)...")
        # 匹配 C 数组: static const char * const ts_symbol_names[] = { ... };
        # 使用 DOTALL 模式匹配跨行内容
        pattern = re.compile(r'ts_symbol_names\[\]\s*=\s*\{(.*?)\};', re.DOTALL)
        match = pattern.search(self.content)
        
        if not match:
            print("[!] 致命错误：无法在源码中找到 ts_symbol_names 数组！请确认这是有效的 parser.c 文件。")
            sys.exit(1)

        body = match.group(1)
        
        # 提取每一行: [sym_xxx] = "xxx", 或 [anon_sym_xxx] = "xxx",
        # 正则解释：
        # \[(\w+)\]    -> 捕获 enum 键名 (如 sym_identifier)
        # \s*=\s*      -> 匹配等号
        # "((?:[^"\\]|\\.)*)" -> 捕获字符串值，支持转义引号
        entry_pattern = re.compile(r'\[(\w+)\]\s*=\s*"((?:[^"\\]|\\.)*)"')
        
        for line in body.split('\n'):
            m = entry_pattern.search(line)
            if m:
                enum_name = m.group(1)
                raw_value = m.group(2)
                # 处理 C 语言转义字符
                str_value = raw_value.encode('utf-8').decode('unicode_escape')
                self.symbols.append((enum_name, str_value))
        
        print(f"[+] 成功提取 {len(self.symbols)} 个符号定义。")

    def _classify_symbols(self):
        for enum, val in self.symbols:
            # 1. 过滤特殊符号
            if val in ["end", ""]: continue
            if enum.startswith("aux_sym"): continue # 辅助符号通常不用于高亮
            if val.startswith("_"): continue        # 隐藏节点不应显式出现在 SCM

            # 2. 区分 命名节点 vs 匿名符号
            # 命名节点通常以 sym_ 开头 (非 anon_sym_)
            is_named = enum.startswith("sym_")
            
            if not is_named:
                # 匿名符号处理 (Operators, Keywords, Punctuation)
                if val.isalpha(): 
                    # 纯字母，通常是关键字 (class, if, return)
                    if len(val) > 1: self.keywords.add(val)
                elif val in ["{", "}", "[", "]", "(", ")"]:
                    self.brackets.add(val)
                else:
                    # 包含符号，视为操作符或分隔符
                    self.operators.add(val)
            else:
                # 命名节点处理 (Identifier, String, Function...)
                # 兜底放入 named_nodes，具体的分类在生成时做
                self.named_nodes.add(val)

                # 特殊处理常量字面量
                if val in ["true", "false", "null", "nullptr"]:
                    self.literals.add((val, "constant.builtin"))

    def generate_scm_file(self, output_dir):
        if not os.path.exists(output_dir):
            os.makedirs(output_dir)
            
        hl_path = os.path.join(output_dir, "highlights.scm")
        
        with open(hl_path, 'w', encoding='ascii') as f:
            f.write("; ===================================================\n")
            f.write("; Auto-generated from parser.c source code analysis\n")
            f.write("; Validated against ts_symbol_names to ensure NO CRASHES\n")
            f.write("; @author android_zero\n")
            f.write("; ===================================================\n\n")

            # 1. 写入关键字 (Keywords)
            if self.keywords:
                f.write("; --- Keywords ---\n")
                f.write("[\n")
                for kw in sorted(self.keywords):
                    f.write(f'  "{kw}"\n')
                f.write("] @keyword\n\n")

            # 2. 写入操作符 (Operators & Delimiters)
            if self.operators:
                f.write("; --- Operators & Punctuation ---\n")
                f.write("[\n")
                for op in sorted(self.operators):
                    # 安全转义
                    safe_op = op.replace('\\', '\\\\').replace('"', '\\"')
                    f.write(f'  "{safe_op}"\n')
                f.write("] @operator\n\n")
                
            # 3. 写入括号 (Brackets)
            if self.brackets:
                f.write("; --- Brackets ---\n")
                f.write("[\n")
                for b in sorted(self.brackets):
                     f.write(f'  "{b}"\n')
                f.write("] @punctuation.bracket\n\n")

            # 4. 写入命名节点 (Named Nodes) - 核心部分
            f.write("; --- Syntax Nodes (Validated) ---\n")
            
            # 先处理已知的字面量
            for node, capture in self.literals:
                f.write(f"({node}) @{capture}\n")

            # 智能匹配命名节点
            # 使用列表副本防止迭代修改
            remaining_nodes = sorted(list(self.named_nodes))
            
            for node in remaining_nodes:
                # 跳过已经处理过的字面量
                if node in [x[0] for x in self.literals]: continue
                
                # 启发式匹配
                matched = False
                for pattern, capture in self.heuristics:
                    if re.search(pattern, node):
                        f.write(f"({node}) @{capture}\n")
                        matched = True
                        break
                
                # 如果没匹配到，作为普通变量兜底 (防止黑字)
                if not matched:
                    # 只有看起来像标识符的才作为变量，其他的忽略以防干扰
                    if "identifier" in node or "name" in node:
                        f.write(f"({node}) @variable\n")

        print(f"[+] 成功生成 Highlights SCM: {hl_path}")

    def generate_locals_scm(self, output_dir):
        # 即使只能做简单的猜测，也比没有强，且必须保证节点存在
        locals_path = os.path.join(output_dir, "locals.scm")
        with open(locals_path, 'w', encoding='ascii') as f:
            f.write("; Auto-generated safe locals.scm\n\n")
            
            # 查找可能的定义节点
            defs = [n for n in self.named_nodes if "declarator" in n or "parameter" in n]
            refs = [n for n in self.named_nodes if "identifier" in n or "name" in n]
            scopes = [n for n in self.named_nodes if "block" in n or "body" in n]

            if defs:
                f.write("; Definitions\n")
                for d in sorted(defs):
                    # 扁平化匹配，不使用字段名
                    f.write(f"({d}) @local.definition\n")
                f.write("\n")

            if refs:
                f.write("; References\n")
                for r in sorted(refs):
                    f.write(f"({r}) @local.reference\n")
                f.write("\n")

            if scopes:
                f.write("; Scopes\n")
                for s in sorted(scopes):
                    f.write(f"({s}) @local.scope\n")

        print(f"[+] 成功生成 Locals SCM: {locals_path}")

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Generate strict & valid SCM from parser.c")
    parser.add_argument("-c", "--source", required=True, help="Path to parser.c file")
    parser.add_argument("-o", "--outdir", required=True, help="Output directory for .scm files")

    args = parser.parse_args()

    if not os.path.isfile(args.source):
        print(f"[!] 错误：找不到文件 {args.source}")
        sys.exit(1)

    analyzer = ParserAnalyzer(args.source)
    analyzer.load_and_parse()
    analyzer.generate_scm_file(args.outdir)
    analyzer.generate_locals_scm(args.outdir)
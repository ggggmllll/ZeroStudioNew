import tomllib
import re
import sys

# 【配置区】项目引用的基础路径前缀
# 遇到 project(":xxx") 时，如果在下面的字典里，就用字典的；否则默认加前缀 projects.core.chatai.
PROJECT_MAPPING = {
    "common": "projects.core.common",
    # 你可以在这里补充更多特定的映射，例如： "core": "projects.core"
}
DEFAULT_PROJECT_PREFIX = "projects.core.chatai."

def check_python_version():
    if sys.version_info < (3, 11):
        print("错误: 此脚本需要 Python 3.11 或更高版本（使用了原生的 tomllib）。")
        sys.exit(1)

def get_group_artifact(lib_val):
    """提取依赖的严格标识 group:name"""
    if isinstance(lib_val, str):
        parts = lib_val.split(':')
        if len(parts) >= 2:
            return f"{parts[0]}:{parts[1]}"
    elif isinstance(lib_val, dict):
        if 'module' in lib_val:
            return lib_val['module']
        elif 'group' in lib_val and 'name' in lib_val:
            return f"{lib_val['group']}:{lib_val['name']}"
    return None

def get_plugin_id(plugin_val):
    """提取插件的严格标识 id"""
    if isinstance(plugin_val, str):
        return plugin_val.split(':')[0]
    elif isinstance(plugin_val, dict):
        return plugin_val.get('id')
    return None

def accessor_to_key(accessor):
    """将 libs.xxx.yyy 的形式还原匹配 (实际是在预处理时使用)"""
    return accessor.replace('-', '.').replace('_', '.')

def format_toml_value(val):
    """格式化字典输出为 TOML 格式"""
    if isinstance(val, str):
        return f'"{val}"'
    elif isinstance(val, dict):
        parts = []
        for k, v in val.items():
            if k == "version.ref":
                parts.append(f'version.ref = "{v}"')
            else:
                parts.append(f'{k} = "{v}"')
        return "{ " + ", ".join(parts) + " }"
    return str(val)

def main():
    check_python_version()

    try:
        with open("a_libs.toml", "rb") as f:
            toml_a = tomllib.load(f)
        with open("b_libs.toml", "rb") as f:
            toml_b = tomllib.load(f)
        with open("b_build.kts", "r", encoding="utf-8") as f:
            gradle_script = f.read()
    except FileNotFoundError as e:
        print(f"文件读取失败，请确保目录下有对应的文件: {e}")
        sys.exit(1)

    # 1. 解析 A 项目，建立 真实标识 -> A项目调用名 的映射
    a_module_to_key = {}
    for k, v in toml_a.get('libraries', {}).items():
        mod = get_group_artifact(v)
        if mod: a_module_to_key[mod] = k

    a_plugin_to_key = {}
    for k, v in toml_a.get('plugins', {}).items():
        pid = get_plugin_id(v)
        if pid: a_plugin_to_key[pid] = k

    # 2. 解析 B 项目，建立 B项目调用名 -> 真实标识 的映射
    b_accessor_to_key = {}
    for k in toml_b.get('libraries', {}).keys():
        b_accessor_to_key[accessor_to_key(k)] = k

    b_plugin_accessor_to_key = {}
    for k in toml_b.get('plugins', {}).keys():
        b_plugin_accessor_to_key[accessor_to_key(k)] = k

    missing_versions = {}
    missing_libs = {}
    missing_plugins = {}

    def replace_dependency(match):
        full_match = match.group(0) # e.g., libs.androidx.core.ktx
        accessor = full_match.replace("libs.", "")

        # 处理 Plugins
        if accessor.startswith("plugins."):
            acc = accessor.replace("plugins.", "")
            b_key = b_plugin_accessor_to_key.get(acc)
            if not b_key: return full_match
            
            b_val = toml_b['plugins'][b_key]
            pid = get_plugin_id(b_val)
            
            if pid in a_plugin_to_key:
                a_key = a_plugin_to_key[pid]
                return "libs.plugins." + accessor_to_key(a_key)
            else:
                # A项目缺失此插件
                missing_plugins[b_key] = b_val
                if isinstance(b_val, dict) and 'version.ref' in b_val:
                    v_ref = b_val['version.ref']
                    if v_ref in toml_b.get('versions', {}):
                        missing_versions[v_ref] = toml_b['versions'][v_ref]
                return full_match

        # 处理 Bundles (跳过处理，因为Bundle的内部内容需单独对齐)
        if accessor.startswith("bundles."):
            return full_match

        # 处理 Libraries
        b_key = b_accessor_to_key.get(accessor)
        if not b_key: return full_match # 在B的TOML里都没找到，原样返回
        
        b_val = toml_b['libraries'][b_key]
        mod = get_group_artifact(b_val)
        
        if mod in a_module_to_key:
            # 完美命中 A 项目已存在的依赖
            a_key = a_module_to_key[mod]
            return "libs." + accessor_to_key(a_key)
        else:
            # A 项目中缺失
            missing_libs[b_key] = b_val
            if isinstance(b_val, dict) and 'version.ref' in b_val:
                v_ref = b_val['version.ref']
                if v_ref in toml_b.get('versions', {}):
                    missing_versions[v_ref] = toml_b['versions'][v_ref]
            return full_match

    def replace_projects(match):
        # 将 project(":xxx:yyy") 转换为 projects.xxx.yyy
        proj_path = match.group(1).lstrip(':')
        if proj_path in PROJECT_MAPPING:
            return PROJECT_MAPPING[proj_path]
        
        # 针对 :ai 自动转为 projects.core.chatai.ai
        parts = proj_path.split(':')
        return DEFAULT_PROJECT_PREFIX + ".".join(parts)

    # 执行替换：Projects 引用
    new_script = re.sub(r'project\([\'"]:(.*?)[\'"]\)', replace_projects, gradle_script)
    
    # 执行替换：libs 引用 (匹配 libs.任意字母数字下划线及点号)
    new_script = re.sub(r'libs\.[A-Za-z0-9_\.]+', replace_dependency, new_script)

    # ------------------ 输出结果 ------------------
    print("="*60)
    print("✅ 转换完成的 build.gradle.kts 内容 (请复制):")
    print("="*60)
    print(new_script)
    
    print("\n" + "="*60)
    print("⚠️ 查漏补缺：需要在 A 项目 libs.versions.toml 中手动添加的内容:")
    print("="*60)
    
    if not (missing_versions or missing_libs or missing_plugins):
        print("完美！A 项目的 TOML 包含所需的所有依赖，无需添加任何内容。")
    else:
        if missing_versions:
            print("[versions]")
            for k, v in missing_versions.items():
                print(f'{k} = "{v}"')
            print("")
            
        if missing_libs:
            print("[libraries]")
            for k, v in missing_libs.items():
                print(f'{k} = {format_toml_value(v)}')
            print("")
            
        if missing_plugins:
            print("[plugins]")
            for k, v in missing_plugins.items():
                print(f'{k} = {format_toml_value(v)}')

if __name__ == "__main__":
    main()
import os
import re

def replace_package_name():
    search_text = "com.termux"
    replace_text = "com.itsaky.androidide"
    
    if not search_text or not replace_text:
        print("❌ 错误：未提供搜索或替换内容")
        return

    pattern = re.compile(r'\b' + re.escape(search_text) + r'\b')

    for root, dirs, files in os.walk("."):
        
        if ".git" in dirs:
            dirs.remove(".git")
        if ".github" in dirs:
            dirs.remove(".github")
            
        for file_name in files:
            
            if file_name == "replace_package.py":
                continue
                
            file_path = os.path.join(root, file_name)
            
            if not os.path.exists(file_path) or os.path.islink(file_path):
                continue

            try:
                with open(file_path, 'r', encoding='utf-8', errors='ignore') as f:
                    content = f.read()
                
                if pattern.search(content):
                    new_content = pattern.sub(replace_text, content)
                    with open(file_path, 'w', encoding='utf-8') as f:
                        f.write(new_content)
                    print(f"✅ 已更新: {file_path}")
            except Exception as e:
                print(f"⚠️ 跳过文件 {file_path}: {e}")
                continue

if __name__ == "__main__":
    replace_package_name()
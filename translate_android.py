import os
import argparse
import requests
import re
import json
import xml.etree.ElementTree as ET
from openai import OpenAI

# 语言映射
LANG_MAP = {
    "values-ar-rSA": "Arabic", "values-bn-rIN": "Bengali", "values-de-rDE": "German",
    "values-es-rES": "Spanish", "values-fa": "Persian", "values-fil": "Filipino",
    "values-fr-rFR": "French", "values-hi-rIN": "Hindi", "values-in-rID": "Indonesian",
    "values-it": "Italian", "values-ja": "Japanese", "values-ko": "Korean",
    "values-ml": "Malayalam", "values-pl": "Polish", "values-pt-rBR": "Brazilian Portuguese",
    "values-ro-rRO": "Romanian", "values-ru-rRU": "Russian", "values-ta": "Tamil",
    "values-th": "Thai", "values-tm-rTM": "Turkmen", "values-tr-rTR": "Turkish",
    "values-uk": "Ukrainian", "values-vi": "Vietnamese", "values-zh-rCN": "Simplified Chinese",
    "values-zh-rTW": "Traditional Chinese"
}

def fix_format(text):
    """修复 Android XML 特殊符号和占位符空格"""
    if not text: return ""
    result = text.replace("'", "\\'") # 单引号转义
    result = re.sub(r"% \s*([sd])", r"%\1", result) # 修复 % s -> %s
    result = re.sub(r"% \s*(\d+\$)\s*([sd])", r"%\1\2", result) # 修复 % 1$ s -> %1$s
    result = result.replace("] ] >", "]]>") # 修复 CDATA
    result = result.replace("\\ n", "\\n") # 修复换行符空格
    return result

def get_best_model(api_key, base_url):
    try:
        headers = {"Authorization": f"Bearer {api_key}"}
        response = requests.get(f"{base_url.rstrip('/')}/models", headers=headers, timeout=10)
        models = [m["id"] for m in response.json().get("data", [])]
        # 优先选择性能强的模型
        for kw in ["deepseek-v3", "qwen2.5-72b", "deepseek-chat"]:
            match = next((m for m in models if kw in m.lower()), None)
            if match: return match
        return models[0] if models else None
    except:
        return "deepseek-ai/DeepSeek-V3" # 硅基流动默认缺省

def translate_via_json(client, model, kv_pairs, target_lang):
    """使用 JSON 格式进行翻译，确保 Key-Value 对应"""
    if not kv_pairs: return {}
    
    system_prompt = f"""You are a professional translator specializing in mobile app localization.
Translate the following Android app string resources from English to {target_lang}.

Important guidelines:
1. Preserve all Android string placeholders like %1$s, %1$d, %s, %d exactly as they are.
2. Keep special characters like \\n, \\', &amp;, etc.
3. Maintain the same tone and style (formal/informal) as the source.
4. For UI text, keep it concise.
5. Do not translate brand names or technical terms that should remain in English.

Respond with a JSON object mapping the same keys to translated values.
Only output the JSON, no other text."""

    try:
        response = client.chat.completions.create(
            model=model,
            messages=[
                {"role": "system", "content": system_prompt},
                {"role": "user", "content": json.dumps(kv_pairs, ensure_ascii=False)}
            ],
            response_format={"type": "json_object"}, # 强制要求 JSON 输出
            temperature=0.2
        )
        translated_data = json.loads(response.choices.message.content)
        # 对每个翻译结果应用格式修复
        return {k: fix_format(v) for k, v in translated_data.items()}
    except Exception as e:
        print(f"❌ Translation Error: {e}")
        return {}

def process_module(client, model, res_path):
    source_xml = os.path.join(res_path, "values/strings.xml")
    if not os.path.exists(source_xml): return

    tree = ET.parse(source_xml)
    root = tree.getroot()
    
    # 只提取可翻译的内容：没有 translatable="false"
    kv_to_translate = {}
    for s in root.findall("string"):
        name = s.get("name")
        is_trans = s.get("translatable", "true").lower() != "false"
        if is_trans and s.text:
            kv_to_translate[name] = s.text

    if not kv_to_translate: return

    for folder, lang_name in LANG_MAP.items():
        print(f"🚀 Translating {res_path} -> {lang_name}...")
        translated_map = translate_via_json(client, model, kv_to_translate, lang_name)
        
        # 创建新的 XML 树（深拷贝）
        new_tree = ET.parse(source_xml)
        new_root = new_tree.getroot()
        
        for s in new_root.findall("string"):
            name = s.get("name")
            if name in translated_map:
                s.text = translated_map[name]
        
        out_dir = os.path.join(res_path, folder)
        os.makedirs(out_dir, exist_ok=True)
        new_tree.write(os.path.join(out_dir, "strings.xml"), encoding="utf-8", xml_declaration=True)

if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("--api_key", required=True)
    parser.add_argument("--base_url", default="https://api.siliconflow.cn")
    args = parser.parse_args()

    model = get_best_model(args.api_key, args.base_url)
    print(f"🤖 Selected Model: {model}")
    
    client = OpenAI(api_key=args.api_key, base_url=args.base_url)
    for path in ["core/resources/src/main/res/", "core/chatai/resources/src/main/res/"]:
        process_module(client, model, path)
    print("✅ All Done!")

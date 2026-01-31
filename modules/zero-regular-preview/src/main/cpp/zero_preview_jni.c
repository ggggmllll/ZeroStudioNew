#include <jni.h>
#include <stdlib.h>
#include <stdio.h>
#include <string.h>

// 包含 Oniguruma 头文件
#include "oniguruma.h"
#include "regparse.h"
#include "regint.h" 

// --- 修复宏定义缺失 ---
#ifndef ONIGENC_CTYPE_ANYCHAR
#define ONIGENC_CTYPE_ANYCHAR -1
#endif

// --- StringBuilder 工具 (保持轻量高效) ---
typedef struct { char* buffer; size_t length; size_t capacity; } StringBuilder;

static void sb_init(StringBuilder* sb) { 
    sb->capacity = 8192; // 增加初始容量，应对复杂正则
    sb->length = 0; 
    sb->buffer = (char*)malloc(sb->capacity); 
    if(sb->buffer) sb->buffer[0] = '\0'; 
}

static void sb_destroy(StringBuilder* sb) { 
    if(sb->buffer) free(sb->buffer); 
}

static void sb_ensure(StringBuilder* sb, size_t needed) {
    if (sb->length + needed >= sb->capacity) {
        size_t new_cap = sb->capacity * 2 + needed;
        char* new_buf = (char*)realloc(sb->buffer, new_cap);
        if(new_buf) { sb->buffer = new_buf; sb->capacity = new_cap; }
    }
}

static void sb_append(StringBuilder* sb, const char* str) {
    if(!str || !sb->buffer) return;
    size_t len = strlen(str);
    sb_ensure(sb, len);
    memcpy(sb->buffer + sb->length, str, len);
    sb->length += len;
    sb->buffer[sb->length] = '\0';
}

static void sb_append_char(StringBuilder* sb, char c) { 
    char s[2] = {c, 0}; 
    sb_append(sb, s); 
}

static void sb_append_int(StringBuilder* sb, int val) { 
    char temp[32]; 
    sprintf(temp, "%d", val); 
    sb_append(sb, temp); 
}

static void sb_append_bool(StringBuilder* sb, int val) { 
    sb_append(sb, val ? "true" : "false"); 
}

// JSON 字符串转义：处理 " \ / \b \f \n \r \t 以及控制字符
static void sb_append_json_string(StringBuilder* sb, const char* str, int len) {
    sb_ensure(sb, len * 6 + 2);
    for (int i = 0; i < len; ++i) {
        unsigned char c = (unsigned char)str[i];
        switch (c) {
            case '"': sb_append(sb, "\\\""); break;
            case '\\': sb_append(sb, "\\\\"); break;
            case '/': sb_append(sb, "\\/"); break;
            case '\b': sb_append(sb, "\\b"); break;
            case '\f': sb_append(sb, "\\f"); break;
            case '\n': sb_append(sb, "\\n"); break;
            case '\r': sb_append(sb, "\\r"); break;
            case '\t': sb_append(sb, "\\t"); break;
            default:
                if (c < 32 || c > 126) { 
                    char hex[8];
                    sprintf(hex, "\\u%04x", c);
                    sb_append(sb, hex);
                } else {
                    char s[2] = {(char)c, '\0'};
                    sb_append(sb, s);
                }
        }
    }
}

// 辅助：将 CodePoint 转为 JSON 字符串内容（用于字符集范围显示）
static void append_code_point_str(StringBuilder* sb, OnigCodePoint code) {
    if (code >= 32 && code <= 126 && code != '"' && code != '\\') {
        char s[2] = {(char)code, 0};
        sb_append(sb, s);
    } else {
        char hex[10];
        sprintf(hex, "\\\\u%04X", code); // JSON escape within a string
        sb_append(sb, hex);
    }
}

// --- 深度解析字符集 (Character Class) ---
static void serialize_cclass(StringBuilder* json, CClassNode* cc, OnigEncoding enc) {
    sb_append(json, "[");
    int first = 1;
    
    // 1. 遍历 ASCII BitSet (0-255)
    // 优化：合并连续的范围，例如 97,98,99 -> "a-c"
    int range_start = -1;
    for (int i = 0; i < 256; i++) {
        if (BITSET_AT(cc->bs, i)) {
            if (range_start == -1) range_start = i;
        } else {
            if (range_start != -1) {
                if (!first) sb_append(json, ",");
                sb_append(json, "\"");
                append_code_point_str(json, range_start);
                if (i - 1 > range_start) {
                    sb_append(json, "-");
                    append_code_point_str(json, i - 1);
                }
                sb_append(json, "\"");
                first = 0;
                range_start = -1;
            }
        }
    }
    // 处理结尾的范围（如果 range_start 还是非 -1）
    if (range_start != -1) {
        if (!first) sb_append(json, ",");
        sb_append(json, "\"");
        append_code_point_str(json, range_start);
        if (255 > range_start) {
            sb_append(json, "-");
            append_code_point_str(json, 255);
        }
        sb_append(json, "\"");
        first = 0;
    }

    // 2. 遍历 Multibyte Ranges (mbuf)
    if (cc->mbuf) {
        BBuf* bbuf = cc->mbuf;
        OnigCodePoint* p = (OnigCodePoint*)bbuf->p;
        // p[0] 是范围的数量
        OnigCodePoint n = p[0]; 
        
        for (int i = 0; i < n; i++) {
            if (!first) sb_append(json, ",");
            OnigCodePoint from = p[i * 2 + 1];
            OnigCodePoint to = p[i * 2 + 2];
            
            sb_append(json, "\"");
            append_code_point_str(json, from);
            if (to > from) {
                sb_append(json, "-");
                append_code_point_str(json, to);
            }
            sb_append(json, "\"");
            first = 0;
        }
    }
    sb_append(json, "]");
}

// 前置声明
static void traverse_node(Node* node, StringBuilder* json, OnigEncoding enc);

// --- 核心 AST 遍历 ---
static void traverse_node(Node* node, StringBuilder* json, OnigEncoding enc) {
    if (!node) { 
        sb_append(json, "null"); 
        return; 
    }

    sb_append(json, "{");
    
    // 原始类型保留用于调试
    sb_append(json, "\"rawType\": ");
    sb_append_int(json, ND_TYPE(node));
    sb_append(json, ",");

    switch (ND_TYPE(node)) {
        // --- 1. 序列 Sequence ---
        case ND_LIST:
            sb_append(json, "\"railType\": \"SEQUENCE\",");
            sb_append(json, "\"children\": [");
            {
                Node* curr = node;
                int first = 1;
                while (curr) {
                    // ND_LIST 的结构是：CAR(当前内容) -> CDR(下一个列表节点)
                    // 需要递归展开 ND_CAR
                    if (!first) sb_append(json, ",");
                    traverse_node(ND_CAR(curr), json, enc);
                    curr = ND_CDR(curr);
                    first = 0;
                }
            }
            sb_append(json, "]");
            break;

        // --- 2. 分支 Alternation ---
        case ND_ALT:
            sb_append(json, "\"railType\": \"ALTERNATION\",");
            sb_append(json, "\"left\": ");
            traverse_node(ND_CAR(node), json, enc);
            sb_append(json, ", \"right\": ");
            traverse_node(ND_CDR(node), json, enc);
            break;

        // --- 3. 字面值 Literals (String) ---
        case ND_STRING: {
            StrNode* sn = STR_(node);
            int len = (int)(sn->end - sn->s);
            sb_append(json, "\"railType\": \"LITERAL\",");
            sb_append(json, "\"text\": \"");
            sb_append_json_string(json, (const char*)sn->s, len);
            sb_append(json, "\"");
            break;
        }

        // --- 4. 字符集 Character Set ---
        case ND_CCLASS:
            sb_append(json, "\"railType\": \"CHARSET\",");
            sb_append(json, "\"invert\": "); 
            sb_append_bool(json, IS_NCCLASS_NOT(CCLASS_(node)));
            sb_append(json, ", \"ranges\": ");
            serialize_cclass(json, CCLASS_(node), enc);
            break;

        // --- 5. 转义序列 Escape / 6. 任意字符 Any Char ---
        case ND_CTYPE:
            // 修复：使用上面定义的宏
            if (CTYPE_(node)->ctype == ONIGENC_CTYPE_ANYCHAR) { 
                 sb_append(json, "\"railType\": \"ANY_CHAR\"");
            } else {
                sb_append(json, "\"railType\": \"ESCAPE\",");
                sb_append(json, "\"escType\": ");
                sb_append_int(json, CTYPE_(node)->ctype);
                sb_append(json, ", \"invert\": ");
                sb_append_bool(json, CTYPE_(node)->not);
            }
            break;

        // --- 7. 量词 Quantifiers ---
        case ND_QUANT:
            sb_append(json, "\"railType\": \"QUANTIFIER\",");
            sb_append(json, "\"min\": ");
            sb_append_int(json, QUANT_(node)->lower);
            sb_append(json, ", \"max\": ");
            sb_append_int(json, QUANT_(node)->upper); // -1 代表无限
            sb_append(json, ", \"greedy\": ");
            sb_append_bool(json, QUANT_(node)->greedy);
            sb_append(json, ", \"body\": ");
            traverse_node(ND_BODY(node), json, enc);
            break;

        // --- 8. 子表达式 (Groups) ---
        case ND_BAG:
            if (BAG_(node)->type == BAG_MEMORY) {
                // 捕获组
                sb_append(json, "\"railType\": \"GROUP\",");
                sb_append(json, "\"isCapture\": true,");
                sb_append(json, "\"groupNum\": ");
                sb_append_int(json, BAG_(node)->m.regnum);
                sb_append(json, ", \"body\": ");
                traverse_node(ND_BODY(node), json, enc);
            } else if (BAG_(node)->type == BAG_STOP_BACKTRACK) {
                // 原子组 (?>...)
                sb_append(json, "\"railType\": \"GROUP\",");
                sb_append(json, "\"isCapture\": false,");
                sb_append(json, "\"subType\": \"atomic\",");
                sb_append(json, "\"body\": ");
                traverse_node(ND_BODY(node), json, enc);
            } else if (BAG_(node)->type == BAG_OPTION) {
                // 选项 (?i:...)
                sb_append(json, "\"railType\": \"GROUP\",");
                sb_append(json, "\"isCapture\": false,");
                sb_append(json, "\"subType\": \"option\",");
                sb_append(json, "\"body\": ");
                traverse_node(ND_BODY(node), json, enc);
            } else {
                // 其他 BAG (如 IF-ELSE 等，暂时作为普通非捕获组处理)
                sb_append(json, "\"railType\": \"GROUP\",");
                sb_append(json, "\"isCapture\": false,");
                sb_append(json, "\"body\": ");
                traverse_node(ND_BODY(node), json, enc);
            }
            break;

        // --- 9. 锚点 Anchors & 10. 环视 Lookaround ---
        case ND_ANCHOR:
            // 区分简单锚点和带子表达式的环视
            if (ANCHOR_(node)->type & (ANCR_PREC_READ | ANCR_PREC_READ_NOT | ANCR_LOOK_BEHIND | ANCR_LOOK_BEHIND_NOT)) {
                // 环视 Lookaround
                sb_append(json, "\"railType\": \"LOOKAROUND\",");
                sb_append(json, "\"subType\": ");
                sb_append_int(json, ANCHOR_(node)->type);
                sb_append(json, ", \"body\": ");
                // 环视一定有 body (目标表达式)
                traverse_node(ND_BODY(node), json, enc);
            } else {
                // 简单锚点
                sb_append(json, "\"railType\": \"ANCHOR\",");
                sb_append(json, "\"subType\": ");
                sb_append_int(json, ANCHOR_(node)->type);
            }
            break;
            
        // --- 11. 反向引用 Backreferences ---
        case ND_BACKREF:
             sb_append(json, "\"railType\": \"BACKREF\",");
             sb_append(json, "\"index\": ");
             // 简化处理：如果有多个引用(multiplex)，只取第一个
             sb_append_int(json, (BACKREF_(node)->back_num > 0) ? BACKREF_(node)->back_static[0] : 0);
             break;
             
        // --- 12. Gimmick (callouts, etc.) ---
        case ND_GIMMICK:
             sb_append(json, "\"railType\": \"ESCAPE\",");
             sb_append(json, "\"subType\": \"gimmick\"");
             sb_append(json, ", \"text\": \"Gimmick\""); // 占位
             break;

        // --- ND_CALL (子程序调用 \g<name>) ---
        case ND_CALL:
             sb_append(json, "\"railType\": \"ESCAPE\","); // 暂时作为 Escape 处理
             sb_append(json, "\"text\": \"Call\"");
             break;

        default:
            sb_append(json, "\"railType\": \"UNKNOWN\",");
            sb_append(json, "\"text\": \"?\"");
            break;
    }
    sb_append(json, "}");
}

// JNI Entry Point
JNIEXPORT jstring JNICALL
Java_android_studio_zero_regular_expression_preview_RegexParser_nParseRegexToJson(
        JNIEnv *env, jclass clazz,
        jbyteArray pattern,
        jint encodingType,
        jint syntaxOptions) {
    
    // 获取字节数组
    jsize len = (*env)->GetArrayLength(env, pattern);
    jbyte* pat_bytes = (*env)->GetByteArrayElements(env, pattern, NULL);

    // 内存分配
    regex_t* reg = (regex_t*)malloc(sizeof(regex_t));
    if (!reg) {
         (*env)->ReleaseByteArrayElements(env, pattern, pat_bytes, JNI_ABORT);
         return NULL;
    }

    OnigErrorInfo einfo;
    ParseEnv scan_env;
    Node* root = NULL;
    
    // 编码选择 (强制 UTF-8 以保证兼容性，除非业务有特殊需求)
    // 注意：Oniguruma 的编码必须初始化
    OnigEncoding enc = ONIG_ENCODING_UTF8; 
    
    // 全局初始化 (仅需一次，加锁或保证线程安全)
    // 这里简单处理，实际生产环境建议在 JNI_OnLoad 中调用
    static int init = 0;
    if(!init) { 
        OnigEncoding encs[] = {ONIG_ENCODING_UTF8};
        onig_initialize(encs, 1); 
        init = 1; 
    }

    // 初始化 Regex 对象
    // 使用 ONIG_SYNTAX_JAVA 或 ONIG_SYNTAX_DEFAULT，
    // ONIG_SYNTAX_JAVA 支持 \p{} 等更现代的属性，更接近用户期望
    int r = onig_reg_init(reg, ONIG_OPTION_NONE, ONIGENC_CASE_FOLD_DEFAULT, enc, ONIG_SYNTAX_JAVA);
    
    if (r != ONIG_NORMAL) {
        free(reg);
        (*env)->ReleaseByteArrayElements(env, pattern, pat_bytes, JNI_ABORT);
        return NULL;
    }

    // 解析！
    // 这里的关键是：regparse.c 会填充 Node* root
    r = onig_parse_tree(&root, (UChar*)pat_bytes, (UChar*)(pat_bytes + len), reg, &scan_env);

    StringBuilder sb;
    sb_init(&sb);

    if (r == ONIG_NORMAL && root != NULL) {
        traverse_node(root, &sb, enc);
        onig_node_free(root); // 遍历完后释放 AST 内存
    } else {
        // 错误处理
        UChar s[ONIG_MAX_ERROR_MESSAGE_LEN];
        onig_error_code_to_str(s, r, &einfo);
        sb_append(&sb, "{\"error\": \"");
        sb_append_json_string(&sb, (const char*)s, strlen((char*)s));
        sb_append(&sb, "\"}");
    }

    // 清理
    onig_free(reg); // 释放 regex 对象 (它会清理内部的内存池等)
    (*env)->ReleaseByteArrayElements(env, pattern, pat_bytes, JNI_ABORT);
    
    jstring res = (*env)->NewStringUTF(env, sb.buffer);
    sb_destroy(&sb);
    return res;
}
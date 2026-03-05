/*
 *  This file is part of android-tree-sitter.
 *  @author android_zero
 */

#include <jni.h>
#include <tree_sitter/api.h>

#include "ts__onload.h"
#include "ts_php.h"

#ifdef __ANDROID__
static jint JNI_VERSION = JNI_VERSION_1_6;
#else
static jint JNI_VERSION = JNI_VERSION_10;
#endif

// 声明外部的两个 Tree-sitter C 语言入口函数
extern "C" TSLanguage *tree_sitter_php();
extern "C" TSLanguage *tree_sitter_php_only();

// 绑定标准 PHP (需要 <?php 标签)
static jlong TSLanguagePhp_getInstance(JNIEnv *env, jclass clazz) {
    return (jlong) tree_sitter_php();
}

// 绑定纯 PHP (不需要 <?php 标签)
static jlong TSLanguagePhp_getPhpOnlyInstance(JNIEnv *env, jclass clazz) {
    return (jlong) tree_sitter_php_only();
}

void TSLanguagePhp_Native_SetJniMethods(JNINativeMethod *methods, int count) {
  // 注册两个方法
  SET_JNI_METHOD(methods, TSLanguagePhp_Native_getInstance, TSLanguagePhp_getInstance);
  SET_JNI_METHOD(methods, TSLanguagePhp_Native_getPhpOnlyInstance, TSLanguagePhp_getPhpOnlyInstance);
}

JNIEXPORT jint JNI_OnLoad(JavaVM *vm, void *reserved) {
  JNIEnv *env;
  if (vm->GetEnv(reinterpret_cast<void **>(&env), JNI_VERSION) != JNI_OK) {
    LOGE("AndroidTreeSitter", "Failed to get JNIEnv* from JavaVM: %p", vm);
    return JNI_ERR;
  }

  TS_JNI_ONLOAD__DEFINE_METHODS_ARR
  TS_JNI_ONLOAD__AUTO_REGISTER(env)

  return JNI_VERSION;
}
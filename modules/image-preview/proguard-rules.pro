# 保持 ThorVG 的 JNI 包装类不被混淆
-keep class android.zero.studio.images.preview.ThorVG {
    native <methods>;
    void <init>(...);
}

# 保持用于 JNI 交互的数据类成员（如果有从 C++ 反射调用的情况）
# 当前设计主要从 Java 调 C++，但也建议保留
-keepclassmembers class android.zero.studio.images.preview.ThorVG {
    *;
}

# 保持 Coil 相关（如果开启了 Minify）
-keep class coil.** { *; }
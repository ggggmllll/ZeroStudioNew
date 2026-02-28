// GradleControllerApp.java (或 Kotlin)
package com.example.gradlecontroller;

import android.app.Application;
import com.itsaky.androidide.utils.Environment;

public class GradleControllerApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // 初始化全局环境路径
        Environment.init(this);
    }
}
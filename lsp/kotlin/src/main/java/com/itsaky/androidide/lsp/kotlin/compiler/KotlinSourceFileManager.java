/*
 *  This file is part of AndroidIDE.
 *
 *  AndroidIDE is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  AndroidIDE is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *   along with AndroidIDE.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.itsaky.androidide.lsp.kotlin.compiler;

import static java.util.Collections.emptySet;

import androidx.annotation.NonNull;

import com.itsaky.androidide.javac.config.JavacConfigProvider;
import com.itsaky.androidide.javac.services.fs.AndroidFsProviderImpl;
import com.itsaky.androidide.projects.ModuleProject;
import com.itsaky.androidide.projects.android.AndroidModule;
import com.itsaky.androidide.utils.Environment;
import com.itsaky.androidide.utils.ILogger;

import java.io.File;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Kotlin 源码文件管理器 (KotlinSourceFileManager)。
 * <p>
 * 用途：为 Kotlin Language Server 专门配置和提取 Android 模块相关的 Classpaths 与 BootClasspaths。
 * 工作流程：它会通过 {@link ModuleProject} API 来获取依赖、R 类生成物、AAR 等依赖库的 Jar 文件路径集合，
 * 并提供给 KotlinClasspathProvider 作为 KLS 引擎运行的外部环境变量使用。
 * </p>
  *  @author android_zero
 */
public class KotlinSourceFileManager {

    public static final KotlinSourceFileManager NO_MODULE;
    private static final ILogger LOG;
    private static final Map<ModuleProject, KotlinSourceFileManager> cachedFileManagers = new ConcurrentHashMap<>();

    static {
        LOG = ILogger.Companion.instance("KotlinSourceFileManager");
        NO_MODULE = new KotlinSourceFileManager(null);
    }

    private final ModuleProject module;
    private final Set<File> classPaths;
    private final Set<File> bootClassPaths;

    private KotlinSourceFileManager(final ModuleProject module) {
        this.module = module;

        AndroidFsProviderImpl.INSTANCE.init();

        if (module == null) {
            this.classPaths = emptySet();
            this.bootClassPaths = emptySet();
            return;
        }

        // Must be set before setting classpaths
        System.setProperty(JavacConfigProvider.PROP_ANDROIDIDE_JAVA_HOME, Environment.JAVA_HOME.getAbsolutePath());

        this.classPaths = configureClasspaths(module);
        this.bootClassPaths = configureBootClasspaths(module);
    }

    @NonNull
    private Set<File> configureClasspaths(final ModuleProject module) {
        if (module == null) {
            return emptySet();
        }
        return module.getCompileClasspaths();
    }

    @NonNull
    private Set<File> configureBootClasspaths(final ModuleProject module) {
        if (module == null) {
            return emptySet();
        }
        if (module instanceof AndroidModule) {
            final AndroidModule androidModule = (AndroidModule) module;
            return new java.util.HashSet<>(androidModule.getBootClassPaths());
        }
        return emptySet();
    }

    public Set<File> getClassPaths() {
        return classPaths;
    }

    public Set<File> getBootClassPaths() {
        return bootClassPaths;
    }

    public Set<File> getAllClassPaths() {
        Set<File> allPaths = new java.util.HashSet<>();
        allPaths.addAll(classPaths);
        allPaths.addAll(bootClassPaths);
        return allPaths;
    }

    public boolean isAndroidModule() {
        return module instanceof AndroidModule;
    }

    public ModuleProject getModule() {
        return module;
    }

    public void destroy() {
        // Clear references
    }

    public static KotlinSourceFileManager forModule(@NonNull ModuleProject project) {
        Objects.requireNonNull(project);
        return cachedFileManagers.computeIfAbsent(project, KotlinSourceFileManager::createForModule);
    }

    private static KotlinSourceFileManager createForModule(@NonNull ModuleProject project) {
        return new KotlinSourceFileManager(project);
    }

    public static void clearCache() {
        for (final KotlinSourceFileManager fileManager : cachedFileManagers.values()) {
            fileManager.destroy();
        }
        cachedFileManagers.clear();
    }
}
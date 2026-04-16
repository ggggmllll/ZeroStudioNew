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

import androidx.annotation.Nullable;

import com.itsaky.androidide.projects.ModuleProject;
import com.itsaky.androidide.projects.android.AndroidModule;
import com.itsaky.androidide.projects.util.BootClasspathProvider;
import com.itsaky.androidide.utils.Environment;
import com.itsaky.androidide.utils.ILogger;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Kotlin 编译器本地服务支持 (KotlinCompilerService)。
 * <p>
 * 用途：与 JavaCompilerService 类似，它扮演着向 LSP Server 暴露项目内可用顶层类型 (Types) 和包名支持的中间层。
 * 并托管该模块特有的 {@link KotlinSourceFileManager} 供提取。
 * </p>
 *  @author android_zero
 */
public class KotlinCompilerService {

    public static final KotlinCompilerService NO_MODULE_COMPILER = new KotlinCompilerService(null);
    private static final ILogger LOG = ILogger.Companion.instance("KotlinCompilerService");

    protected final Set<String> classPathClasses;
    protected final KotlinSourceFileManager fileManager;
    protected final ModuleProject module;
    protected Set<String> bootClasspathClasses;

    public KotlinCompilerService(@Nullable ModuleProject module) {
        this.module = module;
        if (module == null) {
            this.fileManager = KotlinSourceFileManager.NO_MODULE;
            this.classPathClasses = Collections.emptySet();
            this.bootClasspathClasses = Collections.emptySet();
        } else {
            this.fileManager = KotlinSourceFileManager.forModule(module);
            this.classPathClasses = Collections.unmodifiableSet(module.compileClasspathClasses.allClassNames());
            this.bootClasspathClasses = Collections.unmodifiableSet(getBootclasspathClasses());
        }
    }

    private Set<String> getBootclasspathClasses() {
        Set<String> bootClasspathClasses = BootClasspathProvider.getTopLevelClasses(
                Collections.singleton(Environment.ANDROID_JAR.getAbsolutePath()));

        if (module != null && module instanceof AndroidModule) {
            final AndroidModule androidModule = (AndroidModule) module;
            final List<String> classpaths =
                    androidModule.getBootClassPaths().stream().map(File::getPath).collect(Collectors.toList());
            BootClasspathProvider.update(classpaths);
            bootClasspathClasses =
                    Collections.unmodifiableSet(BootClasspathProvider.getTopLevelClasses(classpaths));
        }
        return bootClasspathClasses;
    }

    public ModuleProject getModule() {
        return module;
    }

    public void destroy() {
        if (fileManager != null) {
            fileManager.destroy();
        }
    }

    public Set<String> getAvailableTypes() {
        Set<String> all = new java.util.TreeSet<>();
        all.addAll(classPathClasses);
        all.addAll(bootClasspathClasses);
        if (module != null) {
            Set<String> sourceClasses = module.compileJavaSourceClasses.allClassNames();
            all.addAll(sourceClasses);
        }
        return all;
    }

    public boolean isClassAvailable(String className) {
        return classPathClasses.contains(className) || bootClasspathClasses.contains(className);
    }

    public KotlinSourceFileManager getFileManager() {
        return fileManager;
    }
}
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

import androidx.annotation.NonNull;

import com.itsaky.androidide.projects.ModuleProject;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 核心：模块编译器提供者缓存层 (KotlinCompilerProvider)。
 * <p>
 * 作用：跨文件与跨请求缓存和提供基于不同模块的 {@link KotlinCompilerService}。提升索引与提取效率。
 * </p>
 *  @author android_zero
 */
public class KotlinCompilerProvider {

    private static KotlinCompilerProvider sInstance;
    private final Map<ModuleProject, KotlinCompilerService> mCompilers = new ConcurrentHashMap<>();

    private KotlinCompilerProvider() {
    }

    @NonNull
    public static KotlinCompilerService get(ModuleProject module) {
        return KotlinCompilerProvider.getInstance().forModule(module);
    }

    public static KotlinCompilerProvider getInstance() {
        if (sInstance == null) {
            sInstance = new KotlinCompilerProvider();
        }
        return sInstance;
    }

    @NonNull
    public synchronized KotlinCompilerService forModule(ModuleProject module) {
        final KotlinCompilerService cached = mCompilers.get(module);
        if (cached != null && cached.getModule() != null) {
            return cached;
        }

        final KotlinCompilerService newInstance = new KotlinCompilerService(module);
        mCompilers.put(module, newInstance);

        return newInstance;
    }

    public synchronized void destroy() {
        for (final KotlinCompilerService compiler : mCompilers.values()) {
            compiler.destroy();
        }
        mCompilers.clear();
    }
}
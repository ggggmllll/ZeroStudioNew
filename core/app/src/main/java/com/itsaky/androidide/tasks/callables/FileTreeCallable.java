/************************************************************************************
 * This file is part of AndroidIDE.
 *
 *
 *
 * AndroidIDE is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * AndroidIDE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with AndroidIDE.  If not, see <https://www.gnu.org/licenses/>.
 *
 **************************************************************************************/

package com.itsaky.androidide.tasks.callables;

import android.content.Context;
import android.zero.studio.view.filetree.interfaces.FileObject;
import android.zero.studio.view.filetree.model.Node;
import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.concurrent.Callable;

/**
 * [Deprecated] 
 * 随着项目重构至 android.zero.studio.view.filetree，树状结构的遍历加载已被新 API 内部集成。
 * 保留该类以防止任何意外的依赖中断。
 * 
 * @author android_zero
 */
@Deprecated
public class FileTreeCallable implements Callable<Boolean> {
  private final Context ctx;
  private final Node<FileObject> parent;
  private final File file;

  public FileTreeCallable(Context ctx, Node<FileObject> parent, File file) {
    this.ctx = ctx;
    this.parent = parent;
    this.file = file;
  }

  @Override
  public Boolean call() throws Exception {
    return true; // 现由 API 代管加载工作
  }

  public static class HiddenFilesFilter implements FileFilter {
    @Override
    public boolean accept(File p1) {
      return !p1.getName().startsWith(".");
    }
  }

  public static class SortFileName implements Comparator<File> {
    @Override
    public int compare(File f1, File f2) {
      return f1.getName().compareTo(f2.getName());
    }
  }

  public static class SortFolder implements Comparator<File> {
    @Override
    public int compare(File f1, File f2) {
      if (f1.isDirectory() == f2.isDirectory()) return 0;
      else if (f1.isDirectory() && !f2.isDirectory()) return -1;
      else return 1;
    }
  }
}

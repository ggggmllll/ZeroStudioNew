/*
 *  This file is part of android-tree-sitter.
 *  @author android_zero
 */

package com.itsaky.androidide.treesitter.php;

import com.itsaky.androidide.treesitter.annotations.GenerateNativeHeaders;
import com.itsaky.androidide.treesitter.TSLanguage;
import com.itsaky.androidide.treesitter.TSLanguageCache;

/**
 * Tree Sitter for Php and PhpOnly.
 *
 * @author Akash Yadav
 * @author android_zero (Optimized for dual-parser single-library)
 */
public final class TSLanguagePhp {

  static {
    System.loadLibrary("tree-sitter-php");
  }

  private TSLanguagePhp() {
    throw new UnsupportedOperationException();
  }

  @Deprecated
  public static TSLanguage newInstance() {
    return getInstance();
  }
  
  /**
   * Get the instance of the standard Php language (Requires <?php tags).
   */
  public static TSLanguage getInstance() {
    var language = TSLanguageCache.get("php");
    if (language != null) {
      return language;
    }

    language = TSLanguage.create("php", Native.getInstance());
    TSLanguageCache.cache("php", language);
    return language;
  }

  /**
   * Get the instance of the Php-Only language (Strict PHP, no <?php tag needed).
   * Typically used for injections.
   */
  public static TSLanguage getPhpOnlyInstance() {
    var language = TSLanguageCache.get("php_only");
    if (language != null) {
      return language;
    }

    language = TSLanguage.create("php_only", Native.getPhpOnlyInstance());
    TSLanguageCache.cache("php_only", language);
    return language;
  }

  @GenerateNativeHeaders(fileName = "php")
  public static class Native {
    public static native long getInstance();
    public static native long getPhpOnlyInstance();
  }
}
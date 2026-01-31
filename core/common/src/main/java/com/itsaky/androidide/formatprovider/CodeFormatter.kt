package com.itsaky.androidide.formatprovider

/**
 * An interface for language-specific code formatters.
 * @author android_zeros
 */
interface CodeFormatter {
    /**
     * Formats the given source code string.
     * @param source The raw source code.
     * @return The formatted source code.
     * @throws Exception if formatting fails.
     *
     *@Usage Write any formatting style or code you need in format, 
     and then write a formatting style provider immediately
     */
    fun format(source: String): String
}
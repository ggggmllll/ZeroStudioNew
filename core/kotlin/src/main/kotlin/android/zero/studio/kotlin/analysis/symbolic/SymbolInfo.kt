package android.zero.studio.kotlin.analysis.symbolic

import androidx.annotation.DrawableRes
import com.itsaky.androidide.resources.R

/**
 * Data class representing a code symbol (Class, Function, Variable, etc.).
 *
 * @property name The name of the symbol (e.g., class name, function name).
 * @property kind The type of symbol (e.g., "Class", "Function").
 * @property signature Detailed signature or description.
 * @property line The line number (0-based) where the symbol is defined.
 * @property iconRes Resource ID for the icon representing the symbol kind.
 *
 * @author android_zero
 */
data class SymbolInfo(
    val name: String,
    val kind: String,
    val signature: String,
    val line: Int,
    @DrawableRes val iconRes: Int = R.drawable.ic_code // Default icon
)
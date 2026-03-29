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

package com.itsaky.androidide.fragments.preferences

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import com.itsaky.androidide.lsp.LspBootstrap
import com.itsaky.androidide.lsp.LspManager
import com.itsaky.androidide.lsp.ui.LspSettingsScreen

/**
 * Fragment that hosts the LSP Settings UI.
 *
 * FIXES:
 * 1. Initializes LspManager (loads configs/external servers).
 * 2. Initializes LspBootstrap (loads built-in servers like Kotlin, Python, etc.).
 *
 * @author android_zero
 */
class LspSettingsFragment : Fragment() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    val context = requireContext()

    // 1. Initialize the Manager (Loads configurations and external servers from disk)
    LspManager.init(context)

    // 2. CRITICAL: Bootstrap built-in servers (Registers Kotlin, Python, XML, etc. into LspManager)
    // If this is not called, the built-in list will be empty.
    LspBootstrap.init(context)
  }

  override fun onCreateView(
      inflater: LayoutInflater,
      container: ViewGroup?,
      savedInstanceState: Bundle?,
  ): View {
    return ComposeView(requireContext()).apply {
      // Dispose the Composition when the view's LifecycleOwner is destroyed
      setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)

      setContent {
        // Apply Material Theme for Compose UI
        androidx.compose.material3.MaterialTheme { LspSettingsScreen() }
      }
    }
  }
}

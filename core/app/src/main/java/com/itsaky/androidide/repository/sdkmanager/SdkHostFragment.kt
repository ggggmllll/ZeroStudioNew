package com.itsaky.androidide.repository.sdkmanager

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.itsaky.androidide.repository.sdkmanager.ui.SdkManagerScreen
import com.itsaky.androidide.repository.sdkmanager.viewmodel.SdkManagerViewModel

/**
 * 承载纯 Compose 界面的 Fragment 容器。
 * @author android_zero
 */
class SdkHostFragment : Fragment() {
    private val sdkManagerViewModel: SdkManagerViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MaterialTheme {
                    SdkManagerScreen(
                        onClose = { requireActivity().finish() },
                        viewModel = sdkManagerViewModel
                    )
                }
            }
        }
    }
}

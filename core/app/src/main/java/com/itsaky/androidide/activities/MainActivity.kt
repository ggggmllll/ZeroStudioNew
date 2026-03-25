package com.itsaky.androidide.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.graphics.Insets
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentContainerView
import com.itsaky.androidide.R
import com.itsaky.androidide.activities.editor.EditorActivityKt
import com.itsaky.androidide.app.EdgeToEdgeIDEActivity
import com.itsaky.androidide.fragments.MainFragment
import com.itsaky.androidide.fragments.TemplateDetailsFragment
import com.itsaky.androidide.fragments.TemplateListFragment
import com.itsaky.androidide.preferences.internal.GeneralPreferences
import com.itsaky.androidide.projects.IProjectManager
import com.itsaky.androidide.templates.ITemplateProvider
import com.itsaky.androidide.utils.DialogUtils
import com.itsaky.androidide.utils.RecentProjectsManager
import com.itsaky.androidide.utils.flashInfo
import com.itsaky.androidide.viewmodel.MainViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class MainActivity : EdgeToEdgeIDEActivity() {

    private val viewModel by viewModels<MainViewModel>()

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            viewModel.apply {
                if (creatingProject.value == true) return@apply

                val newScreen = when (currentScreen.value) {
                    MainViewModel.SCREEN_TEMPLATE_DETAILS -> MainViewModel.SCREEN_TEMPLATE_LIST
                    MainViewModel.SCREEN_TEMPLATE_LIST -> MainViewModel.SCREEN_MAIN
                    else -> MainViewModel.SCREEN_MAIN
                }

                if (currentScreen.value != newScreen) {
                    setScreen(newScreen)
                }
            }
        }
    }

    override fun bindLayout(): View {
        return ComposeView(this).apply {
            id = View.generateViewId()
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MaterialTheme {
                    MainActivityScreen(viewModel)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        activityScope.launch(Dispatchers.IO) {
            tryOpenLastProject()
        }

        viewModel.currentScreen.observe(this) { screen ->
            if (screen == -1) return@observe
            onBackPressedCallback.isEnabled = screen != MainViewModel.SCREEN_MAIN
        }

        if (viewModel.currentScreen.value == -1 && viewModel.previousScreen == -1) {
            viewModel.setScreen(MainViewModel.SCREEN_MAIN)
        }

        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }

    @Composable
    private fun MainActivityScreen(viewModel: MainViewModel) {
        val currentScreen by viewModel.currentScreen.observeAsState(MainViewModel.SCREEN_MAIN)
        
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.surface
        ) { padding ->
            AndroidView(
                modifier = Modifier.fillMaxSize().padding(padding).background(MaterialTheme.colorScheme.surface),
                factory = { context ->
                    FrameLayout(context).apply {
                        val mainContainer = FragmentContainerView(context).apply { id = R.id.main }
                        val listContainer = FragmentContainerView(context).apply { id = R.id.template_list }
                        val detailsContainer = FragmentContainerView(context).apply { id = R.id.template_details }

                        setTag(R.id.main, mainContainer)
                        setTag(R.id.template_list, listContainer)
                        setTag(R.id.template_details, detailsContainer)

                        addView(mainContainer, FrameLayout.LayoutParams(-1, -1))
                        addView(listContainer, FrameLayout.LayoutParams(-1, -1))
                        addView(detailsContainer, FrameLayout.LayoutParams(-1, -1))

                        supportFragmentManager.beginTransaction()
                            .replace(R.id.main, MainFragment())
                            .replace(R.id.template_list, TemplateListFragment())
                            .replace(R.id.template_details, TemplateDetailsFragment())
                            .commitNowAllowingStateLoss()
                    }
                },
                update = { view ->
                    val mainContainer = view.getTag(R.id.main) as View
                    val listContainer = view.getTag(R.id.template_list) as View
                    val detailsContainer = view.getTag(R.id.template_details) as View

                    mainContainer.isVisible = currentScreen == MainViewModel.SCREEN_MAIN
                    listContainer.isVisible = currentScreen == MainViewModel.SCREEN_TEMPLATE_LIST
                    detailsContainer.isVisible = currentScreen == MainViewModel.SCREEN_TEMPLATE_DETAILS
                }
            )
        }
    }

    override fun onApplySystemBarInsets(insets: Insets) {
        // Handled by Compose
    }

    private suspend fun tryOpenLastProject() {
        if (!GeneralPreferences.autoOpenProjects) return

        val openedProject = GeneralPreferences.lastOpenedProject
        if (openedProject == GeneralPreferences.NO_OPENED_PROJECT || openedProject.isEmpty()) return

        val project = File(openedProject)
        if (!project.exists()) {
            withContext(Dispatchers.Main) { flashInfo(R.string.msg_opened_project_does_not_exist) }
            return
        }

        withContext(Dispatchers.Main) {
            if (GeneralPreferences.confirmProjectOpen) {
                askProjectOpenPermission(project)
            } else {
                openProject(project)
            }
        }
    }

    private fun askProjectOpenPermission(root: File) {
        DialogUtils.newMaterialDialogBuilder(this)
            .setTitle(R.string.title_confirm_open_project)
            .setMessage(getString(R.string.msg_confirm_open_project, root.absolutePath))
            .setCancelable(false)
            .setPositiveButton(R.string.yes) { _, _ -> openProject(root) }
            .setNegativeButton(R.string.no, null)
            .show()
    }

    fun openProject(root: File) {
        IProjectManager.getInstance().openProject(root)
        val intent = Intent(this, EditorActivityKt::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
        startActivity(intent)
    }

    override fun onDestroy() {
        ITemplateProvider.getInstance().release()
        super.onDestroy()
    }
}
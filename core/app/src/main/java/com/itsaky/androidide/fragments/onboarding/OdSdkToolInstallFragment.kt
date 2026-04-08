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

package com.itsaky.androidide.fragments.onboarding

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Bundle
import android.provider.Settings
import android.text.Html
import android.view.ViewGroup
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.getSystemService
import androidx.fragment.app.viewModels
import com.github.appintro.SlidePolicy
import com.itsaky.androidide.R
import com.itsaky.androidide.activities.OnboardingActivity
import com.itsaky.androidide.repository.sdkmanager.models.SdkTreeNode
import com.itsaky.androidide.repository.sdkmanager.services.SdkInstallerManager
import com.itsaky.androidide.repository.sdkmanager.tree.SdkTreeView
import com.itsaky.androidide.repository.sdkmanager.viewmodel.SdkManagerViewModel
import com.itsaky.androidide.utils.ConnectionInfo
import com.itsaky.androidide.utils.Environment
import com.itsaky.androidide.utils.executioncommand.TermuxCommand
import com.itsaky.androidide.utils.flashError
import com.itsaky.androidide.utils.getConnectionInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

/**
 * AndroidIDE 引导页全新 SDK、构建工具及环境配置模块
 *
 * @author android_zero
 */
class OdSdkToolInstallFragment : OnboardingFragment(), SlidePolicy {

  private var backgroundDataRestrictionReceiver: BroadcastReceiver? = null
  private var networkStateChangeCallback: ConnectivityManager.NetworkCallback? = null

  private val netStateFlow = MutableStateFlow(ConnectionInfo.UNKNOWN)

  private val sdkManagerViewModel: SdkManagerViewModel by viewModels()

  companion object {
    @JvmStatic
    fun newInstance(context: Context): OdSdkToolInstallFragment {
      return OdSdkToolInstallFragment().also {
        it.arguments =
            Bundle().apply {
              putCharSequence(KEY_ONBOARDING_TITLE, context.getString(R.string.title_install_tools))
              putCharSequence(
                  KEY_ONBOARDING_SUBTITLE,
                  context.getString(R.string.subtitle_install_tools),
              )
              putCharSequence(
                  KEY_ONBOARDING_EXTRA_INFO,
                  Html.fromHtml(
                      context.getString(R.string.msg_install_tools),
                      Html.FROM_HTML_MODE_COMPACT,
                  ),
              )
            }
      }
    }
  }

  // 桩方法：保留以确保 OnboardingActivity 的兼容性编译不报错
  fun isAutoInstall(): Boolean = false

  fun buildIdeSetupArguments(): Array<String> = emptyArray()

  override fun createContentView(parent: ViewGroup, attachToParent: Boolean) {
    val composeView =
        ComposeView(requireContext()).apply {
          setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
          setContent {
            val colorScheme = if (isSystemInDarkTheme()) darkColorScheme() else lightColorScheme()
            MaterialTheme(colorScheme = colorScheme) { SetupConfigurationScreen() }
          }
        }
    parent.addView(
        composeView,
        ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
        ),
    )
    updateConnectionStatus()
  }

  override fun onStart() {
    super.onStart()
    updateConnectionStatus()
    monitorNetworkState()
  }

  override fun onStop() {
    super.onStop()
    removeNetworkMonitors()
  }

  private fun updateConnectionStatus(networkCapabilities: NetworkCapabilities? = null) {
    netStateFlow.value = getConnectionInfo(requireContext(), networkCapabilities)
  }

  private fun monitorNetworkState() {
    val connectivityManager = requireContext().getSystemService<ConnectivityManager>() ?: return
    networkStateChangeCallback?.also { connectivityManager.registerDefaultNetworkCallback(it) }

    networkStateChangeCallback =
        object : ConnectivityManager.NetworkCallback() {
          override fun onCapabilitiesChanged(
              network: Network,
              networkCapabilities: NetworkCapabilities,
          ) {
            updateConnectionStatus(networkCapabilities)
          }

          override fun onLost(network: Network) {
            netStateFlow.value = ConnectionInfo.UNKNOWN
          }
        }

    val networkRequest =
        NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            .addTransportType(NetworkCapabilities.TRANSPORT_ETHERNET)
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .build()
    connectivityManager.registerNetworkCallback(networkRequest, networkStateChangeCallback!!)

    backgroundDataRestrictionReceiver?.also {
      try {
        requireContext().unregisterReceiver(it)
      } catch (err: Throwable) {
        /*ignored*/
      }
    }

    backgroundDataRestrictionReceiver =
        object : BroadcastReceiver() {
          override fun onReceive(context: Context?, intent: Intent?) {
            updateConnectionStatus()
          }
        }

    requireContext()
        .registerReceiver(
            backgroundDataRestrictionReceiver!!,
            IntentFilter(ConnectivityManager.ACTION_RESTRICT_BACKGROUND_CHANGED),
        )
  }

  private fun removeNetworkMonitors() {
    networkStateChangeCallback?.also {
      requireContext().getSystemService<ConnectivityManager>()?.unregisterNetworkCallback(it)
      networkStateChangeCallback = null
    }
    backgroundDataRestrictionReceiver?.also {
      requireContext().unregisterReceiver(it)
      backgroundDataRestrictionReceiver = null
    }
  }

  override val isPolicyRespected: Boolean
    get() = getConnectionInfo(requireContext()).isConnected

  override fun onUserIllegallyRequestedNextPage() {
    requireActivity().flashError(R.string.msg_no_internet)
  }

  @Composable
  private fun SetupConfigurationScreen() {
    val netState by netStateFlow.collectAsState()
    val isLoading by sdkManagerViewModel.isLoading.collectAsState()
    val hasPendingChanges by sdkManagerViewModel.hasPendingChanges.collectAsState()

    var installGit by remember { mutableStateOf(true) }
    var installSsh by remember { mutableStateOf(true) }
    var applyNdkFix by remember { mutableStateOf(true) }
    var applyCmakePatch by remember { mutableStateOf(true) }

    var showActionDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp)) {
      // 网络提示区域
      NetworkWarnings(netState)

      Spacer(modifier = Modifier.height(16.dp))

      //  核心 SDK 树状视图区
      Text(
          text = "Select SDKs & Tools:",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          modifier = Modifier.padding(start = 8.dp, bottom = 8.dp),
      )

      Surface(
          shape = RoundedCornerShape(12.dp),
          color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
          modifier = Modifier.fillMaxWidth().heightIn(min = 160.dp, max = 220.dp),
      ) {
        if (isLoading) {
          Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              CircularProgressIndicator()
              Spacer(modifier = Modifier.height(8.dp))
              Text("Loading SDK definitions...", style = MaterialTheme.typography.bodySmall)
            }
          }
        } else {
          SdkTreeTabs(sdkManagerViewModel)
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // 附加工具区域与修复选项
      Text(
          text = "Additional Configurations:",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          modifier = Modifier.padding(start = 8.dp, bottom = 8.dp),
      )

      Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(checked = installGit, onCheckedChange = { installGit = it })
        Text("Install Git (Version Control)", modifier = Modifier.clickable { installGit = !installGit })
      }

      Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(checked = installSsh, onCheckedChange = { installSsh = it })
        Text("Install OpenSSH (Remote Auth)", modifier = Modifier.clickable { installSsh = !installSsh })
      }

      Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(checked = applyNdkFix, onCheckedChange = { applyNdkFix = it })
        Text("Apply NDK Fixes (symlinks & patches)", modifier = Modifier.clickable { applyNdkFix = !applyNdkFix })
      }

      Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(checked = applyCmakePatch, onCheckedChange = { applyCmakePatch = it })
        Text("Apply CMake Patches", modifier = Modifier.clickable { applyCmakePatch = !applyCmakePatch })
      }

      Spacer(modifier = Modifier.height(24.dp))

      // 底部执行按钮
      Button(
          onClick = { showActionDialog = true },
          enabled = hasPendingChanges || installGit || installSsh,
          modifier = Modifier.fillMaxWidth().height(48.dp),
      ) {
        Text("Start Environment Setup")
      }
    }

    if (showActionDialog) {
      val (toInstall, toDelete) = sdkManagerViewModel.getPendingTasks()
      ActionConfirmAndRunDialog(
          toInstall = toInstall,
          toDelete = toDelete,
          installGit = installGit,
          installSsh = installSsh,
          applyNdkFix = applyNdkFix,
          applyCmakePatch = applyCmakePatch,
          onDismiss = {
            showActionDialog = false
            sdkManagerViewModel.loadData()
          },
          onSuccess = {
            // 直接触发父级 Activity 的安装完成回调进入主界面流程
            (requireActivity() as? OnboardingActivity)?.onSetupCompleted()
          },
      )
    }
  }

  @Composable
  private fun NetworkWarnings(netState: ConnectionInfo) {
    val context = LocalContext.current
    if (!netState.isConnected || netState === ConnectionInfo.UNKNOWN) {
      ErrorChip(
          text =
              "${stringResource(R.string.msg_no_internet)} ${stringResource(R.string.action_open_settings)}",
          isError = true,
          onClick = { context.startActivity(Intent(Settings.ACTION_WIFI_SETTINGS)) },
      )
    }
    if (netState.isCellularTransport) {
      ErrorChip(stringResource(R.string.msg_connected_to_cellular), isError = false)
    }
    if (netState.isMeteredConnection && !netState.isCellularTransport) {
      ErrorChip(stringResource(R.string.msg_connected_to_metered_connection), isError = false)
    }
    if (netState.isBackgroundDataRestricted) {
      ErrorChip(stringResource(R.string.msg_disable_background_data_restriction), isError = false)
    }
  }

  @Composable
  private fun ErrorChip(text: String, isError: Boolean = true, onClick: (() -> Unit)? = null) {
    val color = if (isError) Color(0xFFF44336) else Color(0xFFFF9800)
    Surface(
        modifier =
            Modifier.padding(vertical = 4.dp).fillMaxWidth().clickable(enabled = onClick != null) {
              onClick?.invoke()
            },
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, color),
        color = Color.Transparent,
    ) {
      Row(
          modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
          verticalAlignment = Alignment.CenterVertically,
      ) {
        Icon(
            Icons.Default.Info,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(18.dp),
        )
        Spacer(Modifier.width(8.dp))
        Text(text, color = MaterialTheme.colorScheme.onSurface, fontSize = 12.sp)
      }
    }
  }

  @Composable
  private fun SdkTreeTabs(viewModel: SdkManagerViewModel) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    Column {
      TabRow(selectedTabIndex = selectedTabIndex, modifier = Modifier.fillMaxWidth()) {
        Tab(
            selected = selectedTabIndex == 0,
            onClick = { selectedTabIndex = 0 },
            text = { Text("Platforms", fontSize = 12.sp) },
        )
        Tab(
            selected = selectedTabIndex == 1,
            onClick = { selectedTabIndex = 1 },
            text = { Text("Tools", fontSize = 12.sp) },
        )
      }
      Box(modifier = Modifier.fillMaxSize().padding(top = 8.dp)) {
        if (selectedTabIndex == 0) {
          val nodes by viewModel.platformsTree.collectAsState()
          SdkTreeViewSub(nodes, true, viewModel)
        } else {
          val nodes by viewModel.toolsTree.collectAsState()
          SdkTreeViewSub(nodes, false, viewModel)
        }
      }
    }
  }

  @Composable
  private fun SdkTreeViewSub(
      nodes: List<SdkTreeNode>,
      isPlatformsTab: Boolean,
      viewModel: SdkManagerViewModel,
  ) {
    AndroidView(
        factory = { context ->
          SdkTreeView(context).apply {
            isNestedScrollingEnabled = false // 防止与外层 ScrollView 冲突
            bindData(nodes) { clickedNode ->
              viewModel.toggleCheck(clickedNode, isPlatformsTab)
              refreshViews()
            }
          }
        },
        update = { view ->
          view.bindData(nodes) { clickedNode ->
            viewModel.toggleCheck(clickedNode, isPlatformsTab)
            view.refreshViews()
          }
        },
        modifier = Modifier.fillMaxSize(),
    )
  }

  @Composable
  private fun ActionConfirmAndRunDialog(
      toInstall: List<SdkTreeNode>,
      toDelete: List<SdkTreeNode>,
      installGit: Boolean,
      installSsh: Boolean,
      applyNdkFix: Boolean,
      applyCmakePatch: Boolean,
      onDismiss: () -> Unit,
      onSuccess: () -> Unit,
  ) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var isRunning by remember { mutableStateOf(false) }
    var isFinished by remember { mutableStateOf(false) }
    var currentProgress by remember { mutableStateOf(0f) }
    var currentTaskName by remember { mutableStateOf("") }
    val consoleLogs = remember { mutableStateListOf<String>() }

    fun addLog(msg: String) {
      consoleLogs.add(msg)
    }

    AlertDialog(
        onDismissRequest = { if (!isRunning) onDismiss() },
        properties =
            DialogProperties(dismissOnBackPress = !isRunning, dismissOnClickOutside = !isRunning),
        title = { Text(if (isFinished) "Setup Completed" else "Confirm Installation") },
        text = {
          Column(modifier = Modifier.fillMaxWidth()) {
            if (!isRunning && !isFinished) {
              Text("Components to install/update:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
              toInstall.forEach { Text("- ${it.name}", fontSize = 13.sp) }
              if (installGit) Text("- Git Version Control", fontSize = 13.sp)
              if (installSsh) Text("- OpenSSH Remote Auth", fontSize = 13.sp)
              if (toInstall.isEmpty() && !installGit && !installSsh) {
                Text("  (None)", color = Color.Gray, fontSize = 13.sp)
              }

              if (toDelete.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text("Components to remove:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                toDelete.forEach {
                  Text("- ${it.name}", color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
                }
              }
              
              val installingNdk = toInstall.any { it.componentType == "ndk" }
              val installingCmake = toInstall.any { it.componentType == "cmake" }
              
              if (installingNdk || installingCmake) {
                Spacer(modifier = Modifier.height(16.dp))
                Divider()
                Spacer(modifier = Modifier.height(8.dp))
                Text("Additional Configurations:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                if (installingNdk) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = applyNdkFix, onCheckedChange = { /* Disabled override */ }, enabled = false)
                    Text("Apply NDK Fixes (symlinks & patches)", fontSize = 13.sp)
                  }
                }
                if (installingCmake) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = applyCmakePatch, onCheckedChange = { /* Disabled override */ }, enabled = false)
                    Text("Apply CMake Patches", fontSize = 13.sp)
                  }
                }
              }
            }

            if (isRunning || isFinished) {
              Text(text = "Current: $currentTaskName", style = MaterialTheme.typography.labelMedium)
              LinearProgressIndicator(
                  progress = { currentProgress },
                  modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
              )

              val listState = rememberLazyListState()
              LaunchedEffect(consoleLogs.size) {
                if (consoleLogs.isNotEmpty()) listState.animateScrollToItem(consoleLogs.lastIndex)
              }
              Box(
                  modifier =
                      Modifier.fillMaxWidth()
                          .height(200.dp)
                          .background(Color(0xFF1E1E1E), shape = MaterialTheme.shapes.small)
                          .padding(8.dp)
              ) {
                LazyColumn(state = listState) {
                  items(consoleLogs) { msg ->
                    val textColor =
                        when {
                          msg.startsWith("ERR") || msg.startsWith("WARN") -> Color(0xFFFF5252)
                          msg.startsWith(">>") -> Color(0xFF64B5F6)
                          else -> Color(0xFFA5D6A7)
                        }
                    Text(
                        text = msg,
                        color = textColor,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 14.sp,
                    )
                  }
                }
              }
            }
          }
        },
        confirmButton = {
          if (!isFinished) {
            Button(
                onClick = {
                  isRunning = true
                  coroutineScope.launch(Dispatchers.IO) {
                    for (node in toDelete) {
                      currentTaskName = "Removing ${node.name}"
                      currentProgress = 0f
                      SdkInstallerManager.deletePackage(node, ::addLog)
                      currentProgress = 1f
                    }

                    for (node in toInstall) {
                      currentTaskName = "Installing ${node.name}"
                      currentProgress = 0f
                      val success =
                          SdkInstallerManager.downloadAndInstall(
                              context = context,
                              node = node,
                              applyNdkFix = applyNdkFix,
                              applyCmakePatch = applyCmakePatch,
                              onProgress = { currentProgress = it },
                              onLog = ::addLog,
                          )
                      if (!success) {
                        addLog("ERROR: Failed to install ${node.name}. Continuing next task.")
                      }
                    }

                    // 安装 Git & OpenSSH
                    if (installGit || installSsh) {
                      currentTaskName = "Configuring APT environment..."
                      currentProgress = -1f // indeterminate
                      addLog(">> Updating APT repositories...")

                      TermuxCommand.run(context) {
                            label("APT Update")
                            executable(Environment.BASH_SHELL.absolutePath)
                            args("-c", "apt update")
                          }
                          .also {
                            if (it.stdout.isNotBlank()) addLog(it.stdout)
                            if (it.stderr.isNotBlank()) addLog("WARN/ERR: ${it.stderr}")
                          }

                      if (installGit) {
                        currentTaskName = "Installing Git..."
                        addLog(">> Installing Git...")
                        TermuxCommand.run(context) {
                              label("APT Install Git")
                              executable(Environment.BASH_SHELL.absolutePath)
                              args("-c", "apt install git -y")
                            }
                            .also {
                              if (it.stdout.isNotBlank()) addLog(it.stdout)
                              if (it.stderr.isNotBlank()) addLog("WARN/ERR: ${it.stderr}")
                            }
                      }

                      if (installSsh) {
                        currentTaskName = "Installing OpenSSH..."
                        addLog(">> Installing OpenSSH...")
                        TermuxCommand.run(context) {
                              label("APT Install SSH")
                              executable(Environment.BASH_SHELL.absolutePath)
                              args("-c", "apt install openssh -y")
                            }
                            .also {
                              if (it.stdout.isNotBlank()) addLog(it.stdout)
                              if (it.stderr.isNotBlank()) addLog("WARN/ERR: ${it.stderr}")
                            }
                      }
                    }

                    isFinished = true
                    isRunning = false
                    currentTaskName = "All tasks completed."
                    currentProgress = 1f
                  }
                },
                enabled = !isRunning,
            ) {
              Text("Execute")
            }
          } else {
            Button(onClick = onSuccess) { Text("Finish & Launch") }
          }
        },
        dismissButton = {
          if (!isRunning && !isFinished) {
            TextButton(onClick = onDismiss) { Text("Cancel") }
          }
        },
    )
  }
}
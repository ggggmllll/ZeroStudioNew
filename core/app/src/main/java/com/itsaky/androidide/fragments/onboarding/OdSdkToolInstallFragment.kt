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

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.getSystemService
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.github.appintro.SlidePolicy
import com.google.gson.Gson
import com.itsaky.androidide.R
import com.itsaky.androidide.activities.OnboardingActivity
import com.itsaky.androidide.app.configuration.IDEBuildConfigProvider
import com.itsaky.androidide.repository.sdkmanager.models.SdkManifest
import com.itsaky.androidide.repository.sdkmanager.models.SdkTreeNode
import com.itsaky.androidide.repository.sdkmanager.services.SdkInstallerManager
import com.itsaky.androidide.repository.sdkmanager.tree.SdkTreeView
import com.itsaky.androidide.utils.ConnectionInfo
import com.itsaky.androidide.utils.Environment
import com.itsaky.androidide.utils.executioncommand.TermuxCommand
import com.itsaky.androidide.utils.flashError
import com.itsaky.androidide.utils.getConnectionInfo
import com.termux.app.TermuxInstaller
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * 全新精简版 SDK 与环境安装 Fragment。 支持在线下载解压配置，以及本地离线包 (sdkresources.tar.gz) 自动布署。
 *
 * @author android_zero
 */
class OdSdkToolInstallFragment : Fragment(), SlidePolicy {

  private var backgroundDataRestrictionReceiver: BroadcastReceiver? = null
  private var networkStateChangeCallback: ConnectivityManager.NetworkCallback? = null
  private val netStateFlow = MutableStateFlow(ConnectionInfo.UNKNOWN)

  private val setupViewModel: OdSdkSetupViewModel by viewModels()

  companion object {
    @JvmStatic
    fun newInstance(context: Context): OdSdkToolInstallFragment {
      return OdSdkToolInstallFragment()
    }
  }

  fun isAutoInstall(): Boolean = false

  fun buildIdeSetupArguments(): Array<String> = emptyArray()

  override fun onCreateView(
      inflater: LayoutInflater,
      container: ViewGroup?,
      savedInstanceState: Bundle?,
  ): View {
    return ComposeView(requireContext()).apply {
      setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
      setContent {
        val colorScheme = if (isSystemInDarkTheme()) darkColorScheme() else lightColorScheme()
        MaterialTheme(colorScheme = colorScheme) { SetupConfigurationScreen() }
      }
    }
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
    val isLoading by setupViewModel.isLoading.collectAsState()
    val treeNodes by setupViewModel.treeNodes.collectAsState()
    val hasPendingChanges by setupViewModel.hasPendingChanges.collectAsState()

    var installGit by remember { mutableStateOf(true) }
    var installSsh by remember { mutableStateOf(true) }
    var applyNdkFix by remember { mutableStateOf(true) }
    var applyCmakePatch by remember { mutableStateOf(true) }
    var installOffline by remember { mutableStateOf(false) }

    var useGithubMirror by remember { mutableStateOf(false) }
    var githubMirrorUrl by remember { mutableStateOf("https://gh.llkk.cc/") }

    var showActionDialog by remember { mutableStateOf(false) }
    var showOfflineDialog by remember { mutableStateOf(false) }
    var selectedJdk by remember { mutableStateOf("17") }
    var jdkExpanded by remember { mutableStateOf(false) }

    val currentAbi = IDEBuildConfigProvider.getInstance().cpuAbiName

    fun getValidMirror(): String {
      if (!useGithubMirror) return ""
      val t = githubMirrorUrl.trim()
      if (t.isBlank()) return ""
      if (!t.startsWith("http://") && !t.startsWith("https://")) return ""
      if (!t.endsWith("/")) return ""
      return t
    }

    // 主体容器：使用 fillMaxSize 撑满全屏
    // top = 40.dp 留出状态栏空间；bottom = 98.dp 避开底部的 AppIntro 引导栏
    Column(
        modifier =
            Modifier.fillMaxSize().padding(start = 16.dp, end = 16.dp, top = 40.dp, bottom = 98.dp)
    ) {

      // 头部区域
      Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
            text = "SDK Installation and Configuration",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )
      }

      Text(
          text =
              "The development tools must be installed for the IDE to function properly. Please select the required components and then perform the installation at the end.",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.padding(top = 4.dp, bottom = 12.dp),
      )

      NetworkWarnings(netState)

      Spacer(modifier = Modifier.height(8.dp))

      Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
            text = "Select SDKs & Tools:",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
        )
        Surface(
            color = MaterialTheme.colorScheme.secondaryContainer,
            shape = RoundedCornerShape(8.dp),
        ) {
          Text(
              text = "ABI: $currentAbi",
              fontSize = 10.sp,
              color = MaterialTheme.colorScheme.onSecondaryContainer,
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
          )
        }
      }

      Surface(
          shape = RoundedCornerShape(12.dp),
          color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
          modifier =
              Modifier.fillMaxWidth()
                  .weight(1f) // 自动拉伸占满高度
                  .padding(vertical = 8.dp),
      ) {
        if (isLoading) {
          Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(modifier = Modifier.size(32.dp))
          }
        } else {
          AndroidView(
              factory = { context ->
                SdkTreeView(context).apply { isNestedScrollingEnabled = false }
              },
              update = { view ->
                if (view.tag != treeNodes && treeNodes.isNotEmpty()) {
                  val listener: (SdkTreeNode) -> Unit = { clickedNode ->
                    if (
                        clickedNode.componentType != "android-sdk" &&
                            clickedNode.componentType != "cmdline-tools"
                    ) {
                      val nextState =
                          when (clickedNode.checkedState) {
                            ToggleableState.On -> ToggleableState.Off
                            ToggleableState.Off,
                            ToggleableState.Indeterminate -> ToggleableState.On
                          }
                      clickedNode.updateChildrenState(nextState)
                      clickedNode.updateParentState()
                      view.refreshViews()

                      // 触发外部Compose底栏的按钮状态更新
                      setupViewModel.triggerPendingChangesCheck()
                    }
                  }

                  // 首次注入，记录标志位
                  view.bindData(treeNodes, listener)
                  view.tag = treeNodes
                }
              },
              modifier = Modifier.fillMaxSize(),
          )
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      // 底部配置区域
      Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
        Text(
            text = "Additional Configurations:",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
        )

        // JDK 选择
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        ) {
          Text("Java Development Kit: ", fontSize = 12.sp)
          Box {
            OutlinedButton(
                onClick = { jdkExpanded = true },
                modifier = Modifier.height(30.dp),
                contentPadding = PaddingValues(horizontal = 8.dp),
            ) {
              Text("OpenJDK $selectedJdk", fontSize = 11.sp)
            }
            DropdownMenu(expanded = jdkExpanded, onDismissRequest = { jdkExpanded = false }) {
              DropdownMenuItem(
                  text = { Text("OpenJDK 17 (Recommended)", fontSize = 12.sp) },
                  onClick = {
                    selectedJdk = "17"
                    jdkExpanded = false
                  },
              )
              DropdownMenuItem(
                  text = { Text("OpenJDK 21 (Experimental)", fontSize = 12.sp) },
                  onClick = {
                    selectedJdk = "21"
                    jdkExpanded = false
                  },
              )
            }
          }
        }

        // 基础修复与安装开关
        Column {
          Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.height(36.dp)) {
            Checkbox(
                checked = installGit,
                onCheckedChange = { installGit = it },
                modifier = Modifier.scale(0.8f),
            )
            Text(
                "Install Git (Version Control)",
                fontSize = 11.sp,
                modifier = Modifier.clickable { installGit = !installGit },
            )
          }
          Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.height(36.dp)) {
            Checkbox(
                checked = installSsh,
                onCheckedChange = { installSsh = it },
                modifier = Modifier.scale(0.8f),
            )
            Text(
                "Install OpenSSH (Remote Auth)",
                fontSize = 11.sp,
                modifier = Modifier.clickable { installSsh = !installSsh },
            )
          }
          Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.height(36.dp)) {
            Checkbox(
                checked = applyNdkFix,
                onCheckedChange = { applyNdkFix = it },
                modifier = Modifier.scale(0.8f),
            )
            Text(
                "Apply NDK Fixes (symlinks & patches)",
                fontSize = 11.sp,
                modifier = Modifier.clickable { applyNdkFix = !applyNdkFix },
            )
          }
          Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.height(36.dp)) {
            Checkbox(
                checked = applyCmakePatch,
                onCheckedChange = { applyCmakePatch = it },
                modifier = Modifier.scale(0.8f),
            )
            Text(
                "Apply CMake Patches",
                fontSize = 11.sp,
                modifier = Modifier.clickable { applyCmakePatch = !applyCmakePatch },
            )
          }

          // GitHub 镜像加速选项
          Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.height(36.dp)) {
            Checkbox(
                checked = useGithubMirror,
                onCheckedChange = { useGithubMirror = it },
                modifier = Modifier.scale(0.8f),
            )
            Text(
                "Use Github Mirror (Accelerate download)",
                fontSize = 11.sp,
                modifier = Modifier.clickable { useGithubMirror = !useGithubMirror },
            )
          }
          Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.height(36.dp)) {
            Checkbox(
                checked = installOffline,
                onCheckedChange = { installOffline = it },
                modifier = Modifier.scale(0.8f),
            )
            Text(
                "Install Offline SDK & Tools (Local tar.gz)",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable { installOffline = !installOffline },
            )
          }
        }

        if (useGithubMirror && !installOffline) {
          Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.padding(start = 12.dp, top = 4.dp),
          ) {
            OutlinedTextField(
                value = githubMirrorUrl,
                onValueChange = { githubMirrorUrl = it },
                modifier = Modifier.weight(1f).height(46.dp),
                textStyle = LocalTextStyle.current.copy(fontSize = 11.sp),
                singleLine = true,
                placeholder = { Text("https://gh.llkk.cc/", fontSize = 11.sp) },
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = { setupViewModel.loadData(getValidMirror()) },
                modifier = Modifier.height(38.dp),
                contentPadding = PaddingValues(horizontal = 8.dp),
            ) {
              Icon(
                  Icons.Default.Refresh,
                  contentDescription = "Reload",
                  modifier = Modifier.size(16.dp),
              )
              Spacer(Modifier.width(4.dp))
              Text("Reload", fontSize = 11.sp)
            }
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 底部执行按钮
        Button(
            onClick = {
              val activity = requireActivity()
              TermuxInstaller.setupBootstrapIfNeeded(activity) {
                activity.runOnUiThread {
                  if (installOffline) {
                    showOfflineDialog = true
                  } else {
                    showActionDialog = true
                  }
                }
              }
            },
            enabled = installOffline || hasPendingChanges || installGit || installSsh,
            modifier = Modifier.fillMaxWidth().height(46.dp),
        ) {
          Text(
              if (installOffline) "Start Offline Installation" else "Start Environment Setup",
              fontSize = 13.sp,
          )
        }
      }
    }

    if (showActionDialog) {
      val toInstall = setupViewModel.getInstallTasks()
      ActionConfirmAndRunDialog(
          toInstall = toInstall,
          installGit = installGit,
          installSsh = installSsh,
          applyNdkFix = applyNdkFix,
          applyCmakePatch = applyCmakePatch,
          jdkVersion = selectedJdk,
          githubMirror = getValidMirror(),
          onDismiss = {
            showActionDialog = false
            setupViewModel.loadData(getValidMirror())
          },
          onSuccess = { (requireActivity() as? OnboardingActivity)?.onSetupCompleted() },
      )
    }

    if (showOfflineDialog) {
      OfflineConfirmAndRunDialog(
          onDismiss = { showOfflineDialog = false },
          onSuccess = { (requireActivity() as? OnboardingActivity)?.onSetupCompleted() },
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
            Modifier.padding(top = 4.dp).fillMaxWidth().clickable(enabled = onClick != null) {
              onClick?.invoke()
            },
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, color),
        color = Color.Transparent,
    ) {
      Row(
          modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
          verticalAlignment = Alignment.CenterVertically,
      ) {
        Icon(
            Icons.Default.Info,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(16.dp),
        )
        Spacer(Modifier.width(6.dp))
        Text(text, color = MaterialTheme.colorScheme.onSurface, fontSize = 11.sp)
      }
    }
  }

  @Composable
  private fun ActionConfirmAndRunDialog(
      toInstall: List<SdkTreeNode>,
      installGit: Boolean,
      installSsh: Boolean,
      applyNdkFix: Boolean,
      applyCmakePatch: Boolean,
      jdkVersion: String,
      githubMirror: String,
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
        title = {
          Text(if (isFinished) "Setup Completed" else "Confirm Installation", fontSize = 16.sp)
        },
        text = {
          Column(modifier = Modifier.fillMaxWidth()) {
            if (!isRunning && !isFinished) {
              Text("Components to install/update:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
              toInstall.forEach { Text("- ${it.name}", fontSize = 12.sp) }
              Text("- OpenJDK $jdkVersion", fontSize = 12.sp)
              if (installGit) Text("- Git Version Control", fontSize = 12.sp)
              if (installSsh) Text("- OpenSSH Remote Auth", fontSize = 12.sp)

              val installingNdk = toInstall.any { it.componentType == "ndk" }
              val installingCmake = toInstall.any { it.componentType == "cmake" }

              if (installingNdk || installingCmake) {
                Spacer(modifier = Modifier.height(12.dp))
                Divider()
                Spacer(modifier = Modifier.height(6.dp))
                Text("Additional Configurations:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                if (installingNdk) {
                  Text(
                      "• Apply NDK Fixes (symlinks & patches)",
                      fontSize = 11.sp,
                      color = if (applyNdkFix) MaterialTheme.colorScheme.onSurface else Color.Gray,
                  )
                }
                if (installingCmake) {
                  Text(
                      "• Apply CMake Patches",
                      fontSize = 11.sp,
                      color =
                          if (applyCmakePatch) MaterialTheme.colorScheme.onSurface else Color.Gray,
                  )
                }
              }
              if (githubMirror.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    "• Active Github Mirror: $githubMirror",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.primary,
                )
              }
            }

            if (isRunning || isFinished) {
              Text(text = "Current: $currentTaskName", style = MaterialTheme.typography.labelSmall)
              LinearProgressIndicator(
                  progress = { currentProgress },
                  modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
              )

              val listState = rememberLazyListState()
              LaunchedEffect(consoleLogs.size) {
                if (consoleLogs.isNotEmpty()) listState.animateScrollToItem(consoleLogs.lastIndex)
              }
              Box(
                  modifier =
                      Modifier.fillMaxWidth()
                          .height(180.dp)
                          .background(Color(0xFF1E1E1E), shape = MaterialTheme.shapes.small)
                          .padding(6.dp)
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
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 12.sp,
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

                    // 系统依赖与包管理器更新
                    currentTaskName = "Configuring package environment..."
                    currentProgress = -1f
                    addLog(">> Updating pkg repositories...")
                    TermuxCommand.run(context) {
                          executable("sh")
                          args("-c", "pkg update -y && pkg upgrade -y")
                        }
                        .also { if (it.stdout.isNotBlank()) addLog(it.stdout) }

                    // 安装基础包和解压工具
                    addLog(">> Installing required base packages...")
                    TermuxCommand.run(context) {
                          executable("sh")
                          args(
                              "-c",
                              "pkg install -y bash curl wget jq tar unzip p7zip xz-utils patch sed grep coreutils findutils diffutils",
                          )
                        }
                        .also { if (it.stdout.isNotBlank()) addLog(it.stdout) }

                    currentTaskName = "Checking extraction tools..."
                    addLog(">> Verifying unzip/7z/tar availability...")
                    TermuxCommand.run(context) {
                          executable("sh")
                          args(
                              "-c",
                              "command -v unzip && command -v 7z && command -v tar && command -v xz",
                          )
                        }
                        .also {
                          if (it.stdout.isNotBlank()) addLog(it.stdout)
                          if (!it.isSuccess && it.stderr.isNotBlank())
                              addLog("WARN/ERR tools check: ${it.stderr}")
                        }

                    // Git 和 OpenSSH
                    if (installGit) {
                      currentTaskName = "Installing Git..."
                      addLog(">> Installing Git...")
                      TermuxCommand.run(context) {
                        executable("sh")
                        args("-c", "pkg install -y git")
                      }
                    }
                    if (installSsh) {
                      currentTaskName = "Installing OpenSSH..."
                      addLog(">> Installing OpenSSH...")
                      TermuxCommand.run(context) {
                        executable("sh")
                        args("-c", "pkg install -y openssh")
                      }
                    }

                    // 安装 JDK
                    currentTaskName = "Installing OpenJDK $jdkVersion..."
                    addLog(">> Installing package: 'openjdk-$jdkVersion'")
                    TermuxCommand.run(context) {
                          executable("sh")
                          args("-c", "pkg install -y openjdk-$jdkVersion")
                        }
                        .also { addLog(">> JDK $jdkVersion has been installed.") }

                    addLog(">> Updating ide-environment.properties...")
                    val jdkDir = "${Environment.PREFIX.absolutePath}/opt/openjdk"
                    val propsDir = File(Environment.PREFIX, "etc")
                    if (!propsDir.exists()) propsDir.mkdirs()
                    val propsFile = File(propsDir, "ide-environment.properties")
                    try {
                      propsFile.writeText("JAVA_HOME=$jdkDir\n")
                      addLog(">> JAVA_HOME=$jdkDir")
                      addLog(">> Properties file updated successfully!")
                    } catch (e: Exception) {
                      addLog("WARN: Failed to write ide-environment.properties: ${e.message}")
                    }

                    // 执行 SDK/NDK/CMake 安装
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

                    isFinished = true
                    isRunning = false
                    currentTaskName = "All tasks completed. Environment is ready!"
                    currentProgress = 1f
                  }
                },
                enabled = !isRunning,
            ) {
              Text("Execute", fontSize = 13.sp)
            }
          } else {
            Button(onClick = onSuccess) { Text("Finish & Launch", fontSize = 13.sp) }
          }
        },
        dismissButton = {
          if (!isRunning && !isFinished) {
            TextButton(onClick = onDismiss) { Text("Cancel", fontSize = 13.sp) }
          }
        },
    )
  }

  @Composable
  fun OfflineConfirmAndRunDialog(onDismiss: () -> Unit, onSuccess: () -> Unit) {
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

    var offlineUriStr by remember { mutableStateOf("") }

    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
          if (uri != null) {
            offlineUriStr = uri.toString()
          }
        }

    AlertDialog(
        onDismissRequest = { if (!isRunning) onDismiss() },
        properties =
            DialogProperties(dismissOnBackPress = !isRunning, dismissOnClickOutside = !isRunning),
        title = {
          Text(
              if (isFinished) "Offline Setup Completed" else "Offline Installation",
              fontSize = 16.sp,
          )
        },
        text = {
          Column(modifier = Modifier.fillMaxWidth()) {
            if (!isRunning && !isFinished) {
              Text(
                  "Please select the offline resources package (sdkresources.tar.gz):",
                  fontSize = 13.sp,
              )
              Spacer(modifier = Modifier.height(8.dp))

              Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = offlineUriStr,
                    onValueChange = { offlineUriStr = it },
                    modifier = Modifier.weight(1f).height(50.dp),
                    textStyle = LocalTextStyle.current.copy(fontSize = 11.sp),
                    singleLine = true,
                    placeholder = { Text("content://...", fontSize = 11.sp) },
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = { launcher.launch("application/*") }) { Text("Select") }
              }
            }

            if (isRunning || isFinished) {
              Text(text = "Current: $currentTaskName", style = MaterialTheme.typography.labelSmall)
              LinearProgressIndicator(
                  progress = { currentProgress },
                  modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
              )

              val listState = rememberLazyListState()
              LaunchedEffect(consoleLogs.size) {
                if (consoleLogs.isNotEmpty()) listState.animateScrollToItem(consoleLogs.lastIndex)
              }
              Box(
                  modifier =
                      Modifier.fillMaxWidth()
                          .height(180.dp)
                          .background(Color(0xFF1E1E1E), shape = MaterialTheme.shapes.small)
                          .padding(6.dp)
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
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 12.sp,
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
                  if (offlineUriStr.isBlank()) {
                    addLog("ERR: No file selected!")
                    return@Button
                  }
                  isRunning = true
                  coroutineScope.launch(Dispatchers.IO) {
                    currentTaskName = "Preparing offline package..."
                    currentProgress = -1f
                    try {
                      val homeDir = Environment.HOME
                      val targetArchive = File(homeDir, "sdkresources.tar.gz")

                      addLog(">> Copying selected file to HOME...")
                      val uri = Uri.parse(offlineUriStr)
                      context.contentResolver.openInputStream(uri)?.use { input ->
                        targetArchive.outputStream().use { output -> input.copyTo(output) }
                      }

                      addLog(">> File copied. Installing tar...")
                      TermuxCommand.run(context) {
                        executable("sh")
                        args("-c", "pkg install tar dpkg -y")
                      }

                      val scriptFile = File(Environment.TMP_DIR, "offline_install.sh")
                      val script =
                          """
                                        #!/system/bin/sh
                                        set -e
                                        HOME_DIR="${homeDir.absolutePath}"
                                        CACHE_DIR="${'$'}HOME_DIR/Installcache"
                                        PKG_DIR="${'$'}CACHE_DIR/packages"

                                        cd "${'$'}HOME_DIR"
                                        echo ">> Extracting sdkresources.tar.gz to Installcache..."
                                        mkdir -p "${'$'}CACHE_DIR"
                                        tar -xzf sdkresources.tar.gz -C "${'$'}CACHE_DIR"

                                        echo ">> Extracting android-sdk.tar.gz to HOME..."
                                        cd "${'$'}CACHE_DIR"
                                        if [ -f "android-sdk.tar.gz" ]; then
                                            tar -xzf android-sdk.tar.gz -C "${'$'}HOME_DIR"
                                        else
                                            echo "WARN: android-sdk.tar.gz not found!"
                                        fi

                                        echo ">> Extracting packages.tar.gz..."
                                        mkdir -p "${'$'}PKG_DIR"
                                        if [ -f "packages.tar.gz" ]; then
                                            tar -xzf packages.tar.gz -C "${'$'}PKG_DIR"
                                            echo ">> Installing deb packages..."
                                            cd "${'$'}PKG_DIR"
                                            for deb in *.deb; do
                                                if [ -f "${'$'}deb" ]; then
                                                    dpkg -i "${'$'}deb" || apt install -y "${'$'}deb" || true
                                                fi
                                            done
                                        else
                                            echo "WARN: packages.tar.gz not found!"
                                        fi

                                        echo ">> Cleaning up temporary files..."
                                        cd "${'$'}HOME_DIR"
                                        rm -rf "${'$'}CACHE_DIR"
                                        rm -f sdkresources.tar.gz

                                        echo ">> Offline installation completed."
                                    """
                              .trimIndent()

                      scriptFile.writeText(script)
                      scriptFile.setExecutable(true)

                      currentTaskName = "Executing offline installation..."
                      val cmdResult =
                          TermuxCommand.run(context) {
                            label("Offline_Installer")
                            executable("sh")
                            args(scriptFile.absolutePath)
                          }

                      if (cmdResult.stdout.isNotBlank()) addLog(cmdResult.stdout)
                      if (cmdResult.stderr.isNotBlank()) addLog("ERR: ${cmdResult.stderr}")

                      scriptFile.delete()
                    } catch (e: Exception) {
                      addLog("ERR: ${e.message}")
                    }

                    isFinished = true
                    isRunning = false
                    currentTaskName = "All tasks completed. Environment is ready!"
                    currentProgress = 1f
                  }
                },
                enabled = !isRunning,
            ) {
              Text("Execute", fontSize = 13.sp)
            }
          } else {
            Button(onClick = onSuccess) { Text("Finish & Launch", fontSize = 13.sp) }
          }
        },
        dismissButton = {
          if (!isRunning && !isFinished) {
            TextButton(onClick = onDismiss) { Text("Cancel", fontSize = 13.sp) }
          }
        },
    )
  }
}

/** 专门针对引导页的精简版 ViewModel (支持 GitHub 镜像参数) */
class OdSdkSetupViewModel(application: Application) : AndroidViewModel(application) {

  private val _treeNodes = MutableStateFlow<List<SdkTreeNode>>(emptyList())
  val treeNodes: StateFlow<List<SdkTreeNode>> = _treeNodes.asStateFlow()

  private val _isLoading = MutableStateFlow(true)
  val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

  private val _hasPendingChanges = MutableStateFlow(false)
  val hasPendingChanges: StateFlow<Boolean> = _hasPendingChanges.asStateFlow()

  private var currentMirror: String = ""

  private fun normalizeVersion(rawVersion: String): String {
    val noPrefix = rawVersion.trimStart('_', '-')
    return noPrefix.replace("_", ".").trimStart('.')
  }

  private fun compareVersionDesc(a: String, b: String): Int {
    val ap = a.split('.', '-', '_').mapNotNull { it.toIntOrNull() }
    val bp = b.split('.', '-', '_').mapNotNull { it.toIntOrNull() }
    val max = maxOf(ap.size, bp.size)
    for (i in 0 until max) {
      val av = ap.getOrElse(i) { 0 }
      val bv = bp.getOrElse(i) { 0 }
      if (av != bv) return bv.compareTo(av)
    }
    return b.compareTo(a)
  }

  init {
    loadData()
  }

  fun loadData(mirrorUrl: String = currentMirror) {
    currentMirror = mirrorUrl
    viewModelScope.launch {
      _isLoading.value = true
      try {
        val rootNodes = mutableListOf<SdkTreeNode>()
        val manifest = fetchManifest(mirrorUrl)

        if (manifest != null) {
          val arch = getArch()
          val queryArch = if (arch == "armv7l" || arch == "armv8l") "arm" else arch

          fun applyMirror(url: String): String {
            return if (mirrorUrl.isNotEmpty() && url.startsWith("https://github.com"))
                mirrorUrl + url
            else url
          }

          // Android SDK (强制)
          val sdkUrl = manifest.androidSdk
          if (!sdkUrl.isNullOrBlank() && sdkUrl.lowercase() != "x") {
            rootNodes.add(
                SdkTreeNode(
                    name = "Android SDK Platform",
                    revision = "Latest",
                    downloadUrl = applyMirror(sdkUrl),
                    componentType = "android-sdk",
                    checkedState = ToggleableState.On,
                )
            )
          }

          // Cmdline Tools (强制)
          val cmdUrl = manifest.cmdlineTools
          if (!cmdUrl.isNullOrBlank() && cmdUrl.lowercase() != "x") {
            rootNodes.add(
                SdkTreeNode(
                    name = "Command-line Tools",
                    revision = "Latest",
                    downloadUrl = applyMirror(cmdUrl),
                    componentType = "cmdline-tools",
                    checkedState = ToggleableState.On,
                )
            )
          }

          // Build Tools
          manifest.buildTools?.get(queryArch)?.let { map ->
            val group = SdkTreeNode(name = "Build-Tools", isGroup = true, isExpanded = false)
            map.forEach { (k, url) ->
              if (url.isNotBlank() && url.lowercase() != "x") {
                val ver = normalizeVersion(k)
                group.children.add(
                    SdkTreeNode(
                        name = "Build-Tools $ver",
                        revision = ver,
                        downloadUrl = applyMirror(url),
                        componentType = "build-tools",
                        parent = group,
                    )
                )
              }
            }
            group.children.sortWith { a, b -> compareVersionDesc(a.revision, b.revision) }
            // 默认勾选最新
            group.children.firstOrNull()?.let { it.checkedState = ToggleableState.On }
            group.updateParentState()
            if (group.children.isNotEmpty()) rootNodes.add(group)
          }

          // Platform Tools (特定推荐 35.0.2)
          manifest.platformTools?.get(queryArch)?.let { map ->
            val group = SdkTreeNode(name = "Platform-Tools", isGroup = true, isExpanded = false)
            map.forEach { (k, url) ->
              if (url.isNotBlank() && url.lowercase() != "x") {
                val ver = normalizeVersion(k)
                group.children.add(
                    SdkTreeNode(
                        name = "Platform-Tools $ver",
                        revision = ver,
                        downloadUrl = applyMirror(url),
                        componentType = "platform-tools",
                        parent = group,
                    )
                )
              }
            }
            group.children.sortWith { a, b -> compareVersionDesc(a.revision, b.revision) }
            val targetNode =
                group.children.find { it.revision == "35.0.2" } ?: group.children.firstOrNull()
            targetNode?.let { it.checkedState = ToggleableState.On }
            group.updateParentState()
            if (group.children.isNotEmpty()) rootNodes.add(group)
          }

          // NDK
          manifest.androidNdk?.get(queryArch)?.let { map ->
            val group = SdkTreeNode(name = "NDK (Side by side)", isGroup = true, isExpanded = false)
            map.forEach { (k, url) ->
              if (url.isNotBlank() && url.lowercase() != "x") {
                val ver = normalizeVersion(k)
                group.children.add(
                    SdkTreeNode(
                        name = "NDK $ver",
                        revision = ver,
                        downloadUrl = applyMirror(url),
                        componentType = "ndk",
                        parent = group,
                    )
                )
              }
            }
            group.children.sortWith { a, b -> compareVersionDesc(a.revision, b.revision) }
            group.updateParentState()
            if (group.children.isNotEmpty()) rootNodes.add(group)
          }

          // CMake
          manifest.androidCmake?.get(queryArch)?.let { map ->
            val group = SdkTreeNode(name = "CMake", isGroup = true, isExpanded = false)
            map.forEach { (k, url) ->
              if (url.isNotBlank() && url.lowercase() != "x") {
                val ver = normalizeVersion(k)
                group.children.add(
                    SdkTreeNode(
                        name = "CMake $ver",
                        revision = ver,
                        downloadUrl = applyMirror(url),
                        componentType = "cmake",
                        parent = group,
                    )
                )
              }
            }
            group.children.sortWith { a, b -> compareVersionDesc(a.revision, b.revision) }
            group.updateParentState()
            if (group.children.isNotEmpty()) rootNodes.add(group)
          }
        }

        _treeNodes.value = rootNodes
        checkPendingChanges()
      } catch (e: Exception) {
        e.printStackTrace()
      } finally {
        _isLoading.value = false
      }
    }
  }

  private suspend fun fetchManifest(mirrorUrl: String): SdkManifest? =
      withContext(Dispatchers.IO) {
        try {
          val baseUrl =
              "https://github.com/msmt2018/SDK-tool-for-Android-platform/releases/download/IDESdkDownJson2.3/manifest.json"
          val targetUrl =
              if (mirrorUrl.isNotEmpty() && baseUrl.startsWith("https://github.com"))
                  mirrorUrl + baseUrl
              else baseUrl

          val url = URL(targetUrl)
          val connection = url.openConnection() as HttpURLConnection
          connection.connectTimeout = 10000
          connection.readTimeout = 10000
          if (connection.responseCode == 200) {
            val json = connection.inputStream.bufferedReader().readText()
            Gson().fromJson(json, SdkManifest::class.java)
          } else null
        } catch (e: Exception) {
          null
        }
      }

  private fun getArch(): String = IDEBuildConfigProvider.getInstance().cpuArch.name.lowercase()

  fun triggerPendingChangesCheck() {
    var hasChanges = false
    fun checkNode(node: SdkTreeNode) {
      if (!node.isGroup) {
        if (node.checkedState == ToggleableState.On) {
          hasChanges = true
        }
      }
      node.children.forEach { checkNode(it) }
    }
    _treeNodes.value.forEach { checkNode(it) }
    _hasPendingChanges.value = hasChanges
  }

  private fun checkPendingChanges() {
    triggerPendingChangesCheck()
  }

  fun getInstallTasks(): List<SdkTreeNode> {
    val toInstall = mutableListOf<SdkTreeNode>()
    fun collect(node: SdkTreeNode) {
      if (!node.isGroup && node.checkedState == ToggleableState.On) {
        toInstall.add(node)
      }
      node.children.forEach { collect(it) }
    }
    _treeNodes.value.forEach { collect(it) }
    return toInstall
  }
}

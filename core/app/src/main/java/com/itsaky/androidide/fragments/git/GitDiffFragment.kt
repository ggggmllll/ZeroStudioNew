/*
 *  This file is part of AndroidIDE.
 */
package com.itsaky.androidide.fragments.git

import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.graphics.ColorUtils
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.catpuppyapp.puppygit.constants.Cons
import com.catpuppyapp.puppygit.git.StatusTypeEntrySaver
import com.catpuppyapp.puppygit.utils.Libgit2Helper
import com.github.git24j.core.Repository
import com.itsaky.androidide.R
import com.itsaky.androidide.databinding.FragmentGitDiffBinding
import com.itsaky.androidide.projects.IProjectManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

/**
 * Diff 查看器页面。
 *
 * @author android_zero
 */
class GitDiffFragment : BaseGitPageFragment() {

  private var _binding: FragmentGitDiffBinding? = null
  private val binding
    get() = _binding!!

  private val changedFiles = mutableListOf<StatusTypeEntrySaver>()
  private var currentIndex = 0
  private val lines = mutableListOf<DiffLine>()
  private var allLines = listOf<DiffLine>()
  private val adapter = DiffAdapter(lines)
  private var filterKeyword = ""
  private var compactMode = false

  override fun onCreateView(
      inflater: LayoutInflater,
      container: ViewGroup?,
      savedInstanceState: Bundle?,
  ): View {
    _binding = FragmentGitDiffBinding.inflate(inflater, container, false)
    return binding.root
  }

  override fun setupToolbar() {
    addToolbarAction(R.drawable.ic_add_24, getString(R.string.stage)) { stageCurrentFile() }

    addToolbarAction(R.drawable.ic_undo_24, getString(R.string.revert)) { revertCurrentFile() }

    addToolbarAction(R.drawable.ic_chevron_left_24, getString(R.string.previous_page)) {
      navigateDiff(-1)
    }

    addToolbarAction(R.drawable.ic_chevron_right_24, getString(R.string.next_page)) {
      navigateDiff(1)
    }
    addToolbarAction(R.drawable.ic_refresh_24, getString(R.string.refresh)) { reloadChangedFilesAndDiff() }
    addToolbarAction(R.drawable.ic_filter_list_24, getString(R.string.search)) { showSearchDialog() }
    addToolbarAction(R.drawable.ic_warning_24, "Style") {
      compactMode = !compactMode
      adapter.notifyDataSetChanged()
    }
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    binding.rvDiffLines.layoutManager = LinearLayoutManager(context)
    binding.rvDiffLines.adapter = adapter
    reloadChangedFilesAndDiff()
    observeDiffTargets()
    observeCommitTargets()
  }

  private fun reloadChangedFilesAndDiff() {
    withRepo { repo ->
      val statusList = Libgit2Helper.getWorkdirStatusList(repo)
      val loaded = Libgit2Helper.getWorktreeChangeList(repo, statusList, repoId = "")
      changedFiles.clear()
      changedFiles.addAll(loaded)
      currentIndex = currentIndex.coerceIn(0, (changedFiles.size - 1).coerceAtLeast(0))
      loadCurrentDiff(repo)
    }
  }

  private fun loadCurrentDiff(repo: Repository) {
    if (changedFiles.isEmpty()) {
      lines.clear()
      lines.add(DiffLine(-1, -1, "No changed files", DiffType.HUNK_HEADER))
      allLines = lines.toList()
      return
    }

    val file = changedFiles[currentIndex]

    val diffItem = runBlocking {
      Libgit2Helper.getSingleDiffItem(
          repo = repo,
          relativePathUnderRepo = file.relativePathUnderRepo,
          fromTo = Cons.gitDiffFromIndexToWorktree,
          loadChannel = null,
          checkChannelLinesLimit = 200,
          checkChannelSizeLimit = 1024 * 64,
      )
    }

    val rendered = mutableListOf<DiffLine>()
    rendered.add(DiffLine(-1, -1, "File: ${file.relativePathUnderRepo}", DiffType.HUNK_HEADER))
    diffItem.hunks.forEach { hunk ->
      rendered.add(DiffLine(-1, -1, hunk.hunk.cachedNoLineBreakHeader(), DiffType.HUNK_HEADER))
      hunk.lines.forEach { ln ->
        val type =
            when {
              ln.originType.contains("ADD", ignoreCase = true) -> DiffType.ADD
              ln.originType.contains("DEL", ignoreCase = true) -> DiffType.DELETE
              else -> DiffType.CONTEXT
            }
        rendered.add(
            DiffLine(
                oldLine = ln.oldLineNum,
                newLine = ln.newLineNum,
                content = ln.getContentNoLineBreak(),
                type = type,
            )
        )
      }
    }

    allLines = rendered
    applyFilter()
  }

  private fun stageCurrentFile() {
    val target = changedFiles.getOrNull(currentIndex) ?: return
    withRepo { repo -> Libgit2Helper.stageStatusEntryAndWriteToDisk(repo, listOf(target)) }
  }

  private fun revertCurrentFile() {
    val target = changedFiles.getOrNull(currentIndex) ?: return
    withRepo { repo ->
      Libgit2Helper.revertFilesToIndexVersion(
          repo,
          listOf(target.relativePathUnderRepo),
          force = true,
      )
      if (target.changeType == Cons.gitStatusNew) {
        Libgit2Helper.rmUntrackedFiles(listOf(target.canonicalPath))
      }
    }
  }

  private fun navigateDiff(delta: Int) {
    if (changedFiles.isEmpty()) {
      Toast.makeText(context, "No changed files", Toast.LENGTH_SHORT).show()
      return
    }
    currentIndex = (currentIndex + delta).coerceIn(0, changedFiles.lastIndex)
    withRepo { repo -> loadCurrentDiff(repo) }
  }

  private fun withRepo(action: suspend (Repository) -> Unit) {
    val projectDir =
        IProjectManager.getInstance().getWorkspace()?.getProjectDir()?.path
            ?: IProjectManager.getInstance().projectDirPath
    if (projectDir.isBlank()) {
      Toast.makeText(context, "No opened project", Toast.LENGTH_SHORT).show()
      return
    }

    viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
      val ret = runCatching { Repository.open(projectDir).use { repo -> action(repo) } }
      withContext(Dispatchers.Main) {
        ret.onSuccess { adapter.notifyDataSetChanged() }
        ret.onFailure {
          Toast.makeText(context, it.localizedMessage ?: "Diff operation failed", Toast.LENGTH_LONG)
              .show()
        }
      }
    }
  }

  private fun observeDiffTargets() {
    viewLifecycleOwner.lifecycleScope.launch {
      viewLifecycleOwner.repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.STARTED) {
        GitSharedState.selectedDiffPath.collect { targetPath ->
          if (targetPath.isNullOrBlank()) return@collect
          val idx = changedFiles.indexOfFirst { it.relativePathUnderRepo == targetPath }
          if (idx >= 0) {
            currentIndex = idx
            reloadChangedFilesAndDiff()
          }
        }
      }
    }
  }

  private fun observeCommitTargets() {
    viewLifecycleOwner.lifecycleScope.launch {
      viewLifecycleOwner.repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.STARTED) {
        GitSharedState.selectedCommitHash.collect { commitHash ->
          if (commitHash.isNullOrBlank()) return@collect
          withRepo { repo ->
            val settings = com.catpuppyapp.puppygit.settings.SettingsUtil.getSettingsSnapshot()
            val dto =
                Libgit2Helper.getSingleCommitSimple(
                    repo = repo,
                    repoId = "",
                    commitOidStr = commitHash,
                    settings = settings,
                )
            allLines =
                listOf(
                    DiffLine(-1, -1, "Commit: ${dto.shortOidStr}", DiffType.HUNK_HEADER),
                    DiffLine(-1, -1, "Author: ${dto.author}", DiffType.CONTEXT),
                    DiffLine(-1, -1, "Branches: ${dto.branchShortNameList.joinToString()}", DiffType.CONTEXT),
                    DiffLine(-1, -1, "Parents: ${dto.parentShortOidStrList.joinToString()}", DiffType.CONTEXT),
                    DiffLine(-1, -1, dto.msg, DiffType.CONTEXT),
                )
            applyFilter()
          }
        }
      }
    }
  }

  private fun showSearchDialog() {
    val input = EditText(requireContext()).apply { setText(filterKeyword) }
    AlertDialog.Builder(requireContext())
        .setTitle(getString(R.string.search))
        .setView(input)
        .setPositiveButton(android.R.string.ok) { _, _ ->
          filterKeyword = input.text?.toString().orEmpty().trim()
          applyFilter()
        }
        .setNegativeButton(android.R.string.cancel, null)
        .show()
  }

  private fun applyFilter() {
    val filtered =
        if (filterKeyword.isBlank()) {
          allLines
        } else {
          allLines.filter { it.content.contains(filterKeyword, ignoreCase = true) }
        }
    lines.clear()
    lines.addAll(filtered)
  }

  enum class DiffType {
    ADD,
    DELETE,
    CONTEXT,
    HUNK_HEADER,
  }

  data class DiffLine(val oldLine: Int, val newLine: Int, val content: String, val type: DiffType)

  inner class DiffAdapter(private val data: List<DiffLine>) :
      RecyclerView.Adapter<DiffAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
      val v =
          LayoutInflater.from(parent.context).inflate(R.layout.item_git_diff_line, parent, false)
      return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
      val item = data[position]
      holder.tvContent.text = item.content
      holder.tvLineOld.text = if (item.oldLine > 0) item.oldLine.toString() else ""
      holder.tvLineNew.text = if (item.newLine > 0) item.newLine.toString() else ""

      when (item.type) {
        DiffType.ADD -> {
          holder.itemView.setBackgroundColor(
              if (compactMode) ColorUtils.setAlphaComponent(Color.parseColor("#4CAF50"), 20)
              else Color.parseColor("#1A4CAF50")
          )
          holder.tvContent.setTextColor(Color.parseColor("#A5D6A7"))
        }
        DiffType.DELETE -> {
          holder.itemView.setBackgroundColor(
              if (compactMode) ColorUtils.setAlphaComponent(Color.parseColor("#F44336"), 20)
              else Color.parseColor("#1AF44336")
          )
          holder.tvContent.setTextColor(Color.parseColor("#EF9A9A"))
        }
        DiffType.HUNK_HEADER -> {
          holder.itemView.setBackgroundColor(Color.parseColor("#2C2C2C"))
          holder.tvContent.setTextColor(Color.parseColor("#90CAF9"))
        }
        else -> {
          holder.itemView.setBackgroundColor(Color.TRANSPARENT)
          holder.tvContent.setTextColor(Color.parseColor("#C4C4C4"))
        }
      }
    }

    override fun getItemCount() = data.size

    inner class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
      val tvLineOld: TextView = v.findViewById(R.id.tv_line_old)
      val tvLineNew: TextView = v.findViewById(R.id.tv_line_new)
      val tvContent: TextView = v.findViewById(R.id.tv_content)
    }
  }

  override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
  }
}

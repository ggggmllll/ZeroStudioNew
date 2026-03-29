/*
 * @author android_zero
 * 包名：com.itsaky.androidide.repository.dependencies.analyzer.internal
 */
package com.itsaky.androidide.repository.dependencies.analyzer.internal

import com.itsaky.androidide.repository.dependencies.models.datas.*
import com.itsaky.androidide.repository.dependencies.models.enums.*
import com.itsaky.androidide.repository.dependencies.models.interfaces.*
import java.io.File
import java.util.regex.Pattern

class GradleScriptAnalyzer {

  private val DEPENDENCY_STRING_REGEX =
      Pattern.compile(
          """(implementation|api|compileOnly|runtimeOnly|kapt|ksp|annotationProcessor|testImplementation|androidTestImplementation)\s*\(?\s*["']([^"']+)["']\s*\)?"""
      )

  private val DEPENDENCY_TOML_REGEX =
      Pattern.compile(
          """(implementation|api|compileOnly|runtimeOnly|kapt|ksp|annotationProcessor|testImplementation|androidTestImplementation)\s*\(?\s*((?:libs\.)[a-zA-Z0-9_.]+(?:(?:\.get\(\))|(?:\.get\(\)\.toString\(\)))?)\s*\)?"""
      )

  private val MAVEN_URL_REGEX =
      Pattern.compile("""url\s*=?\s*(?:uri\s*\()?\s*["']([^"']+)["']\s*\)?""")

  fun analyze(scriptFile: File): Pair<List<ScopedRepositoryInfo>, List<ScopedDependencyInfo>> {
    if (!scriptFile.exists()) return Pair(emptyList(), emptyList())

    val repositories = mutableListOf<ScopedRepositoryInfo>()
    val dependencies = mutableListOf<ScopedDependencyInfo>()

    val lines = scriptFile.readLines()
    val scopeTracker = ScopeTracker()
    var currentFileOffset = 0

    for (line in lines) {
      val trimmed = line.trim()
      val lineLength = line.length + 1

      if (trimmed.startsWith("//")) {
        currentFileOffset += lineLength
        continue
      }

      scopeTracker.update(line)
      val currentBlock = scopeTracker.currentScope()

      when (currentBlock) {
        DslBlockType.REPOSITORIES,
        DslBlockType.PLUGIN_MANAGEMENT -> {
          if (trimmed.startsWith("google()")) {
            repositories.add(
                createRepo("google", "https://maven.google.com/", RepositoryType.GOOGLE, scriptFile)
            )
          } else if (trimmed.startsWith("mavenCentral()")) {
            repositories.add(
                createRepo(
                    "mavenCentral",
                    "https://repo1.maven.org/maven2/",
                    RepositoryType.MAVEN_CENTRAL,
                    scriptFile,
                )
            )
          } else if (trimmed.contains("url")) {
            val matcher = MAVEN_URL_REGEX.matcher(trimmed)
            if (matcher.find()) {
              val url = matcher.group(1)
              if (url != null && url.startsWith("http")) {
                repositories.add(
                    createRepo(
                        "custom_${url.hashCode()}",
                        url,
                        RepositoryType.CUSTOM_MAVEN,
                        scriptFile,
                    )
                )
              }
            }
          }
        }

        DslBlockType.DEPENDENCIES -> {
          val stringMatcher = DEPENDENCY_STRING_REGEX.matcher(trimmed)
          if (stringMatcher.find()) {
            val config = stringMatcher.group(1)
            val gav = stringMatcher.group(2)

            val parts = gav.split(":")
            if (parts.size >= 3) {
              val group = parts[0]
              val artifact = parts[1]
              val version = parts[2]

              val verStartInLine = line.indexOf(version)
              val range =
                  if (verStartInLine != -1) {
                    val absStart = currentFileOffset + verStartInLine
                    TextRange(absStart, absStart + version.length)
                  } else null

              val statementRange = TextRange(currentFileOffset, currentFileOffset + line.length)

              dependencies.add(
                  ScopedDependencyInfo(
                      configuration = config,
                      groupId = group,
                      artifactId = artifact,
                      version = version,
                      declaredFile = scriptFile,
                      declarationType = DeclarationType.STRING_LITERAL,
                      // 修复：使用 versionDefinitionRange
                      versionDefinitionRange = range,
                      statementTextRange = statementRange,
                  )
              )
            }
          }

          val tomlMatcher = DEPENDENCY_TOML_REGEX.matcher(trimmed)
          if (tomlMatcher.find()) {
            val config = tomlMatcher.group(1)
            var refRaw = tomlMatcher.group(2)
            refRaw = refRaw.replace(".get()", "").replace(".toString()", "")

            dependencies.add(
                ScopedDependencyInfo(
                    configuration = config,
                    groupId = "placeholder",
                    artifactId = "placeholder",
                    version = "placeholder",
                    declaredFile = scriptFile,
                    declarationType = DeclarationType.CATALOG_ACCESSOR,
                    versionReference = refRaw,
                    statementTextRange =
                        TextRange(currentFileOffset, currentFileOffset + line.length),
                )
            )
          }
        }

        else -> {}
      }
      currentFileOffset += lineLength
    }
    return Pair(repositories, dependencies)
  }

  private fun createRepo(
      id: String,
      url: String,
      type: RepositoryType,
      file: File,
  ): ScopedRepositoryInfo {
    return ScopedRepositoryInfo(id, url, type, file)
  }
}

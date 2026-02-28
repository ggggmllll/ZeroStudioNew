/*
 * @author android_zero
 * 包名：com.itsaky.androidide.repository.dependencies.analyzer.internal
 * 用途：针对 build.gradle (Groovy) 的高精度词法分析器。
 * 依赖：com.itsaky.androidide.lexers.groovy.GroovyLexer
 */
package com.itsaky.androidide.repository.dependencies.analyzer.internal

import com.itsaky.androidide.lexers.groovy.GroovyLexer
import com.itsaky.androidide.repository.dependencies.models.*
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.Token
import java.io.File

class GroovyAstAnalyzer : ScriptAnalyzer {

    override fun analyze(file: File): Pair<List<ScopedRepositoryInfo>, List<ScopedDependencyInfo>> {
        val repos = mutableListOf<ScopedRepositoryInfo>()
        val deps = mutableListOf<ScopedDependencyInfo>()
        
        if (!file.exists()) return Pair(repos, deps)

        val text = file.readText()
        val lexer = GroovyLexer(CharStreams.fromString(text))
        
        // 过滤掉空白符与注释，保留有实际意义的语法 Token
        val validTokens = lexer.allTokens.filter { 
            it.type != GroovyLexer.WS && 
            it.type != GroovyLexer.LINE_COMMENT && 
            it.type != GroovyLexer.BLOCK_COMMENT 
        }

        var depth = 0
        var blockDepth = -1
        var currentBlock: String? = null

        var i = 0
        while (i < validTokens.size) {
            val token = validTokens[i]

            // 维护闭包深度状态
            if (token.type == GroovyLexer.LBRACE) {
                depth++
            } else if (token.type == GroovyLexer.RBRACE) {
                depth--
                if (depth == blockDepth) {
                    currentBlock = null
                    blockDepth = -1
                }
            }

            // 探测闭包入口，例如: dependencies {
            if (token.type == GroovyLexer.IDENTIFIER && i + 1 < validTokens.size && validTokens[i + 1].type == GroovyLexer.LBRACE) {
                val blockName = token.text
                if (blockName == "dependencies" || blockName == "repositories" || blockName == "pluginManagement") {
                    currentBlock = blockName
                    blockDepth = depth
                }
            }

            // ------------- 解析 Dependencies 块 -------------
            if (currentBlock == "dependencies") {
                if (token.type == GroovyLexer.IDENTIFIER) {
                    val configName = token.text
                    if (i + 1 < validTokens.size) {
                        val next = validTokens[i + 1]
                        var gavToken: Token? = null
                        var isToml = false
                        var tomlRefStr = ""

                        // 模式 1: implementation 'g:a:v'
                        if (next.type == GroovyLexer.STRING_LITERAL || next.type == GroovyLexer.SINGLE_QUOTE_STRING) {
                            gavToken = next
                        } 
                        // 模式 2: implementation("g:a:v") 或 implementation(libs.xxx)
                        else if (next.type == GroovyLexer.LPAREN && i + 2 < validTokens.size) {
                            val next2 = validTokens[i + 2]
                            if (next2.type == GroovyLexer.STRING_LITERAL || next2.type == GroovyLexer.SINGLE_QUOTE_STRING) {
                                gavToken = next2
                            } else if (next2.type == GroovyLexer.IDENTIFIER && next2.text == "libs") {
                                isToml = true
                                var j = i + 2
                                // 拼接 libs.xxx.yyy 引用链
                                while (j < validTokens.size && (validTokens[j].type == GroovyLexer.DOT || validTokens[j].type == GroovyLexer.IDENTIFIER)) {
                                    tomlRefStr += validTokens[j].text
                                    j++
                                }
                                deps.add(ScopedDependencyInfo(
                                    configuration = configName,
                                    groupId = "placeholder", artifactId = "placeholder", version = "placeholder",
                                    declaredFile = file,
                                    declarationType = DeclarationType.CATALOG_ACCESSOR,
                                    versionReference = tomlRefStr.removePrefix("libs."),
                                    statementTextRange = TextRange(token.startIndex, validTokens[j - 1].stopIndex + 1)
                                ))
                                i = j - 1
                                continue
                            }
                        }

                        if (!isToml && gavToken != null) {
                            val rawStr = gavToken.text
                            val cleanStr = rawStr.trim('\'', '\"')
                            val parts = cleanStr.split(":")
                            if (parts.size >= 3) {
                                val version = parts[2]
                                // 提取精确的偏移量
                                val verStart = gavToken.startIndex + rawStr.lastIndexOf(version)
                                deps.add(ScopedDependencyInfo(
                                    configuration = configName,
                                    groupId = parts[0],
                                    artifactId = parts[1],
                                    version = version,
                                    declaredFile = file,
                                    declarationType = DeclarationType.STRING_LITERAL,
                                    versionTextRange = TextRange(verStart, verStart + version.length),
                                    statementTextRange = TextRange(token.startIndex, gavToken.stopIndex + 1)
                                ))
                            }
                        }
                    }
                }
            } 
            // ------------- 解析 Repositories 块 -------------
            else if (currentBlock == "repositories" || currentBlock == "pluginManagement") {
                if (token.type == GroovyLexer.IDENTIFIER) {
                    val funcName = token.text
                    if (funcName == "google") {
                        repos.add(ScopedRepositoryInfo("google", "https://maven.google.com/", RepositoryType.GOOGLE, file))
                    } else if (funcName == "mavenCentral") {
                        repos.add(ScopedRepositoryInfo("mavenCentral", "https://repo1.maven.org/maven2/", RepositoryType.MAVEN_CENTRAL, file))
                    } else if (funcName == "maven") {
                        // 向前搜索匹配 url '...'
                        var j = i + 1
                        while (j < validTokens.size && validTokens[j].type != GroovyLexer.RBRACE) {
                            if (validTokens[j].type == GroovyLexer.IDENTIFIER && validTokens[j].text == "url") {
                                var k = j + 1
                                while (k < validTokens.size && validTokens[k].type != GroovyLexer.RBRACE) {
                                    val tk = validTokens[k]
                                    if (tk.type == GroovyLexer.STRING_LITERAL || tk.type == GroovyLexer.SINGLE_QUOTE_STRING) {
                                        val url = tk.text.trim('\'', '\"')
                                        if (url.startsWith("http")) {
                                            repos.add(ScopedRepositoryInfo("custom_${url.hashCode()}", url, RepositoryType.CUSTOM_MAVEN, file))
                                        }
                                        break
                                    }
                                    k++
                                }
                                break
                            }
                            j++
                        }
                    }
                }
            }
            i++
        }
        return Pair(repos, deps)
    }
}
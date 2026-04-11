package com.itsaky.androidide.lsp.kotlin.lsp.ops

import java.nio.file.Path
import org.eclipse.lsp4j.ExecuteCommandParams
import org.javacs.kt.externalsources.FernFlowerDecompiler

/** Extra operation capabilities: command dispatch + decompile + source resolve support. */
class KotlinLspOperationService {

  private val decompiler = FernFlowerDecompiler()

  fun execute(command: String, args: List<Any> = emptyList()): ExecuteCommandParams {
    return ExecuteCommandParams(command, args)
  }

  fun decompileClassFile(path: Path): Path {
    return decompiler.decompileClass(path)
  }

  fun decompileLibraryJar(path: Path): Path {
    return decompiler.decompileJar(path)
  }
}

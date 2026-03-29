// rootProject/settings.gradle.kts（仅展示修改部分，其他配置保留）
dependencyResolutionManagement {
  val dependencySubstitutions =
      mapOf(
          "build-deps" to
              arrayOf(
                  "appintro",
                  "fuzzysearch",
                  "google-java-format",
                  "java-compiler",
                  "javac",
                  "javapoet",
                  "jaxp",
                  "jdk-compiler",
                  "jdk-jdeps",
                  "jdt",
                  "layoutlib-api",
                  "logback-core",
                  "editor",
                  "soraLanguageTextmate", // 该模块需要依赖 soraOnigurumaNative
                  "compose-pullrefresh",
              ),
          "build-deps-common" to arrayOf("desugaring-core"),
      )

  for ((build, modules) in dependencySubstitutions) {
    includeBuild("composite-builds/${build}") {
      this.name = build
      dependencySubstitution {
        for (module in modules) {
          substitute(module("com.itsaky.androidide.build:${module}")).using(project(":${module}"))
        }
        // 关键：为 soraLanguageTextmate 添加依赖替换，指向根目录的 modules:soraOnigurumaNative
        if (build == "build-deps" && modules.contains("soraLanguageTextmate")) {
          substitute(module("com.itsaky.androidide.build:soraOnigurumaNative"))
              .using(project(":modules:soraOnigurumaNative"))
        }
      }
    }
  }

  // 其他配置（repositories 等）保留不变
}

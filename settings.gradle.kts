pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "SelfBell"
include(":app")
include(":domain")
include(":core")
include(":data")
include(":feature")
include(":feature:home", ":feature:alerts", ":feature:emergency", ":feature:escort", ":feature:settings") // 생성할 피처 모듈들도 여기에 추가해야 합니다.

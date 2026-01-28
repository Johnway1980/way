// 在项目加载前设置系统属性以禁用Room验证器（解决Windows权限问题）
System.setProperty("room.disableVerification", "true")

pluginManagement {
    repositories {
        google {
            content {
        gradlePluginPortal()
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

rootProject.name = "AlphaDoer"
include(":app")

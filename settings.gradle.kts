// 在项目加载前设置系统属性以禁用Room验证器（解决Windows权限问题）
System.setProperty("room.disableVerification", "true")

pluginManagement {
    repositories {
        // Try Google's Maven first so AGP can be resolved for plugin IDs
        maven {
            url = uri("https://dl.google.com/dl/android/maven2/")
        }
        google()
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

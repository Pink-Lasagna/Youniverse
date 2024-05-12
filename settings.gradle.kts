pluginManagement {
    repositories {
        google()
        mavenCentral()
        maven {
            url = uri("https://npm2mvn.jadaptive.com")
        }
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven {
            url = uri("https://npm2mvn.jadaptive.com")
        }
    }
}

rootProject.name = "Youniverse"
include(":app")

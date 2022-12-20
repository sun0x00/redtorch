group = "xyz.redtorch"
version = "2.0.0"
description = """Java开源量化交易开发框架 http://redtorch.io/"""

// 通过扩展配置统一管理版本
extra.apply {
    set("slf4jApiVersion", "1.7.36")
    set("logbackVersion", "1.2.10")
    set("jacksonVersion","2.14.1")
    set("zookeeperVersion","3.8.0")
    set("commonsIOVersion","2.11.0")
    set("lz4JavaVersion","1.8.0")
    set("zjsonpatchVersion","0.4.13")

}

// 所有项目使用相同仓库配置（删除会导致无法找到依赖）
allprojects {
    repositories {
        mavenLocal()

        // 自定义Maven仓库镜像，必须使用https
        maven {
            url = uri("https://maven.aliyun.com/repository/public")
        }

        mavenCentral()
    }
}

// 全局插件配置（大多复用，少量无需复用的可单独在子项目中定义）
plugins {
    // Java插件
    id("java")

    // Kotlin插件
    kotlin("jvm") version "1.7.20"
    kotlin("plugin.spring") version "1.7.20"

    // Spring Boot 插件
    id("org.springframework.boot") version "2.7.6"
    id("io.spring.dependency-management") version "1.0.13.RELEASE"
}

// 应用于所有子项目
subprojects {

    // 子项目均需要的插件
	apply {
        plugin("java")
        plugin("kotlin")
	}

    java {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            jvmTarget = "17"
        }
    }

    // 子项目均需要的依赖
    dependencies {
        implementation("org.slf4j:slf4j-api:${rootProject.extra.get("slf4jApiVersion")}")
        implementation("ch.qos.logback:logback-core:${rootProject.extra.get("logbackVersion")}")
        implementation("ch.qos.logback:logback-classic:${rootProject.extra.get("logbackVersion")}")
    }

}
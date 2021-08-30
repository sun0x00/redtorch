group = "xyz.redtorch"
version = "2.0.0"
description = """Java开源量化交易开发框架 http://redtorch.io/"""

// 通过扩展配置统一管理版本
extra.apply {
    set("slf4jApiVersion", "1.7.30")
    set("logbackVersion", "1.2.3")
    set("jacksonVersion","2.12.4")
    set("zookeeperVersion","3.6.2")
    set("commonsIOVersion","2.8.0")
    set("lz4JavaVersion","1.8.0")
    set("zjsonpatchVersion","0.4.11")

}

// 所有项目使用相同仓库配置（删除会导致无法找到依赖）
allprojects {
    repositories {
        mavenLocal()

        // 自定义Maven仓库镜像，必须使用https
        maven {
            url = uri("https://maven.aliyun.com/nexus/content/groups/public/")
        }

        mavenCentral()
    }
}

// 全局插件配置（大多复用，少量无需复用的可单独在子项目中定义）
plugins {
    // Java插件
    id("java")

    // Kotlin插件
    kotlin("jvm") version "1.5.21"
    kotlin("plugin.spring") version "1.5.21"

    // Spring Boot 插件
    id("org.springframework.boot") version "2.5.4"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
}

// 应用于所有子项目
subprojects {

    // 子项目均需要的插件
	apply {
        plugin("java")
        plugin("kotlin")
	}

    // 子项目均需要的依赖
    dependencies {
        implementation("org.jetbrains.kotlin:kotlin-reflect")
        implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
        implementation("org.slf4j:slf4j-api:${rootProject.extra.get("slf4jApiVersion")}")
        implementation("ch.qos.logback:logback-core:${rootProject.extra.get("logbackVersion")}")
        implementation("ch.qos.logback:logback-classic:${rootProject.extra.get("logbackVersion")}")
    }

}
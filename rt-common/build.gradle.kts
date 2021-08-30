plugins {
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

dependencies {
    implementation("com.fasterxml.jackson.core:jackson-databind:${rootProject.extra.get("jacksonVersion")}")
    implementation("com.fasterxml.jackson.core:jackson-core:${rootProject.extra.get("jacksonVersion")}")
    implementation("com.fasterxml.jackson.core:jackson-annotations:${rootProject.extra.get("jacksonVersion")}")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:${rootProject.extra.get("jacksonVersion")}")
    implementation("org.apache.zookeeper:zookeeper:${rootProject.extra.get("zookeeperVersion")}")
    implementation("commons-io:commons-io:${rootProject.extra.get("commonsIOVersion")}")
    implementation("org.lz4:lz4-java:${rootProject.extra.get("lz4JavaVersion")}")
    // implementation("com.github.java-json-tools:json-patch:1.13")
    implementation("com.flipkart.zjsonpatch:zjsonpatch:${rootProject.extra.get("zjsonpatchVersion")}")
    compileOnly("org.springframework.boot:spring-boot-starter-undertow")
    compileOnly("org.springframework.boot:spring-boot-starter-websocket") {
        exclude("org.springframework.boot","spring-boot-starter-tomcat")
    }

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")

}

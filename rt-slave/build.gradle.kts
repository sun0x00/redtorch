plugins {
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

springBoot {
    mainClass.set("xyz.redtorch.slave.RedTorchSlaveApplicationKt")
}

dependencies {
    implementation(project(":rt-common"))
    implementation(project(":rt-gateway-api"))
    implementation(project(":rt-gateway-ctp"))

    implementation("org.springframework.boot:spring-boot-starter-undertow")
    implementation("org.springframework.boot:spring-boot-starter-websocket") {
        exclude("org.springframework.boot","spring-boot-starter-tomcat")
    }
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}
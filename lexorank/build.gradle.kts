plugins {
    kotlin("jvm")
    signing
    `maven-publish`
}

tasks.withType<Test> {
    useJUnitPlatform()
}

group = "com.fanmaum"
version = "1.0.0"

dependencies {
    //noinspection UseTomlInstead
    testImplementation("io.kotest:kotest-runner-junit5:5.9.0")
    testImplementation("io.kotest:kotest-assertions-core:5.9.0")
    testImplementation("org.jetbrains.kotlin:kotlin-test:1.9.10")
}

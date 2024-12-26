import com.vanniktech.maven.publish.SonatypeHost

plugins {
    kotlin("jvm")
    id("com.vanniktech.maven.publish") version "0.30.0"
}

tasks.withType<Test> {
    useJUnitPlatform()
}

mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
    signAllPublications()

    coordinates (
        groupId = "com.fanmaum",
        artifactId = "LexoRankKt",
        version = "1.0.0"
    )
    pom {
        name.set("LexoRankKt")
        description.set("LexoRank for kotlin")
        url.set("https://github.com/fan-maum/LexoRankKt")

        licenses {
            license {
                name.set("Apache License, Version 2.0")
                url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }

        developers {
            developer {
                id.set("fanmaum")
                name.set("fanmaum")
                email.set("dev@fanmaum.com")
            }
        }

        scm {
            connection.set("scm:git:git://github.com/fan-maum/LexoRankKt.git")
            developerConnection.set("scm:git:ssh://github.com:fan-maum/LexoRankKt.git")
            url.set("https://github.com/fan-maum/LexoRankKt")
        }
    }
}

dependencies {
    //noinspection UseTomlInstead
    testImplementation("io.kotest:kotest-runner-junit5:5.9.0")
    testImplementation("io.kotest:kotest-assertions-core:5.9.0")
    testImplementation("org.jetbrains.kotlin:kotlin-test:1.9.10")
}

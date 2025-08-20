plugins {
    id("java")
    id("io.freefair.lombok") version "8.14.2"
    id("com.diffplug.spotless") version "7.2.1"
    id("com.avast.gradle.docker-compose") version "0.17.12"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

repositories {
    mavenCentral()
    maven {
        url = uri("https://maven.pkg.github.com/triplea-game/triplea")
        credentials {
            username = System.getenv("GITHUB_ACTOR") ?: project.findProperty("triplea.github.username") as String?
            password = System.getenv("GH_TOKEN") ?: project.findProperty("triplea.github.access.token") as String?
        }
    }
}


tasks.jar {
    manifest {
        attributes["Main-Class"] = "org.triplea.server.SupportServerApplication"
    }
}

tasks.shadowJar {
    archiveClassifier.set("")
    // mergeServiceFiles is needed by dropwizard
    // Without this configuration parsing breaks and is unable to find connector type "http" for
    // the following YAML snippet:  server: {applicationConnectors: [{type: http, port: 8080}]
    mergeServiceFiles()
}

/* "testInteg" runs tests that require a database or a server to be running */
val testInteg: SourceSet = sourceSets.create("testInteg") {
    java {
        java.srcDir("src/testInteg/java")
        compileClasspath += sourceSets.main.get().output
        runtimeClasspath += output + compileClasspath
    }
}

configurations[testInteg.implementationConfigurationName].extendsFrom(configurations.testImplementation.get())
configurations[testInteg.runtimeOnlyConfigurationName].extendsFrom(configurations.testRuntimeOnly.get())

val testIntegTask = tasks.register<Test>("testInteg") {
    group = "verification"
    testClassesDirs = sourceSets["testInteg"].output.classesDirs
    classpath = sourceSets["testInteg"].runtimeClasspath
}

tasks.check {
    dependsOn(testIntegTask)
}


///* docker compose used to set up integ tests, starts a server and database */
// See: https://github.com/avast/gradle-docker-compose-plugin
dockerCompose {
    captureContainersOutput = true
    isRequiredBy(testIntegTask)
    setProjectName("support-server")
    // suppress unset variable warning, assign variables to empty string (which will result in random port numbers)
    environment = mapOf("DATABASE_PORT" to "", "SERVER_PORT" to "")
}

tasks.composeBuild {
    dependsOn(tasks.shadowJar)
}

tasks.register<Exec>("dockerComposeClean") {
    group = "docker"
    description = "Docker compose stop and removes volumes"
    commandLine("docker", "compose", "down", "--volumes")
}

tasks.clean {
    dependsOn(tasks.findByName("dockerComposeClean"))
}

tasks {
    withType<Test> {
        useJUnitPlatform()
        testLogging {
            events("standardOut", "standardError", "skipped", "failed")
        }
        jvmArgs("-XX:+EnableDynamicAgentLoading")
    }
}

spotless {
    java {
        googleJavaFormat()
        removeUnusedImports()
    }
}

val dropWizardVersion = "4.0.7"
val feignVersion = "13.6"
val jaxbVersion = "4.0.5"
val junitVersion = "5.13.4"
val mockitoVersion = "5.18.0"
val tripleaVersion = "2.7.15062"

dependencies {
    implementation("at.favre.lib:bcrypt:0.10.2")
    implementation("be.tomcools:dropwizard-websocket-jsr356-bundle:4.0.0")
    implementation("com.sun.mail:jakarta.mail:2.0.1")
    implementation("com.sun.xml.bind:jaxb-core:$jaxbVersion")
    implementation("com.sun.xml.bind:jaxb-impl:$jaxbVersion")
    implementation("io.dropwizard:dropwizard-auth:$dropWizardVersion")
    implementation("io.dropwizard:dropwizard-core:$dropWizardVersion")
    implementation("io.dropwizard:dropwizard-jdbi3:$dropWizardVersion")
    implementation("io.github.openfeign:feign-core:$feignVersion")
    implementation("io.github.openfeign:feign-gson:$feignVersion")
    implementation("javax.activation:activation:1.1.1")
    implementation("javax.servlet:servlet-api:2.5")
    implementation("javax.xml.bind:jaxb-api:2.3.1")
    implementation("org.apache.httpcomponents:httpclient:4.5.14")
    implementation("org.java-websocket:Java-WebSocket:1.6.0")
    implementation("org.jdbi:jdbi3-core:3.49.5")
    implementation("org.jdbi:jdbi3-sqlobject:3.49.5")
    implementation("org.snakeyaml:snakeyaml-engine:2.10")
    implementation("triplea:domain-data:$tripleaVersion")
    implementation("triplea:feign-common:$tripleaVersion")
    implementation("triplea:java-extras:$tripleaVersion")
    implementation("triplea:lobby-client:$tripleaVersion")
    implementation("triplea:websocket-client:$tripleaVersion")
    runtimeOnly("org.postgresql:postgresql:42.7.7")

    testImplementation("com.github.database-rider:rider-junit5:1.44.0")
    testImplementation("com.github.npathai:hamcrest-optional:2.0.0")
    testImplementation("com.sun.mail:jakarta.mail:2.0.1")
    testImplementation("io.dropwizard:dropwizard-testing:$dropWizardVersion")
    testImplementation("org.awaitility:awaitility:4.3.0")
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-params:$junitVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")

    testImplementation("org.assertj:assertj-core:3.27.3")
    testImplementation("org.mockito:mockito-core:$mockitoVersion")
    testImplementation("org.mockito:mockito-junit-jupiter:$mockitoVersion")
    testImplementation("org.wiremock:wiremock:3.13.1")
    testImplementation("ru.lanwen.wiremock:wiremock-junit5:1.3.1")
    testImplementation("uk.co.datumedge:hamcrest-json:0.3")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.13.4")
}

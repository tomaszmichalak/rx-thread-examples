plugins {
    id("org.jetbrains.kotlin.jvm") version "1.3.61"
}

repositories {
    jcenter()
}

dependencies {
    val vertxVersion = "3.8.4"
    val junitVersion = "5.5.2"

    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    implementation(platform("io.vertx:vertx-dependencies:$vertxVersion"))
    implementation("io.vertx:vertx-core")
    implementation("io.vertx:vertx-lang-kotlin")

    testImplementation("io.vertx:vertx-junit5")
    testImplementation("io.vertx:vertx-unit")
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-params:$junitVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")

    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
}

tasks.withType<Test>().configureEach {
    systemProperties(Pair("vertx.logger-delegate-factory-class-name", "io.vertx.core.logging.SLF4JLogDelegateFactory"))

    failFast = true
    useJUnitPlatform()
}
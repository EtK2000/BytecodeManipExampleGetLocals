plugins {
	`java-library`
}

dependencies {
	testImplementation(platform(libs.junit.bom))
	testImplementation(libs.junit.jupiter)
	testRuntimeOnly(libs.junit.platform.launcher)
}

group = "com.etk2000.bcm"

java {
	sourceCompatibility = JavaVersion.VERSION_17
	targetCompatibility = JavaVersion.VERSION_17
}

tasks.withType<Test>().configureEach {
	useJUnitPlatform()
}

// slot a task in during the compilation phase so it runs for normal compiles
tasks.register(
	"bytecodeManipulation",
	com.etk2000.bcm.getlocals.DependencyPatcherTask::class
) {
	dependsOn("compileJava")
	compileTask = tasks["compileJava"]
}
tasks["classes"].dependsOn("bytecodeManipulation")

// slot a task in during the test compilation phase so it runs for tests
tasks.register(
	"bytecodeManipulationTesting",
	com.etk2000.bcm.getlocals.DependencyPatcherTask::class
) {
	dependsOn("compileTestJava")
	compileTask = tasks["compileTestJava"]
}
tasks["testClasses"].dependsOn("bytecodeManipulationTesting")
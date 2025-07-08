repositories {
	mavenLocal()
	mavenCentral()
}

dependencies {
	implementation(gradleApi())
	implementation(libs.asm)
	implementation(libs.asm.tree)
}
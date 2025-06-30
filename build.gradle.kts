plugins {
	kotlin("jvm") version "1.9.25"
	kotlin("plugin.spring") version "1.9.25"
	id("org.springframework.boot") version "3.5.3"
	id("io.spring.dependency-management") version "1.1.7"
}

group = "com.spotifiejpcastor"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

configurations {
	compileOnly {
		extendsFrom(configurations.annotationProcessor.get())
	}
}

repositories {
	mavenCentral()
}

extra["springAiVersion"] = "1.0.0"

dependencies {
	// ✅ CORE REACTIVO - Spring WebFlux (NO spring-boot-starter-web)
	implementation("org.springframework.boot:spring-boot-starter-webflux")

	// ✅ REDIS REACTIVO - Oficial Spring Boot Starter
	implementation("org.springframework.boot:spring-boot-starter-data-redis-reactive")

	// ✅ OAUTH2 CLIENT REACTIVO - Oficial Spring Security
	implementation("org.springframework.boot:spring-boot-starter-oauth2-client")

	// ✅ SECURITY REACTIVO - Para @EnableWebFluxSecurity
	implementation("org.springframework.boot:spring-boot-starter-security")

	// ✅ VALIDATION - Para @Valid en controllers reactivos
	implementation("org.springframework.boot:spring-boot-starter-validation")

	// ✅ KOTLIN SUPPORT
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.jetbrains.kotlin:kotlin-reflect")

	// ✅ REACTIVE STREAMS & COROUTINES - Para Kotlin Coroutines con Reactor
	implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")

	// ✅ DEVELOPMENT
	developmentOnly("org.springframework.boot:spring-boot-devtools")

	// ✅ LETTUCE CONNECTION POOLING (para producción)
	implementation("org.apache.commons:commons-pool2")

	// ✅ TESTING REACTIVO
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.security:spring-security-test")
	testImplementation("io.projectreactor:reactor-test")
	testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
	testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")

	// ✅ EMBEDDED REDIS FOR TESTING (opcional)
	testImplementation("it.ozimov:embedded-redis:0.7.3")
}

dependencyManagement {
	imports {
		mavenBom("org.springframework.ai:spring-ai-bom:${property("springAiVersion")}")
	}
}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict")
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}

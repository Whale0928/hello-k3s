/*
 * build.gradle.kts - Kotlin DSL 빌드 스크립트
 *
 * 왜 Groovy (.gradle) 대신 Kotlin DSL (.kts)을 사용하는가?
 *
 * 1. 타입 안전성 (Type Safety)
 *    - 컴파일 타임에 오류 검출 가능
 *    - Groovy는 런타임에 오류 발견 (빌드 실패 후 알게 됨)
 *
 * 2. IDE 지원 강화
 *    - 자동완성, 코드 네비게이션, 리팩토링 지원
 *    - 메서드/프로퍼티 문서를 IDE에서 바로 확인 가능
 *
 * 3. 언어 일관성
 *    - Kotlin 프로젝트라면 빌드 스크립트도 Kotlin으로 통일
 *    - 팀원들이 Groovy 문법을 따로 배울 필요 없음
 *
 * 4. 컴파일 타임 검증
 *    - 잘못된 플러그인 설정, 의존성 오타 등을 즉시 발견
 *    - 빌드 실행 전에 문제 파악 가능
 *
 * 단점: Groovy보다 빌드 속도가 약간 느릴 수 있음 (첫 실행 시)
 */

plugins {
    id("org.jetbrains.kotlin.jvm") version "1.9.25"
    id("org.jetbrains.kotlin.plugin.spring") version "1.9.25"
    id("org.springframework.boot") version "3.5.6"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "com.example"
version = "0.0.1-SNAPSHOT"
description = "SKK3s"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}